package com.dataverse.rentals.service;

import com.dataverse.rentals.dto.AuthDtos.ConfigurarMfa;
import com.dataverse.rentals.dto.AuthDtos.RespuestaAuth;
import com.dataverse.rentals.dto.AuthDtos.SolicitudLogin;
import com.dataverse.rentals.dto.AuthDtos.SolicitudMfa;
import com.dataverse.rentals.entity.Cuenta;
import com.dataverse.rentals.exception.ApiException;
import com.dataverse.rentals.repository.CuentaRepository;
import com.dataverse.rentals.security.ClaveHasher;
import com.dataverse.rentals.security.JwtService;
import com.dataverse.rentals.security.LimiteIntentos;
import com.dataverse.rentals.security.Sesion;
import com.dataverse.rentals.security.TotpService;
import com.auth0.jwt.exceptions.JWTVerificationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dataverse.rentals.dto.AuthDtos.CambiarClave;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Set<String> ROLES = Set.of("Administrador", "Inquilino");
    private static final String EMISOR = "Dataverse Rentals";

    private final CuentaRepository cuentas;
    private final ClaveHasher hasher;
    private final TotpService totp;
    private final JwtService jwt;
    private final LimiteIntentos limite;

    /** Paso 1: correo y clave. Si la cuenta tiene verificación en dos pasos, devuelve un token temporal. */
    @Transactional(readOnly = true)
    public RespuestaAuth login(SolicitudLogin solicitud) {
        String correo = solicitud.email().trim();
        String llave = correo.toLowerCase();
        if (limite.bloqueado(llave)) throw demasiadosIntentos();

        Cuenta cuenta = cuentas.findByCorreoIgnoreCase(correo).orElse(null);
        if (cuenta == null || !cuenta.isActiva() || !hasher.verificar(solicitud.clave(), cuenta.getClaveHash())) {
            limite.fallo(llave);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas. Intente nuevamente.");
        }

        Sesion sesion = aSesion(cuenta);
        if (cuenta.isMfaActivo()) {
            return new RespuestaAuth(true, jwt.crear(JwtService.MFA, sesion), null, null);
        }
        limite.exito(llave);
        return new RespuestaAuth(false, null, jwt.crear(JwtService.ACCESO, sesion), sesion);
    }

    /** Paso 2: código de 6 dígitos de la aplicación autenticadora. */
    @Transactional(readOnly = true)
    public RespuestaAuth verificarMfa(SolicitudMfa solicitud) {
        Sesion sesion;
        try {
            sesion = jwt.verificar(solicitud.tokenTemporal(), JwtService.MFA);
        } catch (JWTVerificationException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "La verificación expiró. Inicie sesión nuevamente.");
        }

        String llave = "mfa:" + sesion.email().toLowerCase();
        if (limite.bloqueado(llave)) throw demasiadosIntentos();

        Cuenta cuenta = cuentas.findByCorreoIgnoreCase(sesion.email()).orElse(null);
        String codigo = solicitud.codigo().replace(" ", "");
        if (cuenta == null || !cuenta.isMfaActivo() || !totp.verificar(cuenta.getMfaSecreto(), codigo)) {
            limite.fallo(llave);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Código de verificación incorrecto. Revisa tu aplicación.");
        }

        limite.exito(llave);
        limite.exito(sesion.email().toLowerCase());
        return new RespuestaAuth(false, null, jwt.crear(JwtService.ACCESO, sesion), sesion);
    }

    /** Genera una clave nueva para vincular la aplicación autenticadora (aún sin activar). */
    @Transactional
    public ConfigurarMfa iniciarConfiguracionMfa(Sesion sesion) {
        Cuenta cuenta = buscar(sesion);
        if (cuenta.isMfaActivo()) {
            throw new ApiException(HttpStatus.CONFLICT, "La verificación en dos pasos ya está activada.");
        }
        String secreto = totp.generarSecreto();
        cuenta.setMfaSecreto(secreto);
        cuentas.save(cuenta);
        return new ConfigurarMfa(agrupar(secreto), uriOtpAuth(cuenta.getCorreo(), secreto));
    }

    /** Activa la verificación en dos pasos si el código coincide con la clave generada. */
    @Transactional
    public void activarMfa(Sesion sesion, String codigo) {
        Cuenta cuenta = buscar(sesion);
        if (cuenta.getMfaSecreto() == null || cuenta.getMfaSecreto().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Primero genere la clave de configuración.");
        }
        if (!totp.verificar(cuenta.getMfaSecreto(), codigo.replace(" ", ""))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Código de verificación incorrecto.");
        }
        cuenta.setMfaActivo(true);
        cuentas.save(cuenta);
    }

        /**
     * Cambia la contraseña y devuelve una sesión nueva, ya sin la marca de clave temporal.
     * Una clave actual incorrecta responde 400 (no 401, para que Angular no cierre la sesión).
     */
    @Transactional
    public RespuestaAuth cambiarClave(Sesion sesion, CambiarClave s) {
        String llave = "cambio:" + sesion.email().toLowerCase();
        if (limite.bloqueado(llave)) throw demasiadosIntentos();

        Cuenta cuenta = buscar(sesion);
        if (!hasher.verificar(s.claveActual(), cuenta.getClaveHash())) {
            limite.fallo(llave);
            throw new ApiException(HttpStatus.BAD_REQUEST, "La contraseña actual es incorrecta.");
        }
        if (!s.claveNueva().matches("(?=.*[A-Za-z])(?=.*\\d).+")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La nueva contraseña debe incluir letras y números.");
        }
        if (s.claveNueva().equals(s.claveActual())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La nueva contraseña debe ser distinta de la actual.");
        }

        cuenta.setClaveHash(hasher.hash(s.claveNueva()));
        cuenta.setDebeCambiarClave(false);
        cuentas.save(cuenta);
        limite.exito(llave);

        Sesion nueva = aSesion(cuenta);
        return new RespuestaAuth(false, null, jwt.crear(JwtService.ACCESO, nueva), nueva);
    }

    private Cuenta buscar(Sesion sesion) {
        return cuentas.findByCorreoIgnoreCase(sesion.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "La cuenta ya no existe."));
    }

    private Sesion aSesion(Cuenta cuenta) {
        if (!ROLES.contains(cuenta.getRol())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Tu cuenta no tiene acceso al portal.");
        }
        if ("Inquilino".equals(cuenta.getRol()) && cuenta.getUsuario() == null) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Tu perfil de negocio no está vinculado a la cuenta.");
        }
        String nombre = cuenta.getNombreCompleto() == null || cuenta.getNombreCompleto().isBlank()
                ? cuenta.getCorreo() : cuenta.getNombreCompleto();
        Integer usuarioId = cuenta.getUsuario() != null ? cuenta.getUsuario().getId() : null;
        return new Sesion(cuenta.getCorreo(), nombre, cuenta.getRol(), usuarioId, cuenta.isDebeCambiarClave());
    }

    private static ApiException demasiadosIntentos() {
        return new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                "Demasiados intentos. Espere unos minutos e inténtelo de nuevo.");
    }

    private static String agrupar(String secreto) {
        return secreto.replaceAll("(.{4})(?=.)", "$1 ");
    }

    /** Lo que se codifica en el QR de Google Authenticator. */
    public static String uriOtpAuth(String correo, String secreto) {
        String emisor = URLEncoder.encode(EMISOR, StandardCharsets.UTF_8).replace("+", "%20");
        String etiqueta = URLEncoder.encode(EMISOR + ":" + correo, StandardCharsets.UTF_8).replace("+", "%20");
        return "otpauth://totp/" + etiqueta + "?secret=" + secreto + "&issuer=" + emisor;
    }
}