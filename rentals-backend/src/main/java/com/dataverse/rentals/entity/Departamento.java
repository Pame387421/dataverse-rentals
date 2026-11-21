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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "Departamentos")
@Getter
@Setter
@NoArgsConstructor
public class Departamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @NotNull(message = "El piso debe estar entre 1 y 100.")
    @Min(value = 1, message = "El piso debe estar entre 1 y 100.")
    @Max(value = 100, message = "El piso debe estar entre 1 y 100.")
    @Column(name = "Piso", nullable = false)
    private Integer piso;

    @NotBlank(message = "El número de departamento es obligatorio (máx. 20 caracteres).")
    @Size(max = 20, message = "El número de departamento es obligatorio (máx. 20 caracteres).")
    @Column(name = "NumeroDepartamento", nullable = false, length = 20)
    private String numeroDepartamento;

    @NotNull(message = "Ingrese un área mayor que cero.")
    @DecimalMin(value = "0.00", inclusive = false, message = "Ingrese un área mayor que cero.")
    @Column(name = "AreaM2", nullable = false, precision = 10, scale = 2)
    private BigDecimal areaM2;

    @NotBlank(message = "Seleccione el estado de acabados.")
    @Pattern(regexp = "Nuevo|Remodelado|En buen estado|Necesita reparaciones|Obsoleto",
            message = "El estado de acabados no es válido.")
    @Column(name = "EstadoAcabados", nullable = false, length = 30)
    private String estadoAcabados;

    @NotNull(message = "Seleccione un edificio.")
    @ManyToOne(optional = false)
    @JoinColumn(name = "EdificioId", nullable = false)
    private Edificio edificio;
}