package com.dataverse.rentals.dto;

import com.dataverse.rentals.security.Sesion;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {}

    public record SolicitudLogin(
            @NotBlank(message = "Ingrese el correo electrónico.")
            @Email(message = "Ingrese un correo electrónico válido.") String email,
            @NotBlank(message = "Ingrese la contraseña.") String clave) {}

    public record SolicitudMfa(
            @NotBlank(message = "La verificación expiró. Inicie sesión nuevamente.") String tokenTemporal,
            @NotBlank(message = "Ingrese el código de verificación.") String codigo) {}

    public record ActivarMfa(
            @NotBlank(message = "Ingrese el código de verificación.") String codigo) {}

    /** Si requiereMfa es true se recibe tokenTemporal; si es false se recibe token y usuario. */
    public record RespuestaAuth(boolean requiereMfa, String tokenTemporal, String token, Sesion usuario) {}

    public record ConfigurarMfa(String clave, String uri) {}

     public record CambiarClave(
            @NotBlank(message = "Ingrese su contraseña actual.") String claveActual,
            @NotBlank(message = "Ingrese la nueva contraseña.")
            @Size(min = 10, max = 100, message = "La nueva contraseña debe tener entre 10 y 100 caracteres.") String claveNueva) {}
            
}