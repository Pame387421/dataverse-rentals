package com.dataverse.rentals.security;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 5 fallos por llave (correo o correo+MFA) dentro de 15 minutos bloquean nuevos intentos. */
@Component
public class LimiteIntentos {

    private static final int MAXIMO = 5;
    private static final long VENTANA_MS = 15 * 60 * 1000L;

    private final Map<String, long[]> registro = new ConcurrentHashMap<>();   // {fallos, inicioVentana}

    public boolean bloqueado(String llave) {
        long[] r = registro.get(llave);
        if (r == null) return false;
        if (System.currentTimeMillis() - r[1] > VENTANA_MS) {
            registro.remove(llave);
            return false;
        }
        return r[0] >= MAXIMO;
    }

    public void fallo(String llave) {
        registro.compute(llave, (k, r) -> {
            long ahora = System.currentTimeMillis();
            if (r == null || ahora - r[1] > VENTANA_MS) return new long[]{1, ahora};
            r[0]++;
            return r;
        });
    }

    public void exito(String llave) {
        registro.remove(llave);
    }
}