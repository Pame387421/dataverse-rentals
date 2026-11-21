package com.dataverse.rentals.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Slf4j
@Service
public class JwtService {

    public static final String ACCESO = "acceso";
    public static final String MFA = "mfa";
    private static final String EMISOR = "dataverse-rentals";

    private final Algorithm algoritmo;
    private final Duration vigenciaAcceso;
    private final Duration vigenciaMfa;

    public JwtService(@Value("${app.jwt.secret:}") String secreto,
                      @Value("${app.jwt.horas-acceso:2}") long horasAcceso,
                      @Value("${app.jwt.minutos-mfa:5}") long minutosMfa) {
        if (secreto == null || secreto.length() < 32) {
            byte[] azar = new byte[48];
            new SecureRandom().nextBytes(azar);
            secreto = Base64.getEncoder().encodeToString(azar);
            log.warn("No hay DATAVERSE_JWT_SECRET (mínimo 32 caracteres): se usa una clave temporal. "
                    + "Las sesiones se cerrarán cada vez que se reinicie la aplicación.");
        }
        this.algoritmo = Algorithm.HMAC256(secreto);
        this.vigenciaAcceso = Duration.ofHours(horasAcceso);
        this.vigenciaMfa = Duration.ofMinutes(minutosMfa);
    }

    /** tipo = ACCESO (sesión completa) o MFA (token temporal entre la clave y el código de 6 dígitos). */
    public String crear(String tipo, Sesion sesion) {
        Instant ahora = Instant.now();
        Duration vigencia = MFA.equals(tipo) ? vigenciaMfa : vigenciaAcceso;
        return JWT.create()
                .withIssuer(EMISOR)
                .withSubject(sesion.email())
                .withClaim("tipo", tipo)
                .withClaim("nombre", sesion.nombre())
                .withClaim("rol", sesion.rol())
                .withClaim("uid", sesion.usuarioId())
                .withIssuedAt(ahora)
                .withExpiresAt(ahora.plus(vigencia))
                .sign(algoritmo);
    }

    /** Lanza JWTVerificationException si el token es inválido, venció o no es del tipo esperado. */
    public Sesion verificar(String token, String tipoEsperado) throws JWTVerificationException {
        DecodedJWT jwt = JWT.require(algoritmo)
                .withIssuer(EMISOR)
                .withClaim("tipo", tipoEsperado)
                .build()
                .verify(token);
               return new Sesion(jwt.getSubject(),
                jwt.getClaim("nombre").asString(),
                jwt.getClaim("rol").asString(),
                jwt.getClaim("uid").asInt(),
                Boolean.TRUE.equals(jwt.getClaim("cambiar").asBoolean()));
    }
}