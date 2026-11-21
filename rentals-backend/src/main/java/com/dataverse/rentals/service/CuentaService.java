package com.dataverse.rentals.service;

import com.dataverse.rentals.entity.Cuenta;
import com.dataverse.rentals.entity.Usuario;
import com.dataverse.rentals.exception.ApiException;
import com.dataverse.rentals.repository.CuentaRepository;
import com.dataverse.rentals.security.ClaveHasher;
import com.dataverse.rentals.security.LimiteIntentos;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CuentaService {

    /** Los únicos roles que pueden iniciar sesión en el portal. */
    public static final Set<String> ROLES_CON_ACCESO = Set.of("Administrador", "Inquilino");

    // Sin caracteres que se confunden al leerlos (0/O, 1/l/I)
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

    public record ClaveGenerada(String correo, String claveTemporal, boolean cuentaNueva) {}

    private final CuentaRepository cuentas;
    private final ClaveHasher hasher;
    private final LimiteIntentos limite;
    private final SecureRandom azar = new SecureRandom();

    /** Crea la cuenta del usuario o reinicia su clave. La contraseña temporal solo se devuelve aquí, nunca se guarda. */
    @Transactional
    public ClaveGenerada generarAcceso(Usuario u, boolean reiniciarMfa) {
        String rol = u.getTipoUsuario().getNombre();
        if (!ROLES_CON_ACCESO.contains(rol)) {
            throw new ApiException(HttpStatus.CONFLICT, "El rol «" + rol
                    + "» no tiene acceso al portal. Solo Administrador e Inquilino pueden iniciar sesión.");
        }

        Cuenta c = cuentas.findByUsuarioId(u.getId())
                .or(() -> cuentas.findByCorreoIgnoreCase(u.getCorreo()))
                .orElse(null);
        boolean nueva = c == null;
        if (nueva) c = new Cuenta();

        String clave = generar();
        c.setUsuario(u);
        c.setCorreo(u.getCorreo());
        c.setNombreCompleto(u.getNombre());
        c.setRol(rol);
        c.setActiva(true);
        c.setClaveHash(hasher.hash(clave));
        c.setDebeCambiarClave(true);
        if (reiniciarMfa) {
            c.setMfaActivo(false);
            c.setMfaSecreto(null);
        }
        cuentas.save(c);

        // Si estaba bloqueada por intentos fallidos, queda liberada
        limite.exito(u.getCorreo().toLowerCase());
        limite.exito("mfa:" + u.getCorreo().toLowerCase());
        return new ClaveGenerada(u.getCorreo(), clave, nueva);
    }

    /** 14 caracteres con al menos una letra y un número. */
    private String generar() {
        String clave;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 14; i++) sb.append(ALFABETO.charAt(azar.nextInt(ALFABETO.length())));
            clave = sb.toString();
        } while (!clave.matches(".*[A-Za-z].*") || !clave.matches(".*\\d.*"));
        return clave;
    }
}