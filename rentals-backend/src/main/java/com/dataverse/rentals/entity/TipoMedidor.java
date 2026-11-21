package com.dataverse.rentals.entity;

/** En la base se guarda como número: 1 = Luz, 2 = Agua, 3 = Gas. */
public enum TipoMedidor {
    LUZ(1), AGUA(2), GAS(3);

    private final int valor;

    TipoMedidor(int valor) {
        this.valor = valor;
    }

    public int getValor() {
        return valor;
    }

    public static TipoMedidor desdeValor(int valor) {
        for (TipoMedidor t : values()) {
            if (t.valor == valor) return t;
        }
        throw new IllegalArgumentException("Valor de medidor inválido: " + valor);
    }
}