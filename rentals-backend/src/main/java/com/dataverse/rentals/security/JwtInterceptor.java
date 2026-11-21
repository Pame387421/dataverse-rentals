package com.dataverse.rentals.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.dataverse.rentals.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.Set;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtService jwt;
        /** Con una contraseña temporal solo se permite consultar la sesión y cambiar la clave. */
    private static final Set<String> RUTAS_CAMBIO_CLAVE = Set.of("/api/auth/cambiar-clave", "/api/auth/me");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Las peticiones "preflight" del navegador (OPTIONS) no llevan token
        if (CorsUtils.isPreFlightRequest(request)) return true;

        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera == null || !cabecera.startsWith("Bearer ")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Debe iniciar sesión.");
        }

        Sesion sesion;
        try {
            sesion = jwt.verificar(cabecera.substring(7), JwtService.ACCESO);
        } catch (JWTVerificationException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "La sesión expiró o no es válida.");
        }
        request.setAttribute(Sesion.ATRIBUTO, sesion);
        if (sesion.debeCambiarClave()) {
            String ruta = request.getRequestURI().substring(request.getContextPath().length());
            if (!RUTAS_CAMBIO_CLAVE.contains(ruta)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Debe cambiar su contraseña temporal antes de continuar.");
            }
        }

        if (handler instanceof HandlerMethod metodo) {
            RolesPermitidos roles = metodo.getMethodAnnotation(RolesPermitidos.class);
            if (roles == null) {
                roles = AnnotatedElementUtils.findMergedAnnotation(metodo.getBeanType(), RolesPermitidos.class);
            }
            if (roles != null && Arrays.stream(roles.value()).noneMatch(r -> r.equals(sesion.rol()))) {
                throw new ApiException(HttpStatus.FORBIDDEN, "No tienes permiso para esta operación.");
            }
        }
        return true;
    }
}