package com.dataverse.rentals.client;

import com.dataverse.rentals.dto.ConsultaDtos.DocumentoConsultado;
import com.dataverse.rentals.exception.ApiException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Consulta DNI (RENIEC) y RUC (SUNAT) en una API externa. Revisa que los campos coincidan con la API que uses. */
@Component
public class ConsultaDocumentoClient {

    private final RestClient restClient;
    private final boolean tokenConfigurado;

    public ConsultaDocumentoClient(@Qualifier("consultaDocumentoRestClient") RestClient restClient,
                                   @Value("${app.consulta-documento.token:}") String token) {
        this.restClient = restClient;
        this.tokenConfigurado = token != null && !token.isBlank();
    }

    public DocumentoConsultado consultar(String tipo, String numero) {
        String t = tipo == null ? "" : tipo.trim().toUpperCase();
        String n = numero == null ? "" : numero.trim();

        final String ruta = switch (t) {
            case "DNI" -> "/reniec/dni";
            case "RUC" -> "/sunat/ruc";
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Solo se puede consultar DNI o RUC.");
        };
        if (!n.matches("DNI".equals(t) ? "\\d{8}" : "\\d{11}")) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "DNI".equals(t) ? "El DNI debe tener 8 dígitos." : "El RUC debe tener 11 dígitos.");
        }
        if (!tokenConfigurado) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "La consulta de documentos no está configurada (falta la variable APIS_NET_PE_TOKEN).");
        }

        try {
            Map<String, Object> r = restClient.get()
                    .uri(uri -> uri.path(ruta).queryParam("numero", n).build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (r == null) throw new ApiException(HttpStatus.BAD_GATEWAY, "El servicio de consulta no devolvió datos.");

            String nombre = "DNI".equals(t)
                    ? Stream.of(texto(r, "nombres"), texto(r, "apellidoPaterno"), texto(r, "apellidoMaterno"))
                            .filter(s -> !s.isEmpty()).collect(Collectors.joining(" "))
                    : texto(r, "razonSocial");
            return new DocumentoConsultado(t, n, nombre, texto(r, "direccion"));

        } catch (RestClientResponseException e) {
            int estado = e.getStatusCode().value();
            if (estado == 404 || estado == 422) {
                throw new ApiException(HttpStatus.NOT_FOUND, "No se encontró información para ese documento.");
            }
            throw new ApiException(HttpStatus.BAD_GATEWAY, "El servicio de consulta respondió con un error (" + estado + ").");
        } catch (ResourceAccessException e) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de consulta no responde. Inténtelo más tarde.");
        }
    }

    private static String texto(Map<String, Object> mapa, String clave) {
        Object valor = mapa.get(clave);
        return valor == null ? "" : valor.toString().trim();
    }
}