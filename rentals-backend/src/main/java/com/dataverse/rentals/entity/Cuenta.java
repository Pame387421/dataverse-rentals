package com.dataverse.rentals.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Cuenta de acceso (login). Se vincula con el perfil de negocio (Usuario). */
@Entity
@Table(name = "Cuentas")
@Getter
@Setter
@NoArgsConstructor
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @NotBlank
    @Email
    @Column(name = "Correo", nullable = false, length = 100)
    private String correo;

    @NotBlank
    @Column(name = "ClaveHash", nullable = false, length = 255)
    private String claveHash;

    @NotBlank
    @Column(name = "NombreCompleto", nullable = false, length = 100)
    private String nombreCompleto;

    @Column(name = "MfaSecreto", length = 64)
    private String mfaSecreto;

    @Column(name = "MfaActivo", nullable = false)
    private boolean mfaActivo;

    @NotBlank
    @Column(name = "Rol", nullable = false, length = 30)
    private String rol;

    @Column(name = "Activa", nullable = false)
    private boolean activa = true;

     /** true mientras la persona use una contraseña temporal entregada por el administrador. */
    @Column(name = "DebeCambiarClave", nullable = false)
    private boolean debeCambiarClave;

    @Column(name = "FechaRegistro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "UsuarioId")
    private Usuario usuario;
}