package com.dataverse.rentals.entity;

/** En la base se guarda como número: 0 = Pendiente, 1 = Pagada, 2 = Vencida, 3 = Anulada. */
public enum EstadoBoleta {
    PENDIENTE(0), PAGADA(1), VENCIDA(2), ANULADA(3);

    private final int valor;

    EstadoBoleta(int valor) {
        this.valor = valor;
    }

    public int getValor() {
        return valor;
    }

    public static EstadoBoleta desdeValor(int valor) {
        for (EstadoBoleta e : values()) {
            if (e.valor == valor) return e;
        }
        throw new IllegalArgumentException("Estado de boleta inválido: " + valor);
    }
}