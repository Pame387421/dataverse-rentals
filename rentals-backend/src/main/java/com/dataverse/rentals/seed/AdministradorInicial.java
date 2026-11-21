package com.dataverse.rentals.seed;

import com.dataverse.rentals.entity.Cuenta;
import com.dataverse.rentals.entity.Usuario;
import com.dataverse.rentals.repository.CuentaRepository;
import com.dataverse.rentals.repository.UsuarioRepository;
import com.dataverse.rentals.security.ClaveHasher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea la cuenta del primer administrador si ADMIN_CORREO y ADMIN_CLAVE están
 * definidas y la cuenta aún no existe.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdministradorInicial implements ApplicationRunner {
    private final CuentaRepository cuentas;
    private final UsuarioRepository usuarios;
    private final ClaveHasher hasher;
    @Value("${app.bootstrap.admin-correo:}")
    private String correo;
    @Value("${app.bootstrap.admin-clave:}")
    private String clave;
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (correo.isBlank() || clave.isBlank())
            return;
        String c = correo.trim();
        if (cuentas.findByCorreoIgnoreCase(c).isPresent())
            return;

        if (clave.length() < 12) {
            log.error("ADMIN_CLAVE debe tener al menos 12 caracteres. No se creó la cuenta del administrador.");
            return;
        }
        Usuario perfil = usuarios.findByCorreoIgnoreCase(c).orElse(null);
        if (perfil == null || !"Administrador".equals(perfil.getTipoUsuario().getNombre())) {
            log.error("No existe un usuario con rol Administrador y correo {}. No se creó la cuenta.", c);
            return;
        }
        Cuenta cuenta = new Cuenta();
        cuenta.setCorreo(perfil.getCorreo());
        cuenta.setClaveHash(hasher.hash(clave));
        cuenta.setNombreCompleto(perfil.getNombre());
        cuenta.setRol("Administrador");
        cuenta.setUsuario(perfil);
        cuenta.setDebeCambiarClave(true); // la clave de ADMIN_CLAVE sirve solo para el primer ingreso
        cuentas.save(cuenta);
        log.info(
                "Cuenta del administrador inicial creada para {}. Active la verificación en dos pasos desde «Seguridad» "
                        + "y luego quite ADMIN_CLAVE de la configuración.",
                c);
    }
}