package com.dataverse.rentals.seed;

import com.dataverse.rentals.entity.Cuenta;
import com.dataverse.rentals.entity.Usuario;
import com.dataverse.rentals.repository.CuentaRepository;
import com.dataverse.rentals.repository.UsuarioRepository;
import com.dataverse.rentals.security.ClaveHasher;
import com.dataverse.rentals.security.TotpService;
import com.dataverse.rentals.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** SOLO DESARROLLO: crea cuentas de prueba si la tabla Cuentas está vacía. Desactívalo con app.seed.habilitado=false. */
@Slf4j
@Component
@RequiredArgsConstructor
public class SembradorCuentas implements ApplicationRunner {

    private final CuentaRepository cuentas;
    private final UsuarioRepository usuarios;
    private final ClaveHasher hasher;
    private final TotpService totp;

    @Value("${app.seed.habilitado:true}")
    private boolean habilitado;

    @Value("${app.seed.mfa-admin:true}")
    private boolean mfaAdmin;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!habilitado || cuentas.count() > 0) return;

        crear("admin@dataverserentals.pe", "Admin123!", "Administrador", mfaAdmin);
        crear("carlos.ramirez@correo.pe", "Inquilino123!", "Inquilino", false);
        crear("maria.torres@correo.pe", "Inquilino123!", "Inquilino", false);
        crear("luis.fernandez@correo.pe", "Inquilino123!", "Inquilino", false);
    }

    private void crear(String correo, String clave, String rol, boolean conMfa) {
        Usuario perfil = usuarios.findByCorreoIgnoreCase(correo).orElse(null);
        if (perfil == null) {
            log.warn("No se creó la cuenta {}: no existe un usuario con ese correo. Ejecute primero el script SQL.", correo);
            return;
        }

        Cuenta c = new Cuenta();
        c.setCorreo(correo);
        c.setClaveHash(hasher.hash(clave));
        c.setNombreCompleto(perfil.getNombre());
        c.setRol(rol);
        c.setUsuario(perfil);
        if (conMfa) {
            String secreto = totp.generarSecreto();
            c.setMfaSecreto(secreto);
            c.setMfaActivo(true);
            log.info("MFA de {} -> clave: {}  |  uri: {}", correo, secreto, AuthService.uriOtpAuth(correo, secreto));
        }
        cuentas.save(c);
        log.info("Cuenta de prueba creada: {} ({})", correo, rol);
    }
}