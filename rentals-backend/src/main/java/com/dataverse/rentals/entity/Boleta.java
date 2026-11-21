package com.dataverse.rentals.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Boletas")
@Getter
@Setter
@NoArgsConstructor
public class Boleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ContratoId", nullable = false)
    private Contrato contrato;

    @Column(name = "FechaEmision", nullable = false)
    private LocalDateTime fechaEmision;

    @Column(name = "FechaVencimiento", nullable = false)
    private LocalDateTime fechaVencimiento;

    @Column(name = "ConsumoLuz", nullable = false, precision = 18, scale = 2)
    private BigDecimal consumoLuz = BigDecimal.ZERO;

    @Column(name = "ConsumoAgua", nullable = false, precision = 18, scale = 2)
    private BigDecimal consumoAgua = BigDecimal.ZERO;

    @Column(name = "ConsumoGas", nullable = false, precision = 18, scale = 2)
    private BigDecimal consumoGas = BigDecimal.ZERO;

    @Column(name = "Arbitrios", nullable = false, precision = 18, scale = 2)
    private BigDecimal arbitrios = BigDecimal.ZERO;

    @Column(name = "MontoTotal", nullable = false, precision = 18, scale = 2)
    private BigDecimal montoTotal = BigDecimal.ZERO;

    @Convert(converter = EstadoBoletaConverter.class)
    @Column(name = "Estado", nullable = false)
    private EstadoBoleta estado = EstadoBoleta.PENDIENTE;

    @Column(name = "FechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "FechaActualizacion")
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "boleta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<DetalleBoleta> detalles = new ArrayList<>();

    public void agregarDetalle(String concepto, BigDecimal monto) {
        DetalleBoleta d = new DetalleBoleta();
        d.setBoleta(this);
        d.setConcepto(concepto);
        d.setMonto(monto);
        detalles.add(d);
    }
}