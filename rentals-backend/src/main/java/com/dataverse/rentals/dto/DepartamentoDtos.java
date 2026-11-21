package com.dataverse.rentals.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public final class DepartamentoDtos {

    private DepartamentoDtos() {}

    public record DepartamentoRequest(
            @NotNull(message = "Seleccione un edificio.") Integer edificioId,

            @NotNull(message = "El piso debe estar entre 1 y 100.")
            @Min(value = 1, message = "El piso debe estar entre 1 y 100.")
            @Max(value = 100, message = "El piso debe estar entre 1 y 100.") Integer piso,

            @NotBlank(message = "El número de departamento es obligatorio (máx. 20 caracteres).")
            @Size(max = 20, message = "El número de departamento es obligatorio (máx. 20 caracteres).") String numeroDepartamento,

            @NotNull(message = "Ingrese un área mayor que cero.")
            @DecimalMin(value = "0.00", inclusive = false, message = "Ingrese un área mayor que cero.")
            @Digits(integer = 8, fraction = 2, message = "El área admite hasta 2 decimales.") BigDecimal areaM2,

            @NotBlank(message = "Seleccione el estado de acabados.")
            @Pattern(regexp = "Nuevo|Remodelado|En buen estado|Necesita reparaciones|Obsoleto",
                    message = "El estado de acabados no es válido.") String estadoAcabados) {}

    public record DepartamentoResponse(Integer id, Integer piso, String numeroDepartamento, BigDecimal areaM2,
                                       String estadoAcabados, Integer edificioId, String edificioNombre) {}
}