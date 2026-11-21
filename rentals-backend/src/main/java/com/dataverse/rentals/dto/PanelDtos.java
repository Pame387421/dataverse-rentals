package com.dataverse.rentals.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class PanelDtos {

    private PanelDtos() {}

    public record PagoResumen(@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime fecha,
                              String usuario, String departamento, BigDecimal monto) {}

    public record DashboardResponse(String mes, String oficina, BigDecimal ingresosMensuales, BigDecimal consumosM3,
                                    int contratosPorVencer, long usuariosActivos, int unidadesDisponibles,
                                    long alertas, List<String> mesesLabels, List<BigDecimal> ingresosPorMes,
                                    BigDecimal totalAgua, BigDecimal totalLuz, BigDecimal totalGas,
                                    List<PagoResumen> ultimosPagos) {}

    public record IngresosTresMeses(List<String> etiquetas, List<BigDecimal> valores) {}

    public record ConsumosGlobales(BigDecimal luz, BigDecimal agua, BigDecimal gas) {}

    public record MetricasContratos(int vigentes, int porVencer) {}

    public record ResumenReportes(IngresosTresMeses ingresos, ConsumosGlobales consumos, MetricasContratos contratos) {}

    public record ConsumoUnidad(String departamento, BigDecimal luz, BigDecimal agua, BigDecimal gas,
                                BigDecimal arbitrios, BigDecimal total) {}

    public record IngresoMes(String clave, String etiqueta, int pagos, BigDecimal abonado, BigDecimal saldo) {}

    public record ContratoPorVencer(Integer contratoId, String inquilino, String departamento,
                                    LocalDate fechaFin, long diasRestantes) {}

    public record EstadoSistema(boolean baseDatosConectada, long totalContratos, long totalPagos,
                                long totalSoportes, String estadoGeneral,
                                @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime ultimaActualizacion) {}
}