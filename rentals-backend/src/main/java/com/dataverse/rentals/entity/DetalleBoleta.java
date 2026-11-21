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

@Entity
@Table(name = "DetallesBoleta")
@Getter
@Setter
@NoArgsConstructor
public class DetalleBoleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "BoletaId", nullable = false)
    private Boleta boleta;

    @Column(name = "Concepto", nullable = false, length = 200)
    private String concepto;

    @Column(name = "Monto", nullable = false, precision = 18, scale = 2)
    private BigDecimal monto;
}