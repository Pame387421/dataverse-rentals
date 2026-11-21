package com.dataverse.rentals.security;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Hash de contraseñas con PBKDF2-HMAC-SHA256 (incluido en el JDK). Formato: pbkdf2_sha256$iteraciones$sal$hash */
@Component
public class ClaveHasher {

    private static final String PREFIJO = "pbkdf2_sha256";
    private static final int ITERACIONES = 210_000;
    private static final int BYTES_SAL = 16;
    private static final int BYTES_HASH = 32;

    private final SecureRandom azar = new SecureRandom();

    public String hash(String clave) {
        byte[] sal = new byte[BYTES_SAL];
        azar.nextBytes(sal);
        byte[] hash = derivar(clave, sal, ITERACIONES);
        return PREFIJO + "$" + ITERACIONES + "$"
                + Base64.getEncoder().encodeToString(sal) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    public boolean verificar(String clave, String almacenado) {
        if (clave == null || almacenado == null) return false;
        String[] partes = almacenado.split("\\$");
        if (partes.length != 4 || !PREFIJO.equals(partes[0])) return false;
        try {
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            return MessageDigest.isEqual(esperado, derivar(clave, sal, iteraciones));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derivar(String clave, byte[] sal, int iteraciones) {
        try {
            PBEKeySpec spec = new PBEKeySpec(clave.toCharArray(), sal, iteraciones, BYTES_HASH * 8);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña.", e);
        }
    }
}