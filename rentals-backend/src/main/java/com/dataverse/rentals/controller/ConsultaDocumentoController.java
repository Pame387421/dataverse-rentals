package com.dataverse.rentals.controller;

import com.dataverse.rentals.client.ConsultaDocumentoClient;
import com.dataverse.rentals.dto.ConsultaDtos.DocumentoConsultado;
import com.dataverse.rentals.security.RolesPermitidos;
import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/consultas")
//@CrossOrigin(origins = "http://localhost:4200")
@RolesPermitidos("Administrador")
@RequiredArgsConstructor
public class ConsultaDocumentoController {

    private final ConsultaDocumentoClient cliente;

    /** GET /api/consultas/documento?tipo=DNI&numero=12345678 */
    @GetMapping("/documento")
    public DocumentoConsultado documento(@RequestParam String tipo, @RequestParam String numero) {
        return cliente.consultar(tipo, numero);
    }
}