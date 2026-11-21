package com.dataverse.rentals.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Edificios")
@Getter
@Setter
@NoArgsConstructor
public class Edificio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres.")
    @Column(name = "Nombre", nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "La dirección es obligatoria.")
    @Size(max = 200, message = "La dirección no puede superar los 200 caracteres.")
    @Column(name = "Direccion", nullable = false, length = 200)
    private String direccion;

    @NotNull(message = "Ingrese un número de pisos entre 1 y 100.")
    @Min(value = 1, message = "Ingrese un número de pisos entre 1 y 100.")
    @Max(value = 100, message = "Ingrese un número de pisos entre 1 y 100.")
    @Column(name = "Pisos", nullable = false)
    private Integer pisos;
}