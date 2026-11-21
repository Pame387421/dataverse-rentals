package com.dataverse.rentals.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class PagoDtos {

    private PagoDtos() {}

    public record PagoResponse(Integer id, Integer boletaId, String inquilino, String nombreDepartamento,
                               @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime fechaPago,
                               BigDecimal alquiler, BigDecimal consumoLuz, BigDecimal consumoAgua,
                               BigDecimal consumoGas, BigDecimal arbitrios, BigDecimal montoTotal,
                               BigDecimal montoAbonado, BigDecimal saldoPendiente,
                               boolean tieneVoucherPdf, String urlVoucher) {}
}