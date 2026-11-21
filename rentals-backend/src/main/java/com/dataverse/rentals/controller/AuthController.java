package com.dataverse.rentals.controller;

import com.dataverse.rentals.dto.AuthDtos.ActivarMfa;
import com.dataverse.rentals.dto.AuthDtos.ConfigurarMfa;
import com.dataverse.rentals.dto.AuthDtos.RespuestaAuth;
import com.dataverse.rentals.dto.AuthDtos.SolicitudLogin;
import com.dataverse.rentals.dto.AuthDtos.SolicitudMfa;
import com.dataverse.rentals.security.Sesion;
import com.dataverse.rentals.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.dataverse.rentals.dto.AuthDtos.CambiarClave;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
//@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService auth;

        /** POST /api/auth/cambiar-clave  { claveActual, claveNueva }: devuelve una sesión nueva. */
    @PostMapping("/cambiar-clave")
    public RespuestaAuth cambiarClave(@RequestAttribute(Sesion.ATRIBUTO) Sesion sesion,
                                      @Valid @RequestBody CambiarClave solicitud) {
        return auth.cambiarClave(sesion, solicitud);
    }

    /** POST /api/auth/login  { email, clave }  (público) */
    @PostMapping("/login")
    public RespuestaAuth login(@Valid @RequestBody SolicitudLogin solicitud) {
        return auth.login(solicitud);
    }

    /** POST /api/auth/verificar-2fa  { tokenTemporal, codigo }  (público) */
    @PostMapping("/verificar-2fa")
    public RespuestaAuth verificar(@Valid @RequestBody SolicitudMfa solicitud) {
        return auth.verificarMfa(solicitud);
    }

    /** GET /api/auth/me: devuelve la sesión del token (sirve para restaurar la sesión en Angular). */
    @GetMapping("/me")
    public Sesion me(@RequestAttribute(Sesion.ATRIBUTO) Sesion sesion) {
        return sesion;
    }

    /** POST /api/auth/mfa/iniciar: genera la clave para la aplicación autenticadora. */
    @PostMapping("/mfa/iniciar")
    public ConfigurarMfa iniciarMfa(@RequestAttribute(Sesion.ATRIBUTO) Sesion sesion) {
        return auth.iniciarConfiguracionMfa(sesion);
    }

    /** POST /api/auth/mfa/activar  { codigo } */
    @PostMapping("/mfa/activar")
    public Map<String, String> activarMfa(@RequestAttribute(Sesion.ATRIBUTO) Sesion sesion,
                                          @Valid @RequestBody ActivarMfa solicitud) {
        auth.activarMfa(sesion, solicitud.codigo());
        return Map.of("mensaje", "La verificación en dos pasos quedó activada.");
    }
}