package com.dataverse.rentals.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TipoMedidorConverter implements AttributeConverter<TipoMedidor, Integer> {

    @Override
    public Integer convertToDatabaseColumn(TipoMedidor tipo) {
        return tipo == null ? null : tipo.getValor();
    }

    @Override
    public TipoMedidor convertToEntityAttribute(Integer valor) {
        return valor == null ? null : TipoMedidor.desdeValor(valor);
    }
}