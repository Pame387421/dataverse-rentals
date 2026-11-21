package com.dataverse.rentals.security;

/** Datos del usuario que viajan dentro del token. Angular los recibe tal cual al iniciar sesión. */
public record Sesion(String email, String nombre, String rol, Integer usuarioId, boolean debeCambiarClave) {

    public static final String ATRIBUTO = "sesion";
}