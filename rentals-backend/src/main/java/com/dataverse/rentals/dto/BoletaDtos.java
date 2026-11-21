package com.dataverse.rentals.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class BoletaDtos {

    private BoletaDtos() {}

    /** Solo contrato y fechas: los montos los calcula el servidor. */
    public record BoletaRequest(
            @NotNull(message = "Seleccione un contrato.") Integer contratoId,
            @NotNull(message = "La fecha de emisión es obligatoria.") LocalDate fechaEmision,
            @NotNull(message = "La fecha de vencimiento es obligatoria.") LocalDate fechaVencimiento) {}

    public record DetalleResponse(String concepto, BigDecimal monto) {}

    public record BoletaResponse(Integer id, Integer contratoId, LocalDate fechaEmision, LocalDate fechaVencimiento,
                                 BigDecimal montoAlquiler, BigDecimal consumoLuz, BigDecimal consumoAgua,
                                 BigDecimal consumoGas, BigDecimal arbitrios, BigDecimal montoTotal,
                                 String estado, BigDecimal saldoPendiente, List<DetalleResponse> detalles,
                                 String inquilino, String departamentoNombre, String edificioNombre,
                                 Integer piso, String numeroDepartamento) {}

    /** La boleta calculada antes de emitirla (lo que Angular muestra en la confirmación). */
    public record BoletaPreviaResponse(Integer contratoId, String inquilino, String departamento,
                                       LocalDate fechaEmision, LocalDate fechaVencimiento,
                                       BigDecimal montoAlquiler, BigDecimal montoArbitrios, BigDecimal consumoAgua,
                                       BigDecimal consumoLuz, BigDecimal consumoGas, BigDecimal montoTotal) {}
}