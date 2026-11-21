package com.dataverse.rentals.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class UsuarioDtos {

    private UsuarioDtos() {}

    public record UsuarioRequest(
            @NotBlank(message = "El nombre es obligatorio.")
            @Size(max = 100, message = "El nombre no puede superar los 100 caracteres.") String nombre,

            @NotBlank(message = "El correo es obligatorio.")
            @Email(message = "Ingrese un correo electrónico válido.")
            @Size(max = 100, message = "El correo no puede superar los 100 caracteres.") String correo,

            @NotBlank(message = "Seleccione el tipo de documento.")
            @Pattern(regexp = "DNI|RUC|CE|Pasaporte", message = "El tipo de documento no es válido.") String tipoDocumento,

            @NotBlank(message = "El número de documento es obligatorio.")
            @Size(max = 20, message = "El número de documento no puede superar los 20 caracteres.") String numeroDocumento,

            @NotNull(message = "Seleccione un rol.") Integer tipoUsuarioId,

            @Size(max = 500, message = "La URL del voucher no puede superar los 500 caracteres.")
            @Pattern(regexp = "^$|^https?://.+", message = "La URL del voucher debe empezar con http:// o https://.") String urlVoucher) {}

    /** Lo único que puede cambiar un inquilino de su propio perfil. */
    public record PerfilRequest(
            @NotBlank(message = "El nombre es obligatorio.")
            @Size(max = 100, message = "El nombre no puede superar los 100 caracteres.") String nombre,

            @NotBlank(message = "Seleccione el tipo de documento.")
            @Pattern(regexp = "DNI|RUC|CE|Pasaporte", message = "El tipo de documento no es válido.") String tipoDocumento,

            @NotBlank(message = "El número de documento es obligatorio.")
            @Size(max = 20, message = "El número de documento no puede superar los 20 caracteres.") String numeroDocumento) {}

        public record UsuarioResponse(Integer id, String nombre, String tipoDocumento, String numeroDocumento,
                                  String correo, String urlVoucher, String contratoPdf,
                                  Integer tipoUsuarioId, String nombreRol,
                                  boolean tieneCuenta, boolean mfaActivo, boolean pendientePrimerIngreso) {}

    public record TipoUsuarioResponse(Integer id, String nombre, String descripcion) {}

    public record Creado(String mensaje, Integer id) {}
}