package com.dataverse.rentals.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class MedidorDtos {

    private MedidorDtos() {}

    public record MedidorRequest(
            @NotBlank(message = "El número de serie es obligatorio (máx. 50 caracteres).")
            @Size(max = 50, message = "El número de serie es obligatorio (máx. 50 caracteres).") String numeroSerie,

            @NotBlank(message = "Seleccione el tipo de medidor.")
            @Pattern(regexp = "LUZ|AGUA|GAS", message = "El tipo de medidor no es válido (LUZ, AGUA o GAS).") String tipo,

            @NotNull(message = "Seleccione un departamento.") Integer departamentoId) {}

    public record MedidorResponse(Integer id, String numeroSerie, String tipo, Integer departamentoId,
                                  String edificioNombre, Integer piso, String numeroDepartamento) {}
}