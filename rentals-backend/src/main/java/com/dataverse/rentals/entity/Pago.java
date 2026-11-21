package com.dataverse.rentals.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Pagos")
@Getter
@Setter
@NoArgsConstructor
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ContratoId", nullable = false)
    private Contrato contrato;

    @ManyToOne(optional = false)
    @JoinColumn(name = "DepartamentoId", nullable = false)
    private Departamento departamento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "BoletaId", nullable = false)
    private Boleta boleta;

    @Column(name = "ConsumoLuz", nullable = false, precision = 18, scale = 2)
    private BigDecimal consumoLuz = BigDecimal.ZERO;

    @Column(name = "ConsumoAgua", nullable = false, precision = 18, scale = 2)
    private BigDecimal consumoAgua = BigDecimal.ZERO;

    @Column(name = "ConsumoGas", nullable = false, precision = 18, scale = 2)
    private BigDecimal consumoGas = BigDecimal.ZERO;

    @Column(name = "Arbitrios", nullable = false, precision = 18, scale = 2)
    private BigDecimal arbitrios = BigDecimal.ZERO;

    @Column(name = "MontoAbonado", nullable = false, precision = 18, scale = 2)
    private BigDecimal montoAbonado;

    /** Saldo de la boleta después de este pago. */
    @Column(name = "SaldoPendiente", nullable = false, precision = 18, scale = 2)
    private BigDecimal saldoPendiente;

    @Column(name = "MontoTotal", nullable = false, precision = 18, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "VoucherPdf", length = 500)
    private String voucherPdf;

    @Column(name = "UrlVoucher", length = 500)
    private String urlVoucher;

    @Column(name = "FechaPago", nullable = false)
    private LocalDateTime fechaPago = LocalDateTime.now();
}