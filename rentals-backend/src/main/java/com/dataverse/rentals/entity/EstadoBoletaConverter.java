package com.dataverse.rentals.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EstadoBoletaConverter implements AttributeConverter<EstadoBoleta, Integer> {

    @Override
    public Integer convertToDatabaseColumn(EstadoBoleta estado) {
        return estado == null ? null : estado.getValor();
    }

    @Override
    public EstadoBoleta convertToEntityAttribute(Integer valor) {
        return valor == null ? null : EstadoBoleta.desdeValor(valor);
    }
}