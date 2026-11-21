package com.dataverse.rentals.dto;

public final class ConsultaDtos {

    private ConsultaDtos() {}

    public record DocumentoConsultado(String tipo, String numero, String nombre, String direccion) {}
}