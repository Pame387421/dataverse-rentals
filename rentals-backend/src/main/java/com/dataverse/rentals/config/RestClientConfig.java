package com.dataverse.rentals.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient consultaDocumentoRestClient(
            RestClient.Builder builder,
            @Value("${app.consulta-documento.url}") String url,
            @Value("${app.consulta-documento.token:}") String token,
            @Value("${app.consulta-documento.timeout-segundos:5}") long timeoutSegundos) {

        Duration timeout = Duration.ofSeconds(timeoutSegundos);
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(timeout).build());
        fabrica.setReadTimeout(timeout);

        RestClient.Builder cliente = builder
                .baseUrl(url)
                .requestFactory(fabrica)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        if (token != null && !token.isBlank()) {
            cliente.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return cliente.build();
    }
}