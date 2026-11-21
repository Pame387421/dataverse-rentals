package com.dataverse.rentals.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Restringe un controlador o un método a ciertos roles. Sin la anotación, basta con tener sesión. */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RolesPermitidos {
    String[] value();
}