package com.dataverse.rentals.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "TiposUsuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @NotBlank(message = "El nombre del rol es obligatorio.")
    @Size(max = 50, message = "El nombre del rol no puede superar los 50 caracteres.")
    @Column(name = "Nombre", nullable = false, length = 50)
    private String nombre;

    @Size(max = 200)
    @Column(name = "Descripcion", length = 200)
    private String descripcion;
}