package com.dataverse.rentals.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Encuestas")
@Getter
@Setter
@NoArgsConstructor
public class Encuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "UsuarioId", nullable = false)
    private Usuario usuario;

    @NotNull
    @Min(value = 1, message = "La calificación debe ser un número del 1 al 5.")
    @Max(value = 5, message = "La calificación debe ser un número del 1 al 5.")
    @Column(name = "Calificacion", nullable = false)
    private Integer calificacion;

    @NotBlank(message = "El comentario es obligatorio.")
    @Size(max = 500, message = "El comentario no puede superar los 500 caracteres.")
    @Column(name = "Comentario", nullable = false, length = 500)
    private String comentario;

    @Column(name = "FechaRegistro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();
}