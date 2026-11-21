package com.dataverse.rentals.controller;

import com.dataverse.rentals.dto.EncuestaDtos.EncuestaRequest;
import com.dataverse.rentals.dto.EncuestaDtos.EncuestaResponse;
import com.dataverse.rentals.entity.Encuesta;
import com.dataverse.rentals.entity.Usuario;
import com.dataverse.rentals.exception.ApiException;
import com.dataverse.rentals.repository.EncuestaRepository;
import com.dataverse.rentals.repository.UsuarioRepository;
import com.dataverse.rentals.security.RolesPermitidos;
import com.dataverse.rentals.security.Sesion;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/encuestas")
//@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class EncuestaController {

    private final EncuestaRepository encuestas;
    private final UsuarioRepository usuarios;

    private static EncuestaResponse aDto(Encuesta e) {
        return new EncuestaResponse(e.getId(), e.getUsuario().getId(), e.getCalificacion(),
                e.getComentario(), e.getFechaRegistro(), e.getUsuario().getNombre());
    }

    /** GET /api/encuestas: todas, de la más reciente a la más antigua (Administrador). */
    @GetMapping
    @RolesPermitidos("Administrador")
    public List<EncuestaResponse> listar() {
        return encuestas.findAllByOrderByFechaRegistroDescIdDesc().stream().map(EncuestaController::aDto).toList();
    }

    /** GET /api/encuestas/mias: solo las del inquilino que inició sesión. */
    @GetMapping("/mias")
    @RolesPermitidos("Inquilino")
    public List<EncuestaResponse> mias(@RequestAttribute(Sesion.ATRIBUTO) Sesion sesion) {
        return encuestas.findByUsuarioIdOrderByFechaRegistroDescIdDesc(sesion.usuarioId()).stream()
                .map(EncuestaController::aDto).toList();
    }

    /** POST /api/encuestas: el inquilino responde por sí mismo; el administrador elige el usuario. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RolesPermitidos({"Administrador", "Inquilino"})
    public Map<String, String> registrar(@Valid @RequestBody EncuestaRequest r,
                                         @RequestAttribute(Sesion.ATRIBUTO) Sesion sesion) {
        Integer usuarioId = "Inquilino".equals(sesion.rol()) ? sesion.usuarioId() : r.usuarioId();
        if (usuarioId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Seleccione un usuario.");
        }
        Usuario usuario = usuarios.findById(usuarioId)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "El usuario seleccionado no existe."));

        Encuesta e = new Encuesta();
        e.setUsuario(usuario);
        e.setCalificacion(r.calificacion());
        e.setComentario(r.comentario().trim());
        e.setFechaRegistro(LocalDateTime.now());
        encuestas.save(e);
        return Map.of("mensaje", "Encuesta se registró correctamente");
    }
}