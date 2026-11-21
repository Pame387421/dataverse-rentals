package com.dataverse.rentals.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Mantenimientos")
@Getter
@Setter
@NoArgsConstructor
public class Mantenimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "Descripcion", nullable = false, length = 500)
    private String descripcion;

    /** Correctivo, Preventivo o Mejora. */
    @Column(name = "Categoria", nullable = false, length = 20)
    private String categoria;

    @Column(name = "CostoMateriales", nullable = false, precision = 18, scale = 2)
    private BigDecimal costoMateriales = BigDecimal.ZERO;

    @Column(name = "CostoManoObra", nullable = false, precision = 18, scale = 2)
    private BigDecimal costoManoObra = BigDecimal.ZERO;

    @Column(name = "FechaRegistro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(name = "FechaEjecucion")
    private LocalDateTime fechaEjecucion;

    @Column(name = "FechaFinalizacion")
    private LocalDateTime fechaFinalizacion;

    /** PENDIENTE, EN_PROCESO o FINALIZADO. */
    @Column(name = "Estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "Observaciones", length = 500)
    private String observaciones;

    @ManyToOne(optional = false)
    @JoinColumn(name = "DepartamentoId", nullable = false)
    private Departamento departamento;

    @ManyToOne
    @JoinColumn(name = "EdificioId")
    private Edificio edificio;

    @ManyToOne
    @JoinColumn(name = "BoletaId")
    private Boleta boleta;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ProveedorId", nullable = false)
    private Proveedor proveedor;
}