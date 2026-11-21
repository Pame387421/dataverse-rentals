package com.dataverse.rentals.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Medidores")
@Getter
@Setter
@NoArgsConstructor
public class Medidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @NotBlank(message = "El número de serie es obligatorio (máx. 50 caracteres).")
    @Size(max = 50, message = "El número de serie es obligatorio (máx. 50 caracteres).")
    @Column(name = "NumeroSerie", nullable = false, length = 50)
    private String numeroSerie;

    @NotNull(message = "Seleccione el tipo de medidor.")
    @Convert(converter = TipoMedidorConverter.class)
    @Column(name = "Tipo", nullable = false)
    private TipoMedidor tipo;

    @NotNull(message = "Seleccione un departamento.")
    @ManyToOne(optional = false)
    @JoinColumn(name = "DepartamentoId", nullable = false)
    private Departamento departamento;
}