package com.dataverse.rentals.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class OperacionDtos {

    private OperacionDtos() {}

    // ---------------------------------------------------------------- Proveedores

    public record ProveedorRequest(
            @NotBlank(message = "El nombre y el RUC del proveedor son campos obligatorios.")
            @Size(max = 100, message = "El nombre no puede superar los 100 caracteres.") String nombre,

            @NotBlank(message = "El nombre y el RUC del proveedor son campos obligatorios.")
            @Pattern(regexp = "^$|\\d{11}", message = "El RUC debe tener exactamente 11 dígitos.") String ruc,

            @NotBlank(message = "El correo es obligatorio.")
            @Email(message = "Ingrese un correo electrónico válido.")
            @Size(max = 100, message = "El correo no puede superar los 100 caracteres.") String correo,

            @NotBlank(message = "El teléfono es obligatorio.")
            @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres.") String telefono) {}

    public record ProveedorResponse(Integer id, String nombre, String ruc, String correo, String telefono) {}

    // ---------------------------------------------------------------- Soporte

    /** estado y mantenimientoId solo se usan al editar: un ticket nuevo siempre nace Pendiente y sin orden. */
    public record SoporteRequest(
            @NotBlank(message = "La descripción técnica del ticket es obligatoria.")
            @Size(max = 500, message = "La descripción no puede superar los 500 caracteres.") String descripcion,

            @NotBlank(message = "Seleccione la prioridad.")
            @Pattern(regexp = "Alta|Media|Baja", message = "La prioridad no es válida.") String prioridad,

            @Pattern(regexp = "PENDIENTE|EN_PROCESO|RESUELTO", message = "El estado no es válido.") String estado,

            @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres.") String observaciones,

            @NotNull(message = "Seleccione un departamento.") Integer departamentoId,

            Integer usuarioId,          // solo lo usa el Administrador; el Inquilino se toma de su sesión
            Integer mantenimientoId) {}

    public record SoporteResponse(
            Integer id, String descripcion,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime fechaRegistro,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime fechaCierre,
            String estado, String prioridad, String observaciones,
            Integer departamentoId, Integer usuarioId, Integer mantenimientoId,
            String nombreDepartamento, String nombreUsuario, String textoMantenimiento) {}

    // ---------------------------------------------------------------- Mantenimientos

    public record MantenimientoRequest(
            @NotBlank(message = "La descripción es obligatoria.")
            @Size(max = 500, message = "La descripción no puede superar los 500 caracteres.") String descripcion,

            @NotBlank(message = "Seleccione la categoría.")
            @Pattern(regexp = "Correctivo|Preventivo|Mejora", message = "La categoría no es válida.") String categoria,

            @NotNull(message = "Ingrese el costo de materiales.")
            @DecimalMin(value = "0.00", message = "El costo de materiales no puede ser negativo.")
            @Digits(integer = 16, fraction = 2, message = "El costo admite hasta 2 decimales.") BigDecimal costoMateriales,

            @NotNull(message = "Ingrese el costo de mano de obra.")
            @DecimalMin(value = "0.00", message = "El costo de mano de obra no puede ser negativo.")
            @Digits(integer = 16, fraction = 2, message = "El costo admite hasta 2 decimales.") BigDecimal costoManoObra,

            LocalDate fechaEjecucion,
            LocalDate fechaFinalizacion,

            @NotBlank(message = "Seleccione el estado.")
            @Pattern(regexp = "PENDIENTE|EN_PROCESO|FINALIZADO", message = "El estado no es válido.") String estado,

            @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres.") String observaciones,

            @NotNull(message = "Seleccione un departamento.") Integer departamentoId,
            @NotNull(message = "Seleccione un proveedor.") Integer proveedorId,

            Integer ticketSoporteId) {}       // solo se usa al crear

    public record MantenimientoResponse(
            Integer id, String descripcion, String categoria, BigDecimal costoMateriales, BigDecimal costoManoObra,
            BigDecimal costoTotal, LocalDate fechaRegistro, LocalDate fechaEjecucion, LocalDate fechaFinalizacion,
            String estado, String observaciones, Integer departamentoId, Integer edificioId, Integer boletaId,
            Integer proveedorId, Integer ticketSoporteId, String nombreDepartamento, String nombreProveedor,
            String textoTicket) {}
}