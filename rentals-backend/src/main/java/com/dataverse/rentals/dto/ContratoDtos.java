package com.dataverse.rentals.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class ContratoDtos {

    private ContratoDtos() {}

    public record ContratoRequest(
            @NotNull(message = "Seleccione una unidad.") Integer departamentoId,
            @NotNull(message = "Seleccione un inquilino.") Integer usuarioId,
            @NotNull(message = "La fecha de inicio es obligatoria.") LocalDate fechaInicioContrato,
            @NotNull(message = "La fecha de finalización es obligatoria.") LocalDate fechaFinContrato,

            @NotNull(message = "El monto de alquiler mensual debe ser mayor a 0.")
            @DecimalMin(value = "0.00", inclusive = false, message = "El monto de alquiler mensual debe ser mayor a 0.")
            @Digits(integer = 16, fraction = 2, message = "El monto admite hasta 2 decimales.") BigDecimal montoAlquiler,

            @Size(max = 500, message = "La URL del voucher no puede superar los 500 caracteres.")
            @Pattern(regexp = "^$|^https?://.+", message = "La URL del voucher debe empezar con http:// o https://.") String urlVoucher) {}

    public record ContratoResponse(Integer id, Integer departamentoId, Integer usuarioId,
                                   LocalDate fechaInicioContrato, LocalDate fechaFinContrato,
                                   BigDecimal montoAlquiler, String urlVoucher,
                                   String departamentoNombre, String usuarioNombre) {}
}