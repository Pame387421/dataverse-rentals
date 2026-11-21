package com.dataverse.rentals.controller;

import com.dataverse.rentals.dto.UsuarioDtos.TipoUsuarioResponse;
import com.dataverse.rentals.repository.TipoUsuarioRepository;
import com.dataverse.rentals.security.RolesPermitidos;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
//import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-usuario")
//@CrossOrigin(origins = "http://localhost:4200")
@RolesPermitidos("Administrador")
@RequiredArgsConstructor
public class TipoUsuarioController {

    private final TipoUsuarioRepository tipos;

    /** Alimenta el desplegable de roles del formulario de usuarios. */
    @GetMapping
    public List<TipoUsuarioResponse> listar() {
        return tipos.findAll(Sort.by("id")).stream()
                .map(t -> new TipoUsuarioResponse(t.getId(), t.getNombre(), t.getDescripcion()))
                .toList();
    }
}