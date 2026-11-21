package com.dataverse.rentals.config;

import com.dataverse.rentals.security.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    /** Orígenes permitidos, separados por coma. Vacío = sin CORS (Angular y la API en el mismo servidor). */
    @Value("${app.cors.origenes:}")
    private String origenes;

    @Override
    public void addInterceptors(InterceptorRegistry registro) {
        registro.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/verificar-2fa");
    }

    @Override
    public void addCorsMappings(CorsRegistry registro) {
        String[] permitidos = Arrays.stream(origenes.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
        if (permitidos.length == 0) return;

        registro.addMapping("/api/**")
                .allowedOrigins(permitidos)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type")
                .maxAge(3600);
    }
}