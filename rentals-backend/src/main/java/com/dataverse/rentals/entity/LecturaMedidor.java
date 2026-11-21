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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "LecturasMedidor")
@Getter
@Setter
@NoArgsConstructor
public class LecturaMedidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @NotNull(message = "Seleccione un medidor.")
    @ManyToOne(optional = false)
    @JoinColumn(name = "MedidorId", nullable = false)
    private Medidor medidor;

    @NotNull(message = "La fecha de lectura es obligatoria.")
    @Column(name = "FechaLectura", nullable = false)
    private LocalDateTime fechaLectura;

    @NotNull(message = "El valor del consumo registrado debe ser un número positivo.")
    @DecimalMin(value = "0.00", message = "El valor del consumo registrado debe ser un número positivo.")
    @Column(name = "Valor", nullable = false, precision = 18, scale = 2)
    private BigDecimal valor;
}