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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 100)
    @Column(name = "Nombre", nullable = false, length = 100)
    private String nombre;

    @NotBlank
    @Pattern(regexp = "DNI|RUC|CE|Pasaporte", message = "El tipo de documento no es válido.")
    @Column(name = "TipoDocumento", nullable = false, length = 20)
    private String tipoDocumento;

    @NotBlank(message = "El número de documento es obligatorio.")
    @Size(max = 20)
    @Column(name = "NumeroDocumento", nullable = false, length = 20)
    private String numeroDocumento;

    @NotBlank
    @Email(message = "Ingrese un correo electrónico válido.")
    @Size(max = 100)
    @Column(name = "Correo", nullable = false, length = 100)
    private String correo;

    @Size(max = 500)
    @Column(name = "UrlVoucher", length = 500)
    private String urlVoucher;

    /** Ruta del contrato en PDF. Vacío o "pendiente.pdf" = sin contrato. */
    @Column(name = "ContratoPdf", nullable = false, length = 500)
    private String contratoPdf = "";

    @NotNull(message = "Seleccione un rol.")
    @ManyToOne(optional = false)
    @JoinColumn(name = "TipoUsuarioId", nullable = false)
    private TipoUsuario tipoUsuario;
}