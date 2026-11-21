package com.dataverse.rentals.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Contratos")
@Getter
@Setter
@NoArgsConstructor
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "DepartamentoId", nullable = false)
    private Departamento departamento;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "UsuarioId", nullable = false)
    private Usuario usuario;

    @NotNull
    @Column(name = "FechaInicioContrato", nullable = false)
    private LocalDate fechaInicioContrato;

    @NotNull
    @Column(name = "FechaFinContrato", nullable = false)
    private LocalDate fechaFinContrato;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @Column(name = "MontoAlquiler", nullable = false, precision = 18, scale = 2)
    private BigDecimal montoAlquiler;

    @Size(max = 500)
    @Column(name = "UrlVoucher", length = 500)
    private String urlVoucher;
}