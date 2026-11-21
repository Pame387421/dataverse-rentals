package com.dataverse.rentals.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Proveedores")
@Getter
@Setter
@NoArgsConstructor
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "Nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "Ruc", nullable = false, length = 11)
    private String ruc;

    @Column(name = "Telefono", nullable = false, length = 20)
    private String telefono;

    @Column(name = "Correo", nullable = false, length = 100)
    private String correo;
}