package com.dataverse.rentals.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class EdificioDtos {

    private EdificioDtos() {}

    public record EdificioRequest(
            @NotBlank(message = "El nombre es obligatorio.")
            @Size(max = 100, message = "El nombre no puede superar los 100 caracteres.") String nombre,

            @NotBlank(message = "La dirección es obligatoria.")
            @Size(max = 200, message = "La dirección no puede superar los 200 caracteres.") String direccion,

            @NotNull(message = "Ingrese un número de pisos entre 1 y 100.")
            @Min(value = 1, message = "Ingrese un número de pisos entre 1 y 100.")
            @Max(value = 100, message = "Ingrese un número de pisos entre 1 y 100.") Integer pisos) {}

    public record EdificioResponse(Integer id, String nombre, String direccion, Integer pisos,
                                   Long cantidadDepartamentos) {}
}