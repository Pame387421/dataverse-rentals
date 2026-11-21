package com.dataverse.rentals.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class EncuestaDtos {

    private EncuestaDtos() {}

    /** usuarioId solo lo usa el Administrador; para un Inquilino se toma siempre de su sesión. */
    public record EncuestaRequest(
            Integer usuarioId,

            @NotNull(message = "La calificación es obligatoria.")
            @Min(value = 1, message = "La calificación debe ser un número del 1 al 5.")
            @Max(value = 5, message = "La calificación debe ser un número del 1 al 5.") Integer calificacion,

            @NotBlank(message = "El comentario es obligatorio.")
            @Size(max = 500, message = "El comentario no puede superar los 500 caracteres.") String comentario) {}

    public record EncuestaResponse(
            Integer id, Integer usuarioId, Integer calificacion, String comentario,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime fechaRegistro,
            String nombreUsuario) {}
}