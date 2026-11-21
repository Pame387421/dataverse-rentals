package com.dataverse.rentals.security;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;

@Service
public class TotpService {

    private static final String ALFABETO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int SEGUNDOS_POR_PASO = 30;

    private final SecureRandom azar = new SecureRandom();

    /** Clave secreta en Base32 (20 bytes), la que se escanea o se escribe en la aplicación autenticadora. */
    public String generarSecreto() {
        byte[] bytes = new byte[20];
        azar.nextBytes(bytes);
        return codificar(bytes);
    }

    /** Acepta el código actual y los de la ventana anterior y siguiente (tolerancia de reloj). */
    public boolean verificar(String secreto, String codigo) {
        if (secreto == null || codigo == null || !codigo.matches("[0-9]{6}")) return false;
        long paso = Instant.now().getEpochSecond() / SEGUNDOS_POR_PASO;
        try {
            for (long desfase = -1; desfase <= 1; desfase++) {
                String esperado = codigo(secreto, paso + desfase);
                if (MessageDigest.isEqual(esperado.getBytes(), codigo.getBytes())) return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private String codigo(String secretoBase32, long contador) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(decodificar(secretoBase32), "HmacSHA1"));
        byte[] h = mac.doFinal(ByteBuffer.allocate(8).putLong(contador).array());
        int desplazamiento = h[h.length - 1] & 0x0F;
        int binario = ((h[desplazamiento] & 0x7F) << 24)
                | ((h[desplazamiento + 1] & 0xFF) << 16)
                | ((h[desplazamiento + 2] & 0xFF) << 8)
                | (h[desplazamiento + 3] & 0xFF);
        return String.format("%06d", binario % 1_000_000);
    }

    private static String codificar(byte[] datos) {
        StringBuilder sb = new StringBuilder();
        int buffer = 0, bits = 0;
        for (byte b : datos) {
            buffer = (buffer << 8) | (b & 0xFF);
            bits += 8;
            while (bits >= 5) {
                sb.append(ALFABETO.charAt((buffer >> (bits - 5)) & 31));
                bits -= 5;
            }
            buffer &= (1 << bits) - 1;
        }
        if (bits > 0) sb.append(ALFABETO.charAt((buffer << (5 - bits)) & 31));
        return sb.toString();
    }

    private static byte[] decodificar(String texto) {
        String limpio = texto.replace(" ", "").replace("=", "").toUpperCase();
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        int buffer = 0, bits = 0;
        for (char c : limpio.toCharArray()) {
            int valor = ALFABETO.indexOf(c);
            if (valor < 0) throw new IllegalArgumentException("Clave Base32 inválida.");
            buffer = (buffer << 5) | valor;
            bits += 5;
            if (bits >= 8) {
                salida.write((buffer >> (bits - 8)) & 0xFF);
                bits -= 8;
                buffer &= (1 << bits) - 1;
            }
        }
        return salida.toByteArray();
    }
}