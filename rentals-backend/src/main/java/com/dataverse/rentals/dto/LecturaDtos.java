package com.dataverse.rentals.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class LecturaDtos {

    private LecturaDtos() {}

    public record LecturaRequest(
            @NotNull(message = "Seleccione un medidor.") Integer medidorId,

            @NotNull(message = "La fecha de lectura es obligatoria.") LocalDate fechaLectura,

            @NotNull(message = "El valor del consumo registrado debe ser un número positivo.")
            @DecimalMin(value = "0.00", message = "El valor del consumo registrado debe ser un número positivo.")
            @Digits(integer = 16, fraction = 2, message = "El valor admite hasta 2 decimales.") BigDecimal valor) {}

    public record LecturaResponse(Integer id, Integer medidorId, LocalDate fechaLectura, BigDecimal valor,
                                  String tipo, String numeroSerie, String edificioNombre, Integer piso,
                                  String numeroDepartamento) {}
}