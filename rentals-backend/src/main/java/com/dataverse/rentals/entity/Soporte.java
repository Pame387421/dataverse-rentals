package com.dataverse.rentals.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Soportes")
@Getter
@Setter
@NoArgsConstructor
public class Soporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "Descripcion", nullable = false, length = 500)
    private String descripcion;

    @Column(name = "FechaRegistro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(name = "FechaCierre")
    private LocalDateTime fechaCierre;

    /** PENDIENTE, EN_PROCESO o RESUELTO. */
    @Column(name = "Estado", nullable = false, length = 20)
    private String estado = "PENDIENTE";

    /** Alta, Media o Baja. */
    @Column(name = "Prioridad", nullable = false, length = 10)
    private String prioridad;

    @Column(name = "Observaciones", length = 500)
    private String observaciones;

    @ManyToOne(optional = false)
    @JoinColumn(name = "DepartamentoId", nullable = false)
    private Departamento departamento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "UsuarioId", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "MantenimientoId")
    private Mantenimiento mantenimiento;
}