package com.dataverse.rentals.controller;

import com.dataverse.rentals.dto.OperacionDtos.SoporteRequest;
import com.dataverse.rentals.dto.OperacionDtos.SoporteResponse;
import com.dataverse.rentals.entity.Departamento;
import com.dataverse.rentals.entity.Mantenimiento;
import com.dataverse.rentals.entity.Soporte;
import com.dataverse.rentals.entity.Usuario;
import com.dataverse.rentals.exception.ApiException;
import com.dataverse.rentals.repository.ContratoRepository;
import com.dataverse.rentals.repository.DepartamentoRepository;
import com.dataverse.rentals.repository.MantenimientoRepository;
import com.dataverse.rentals.repository.SoporteRepository;
import com.dataverse.rentals.repository.UsuarioRepository;
import com.dataverse.rentals.security.RolesPermitidos;
import com.dataverse.rentals.security.Sesion;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/soportes")
//@CrossOrigin(origins = "http://localhost:4200")
@RolesPermitidos("Administrador")
@RequiredArgsConstructor
public class SoporteController {

    private final SoporteRepository soportes;
    private final DepartamentoRepository departamentos;
    private final UsuarioRepository usuarios;
    private final MantenimientoRepository mantenimientos;
    private final ContratoRepository contratos;

    static String recortar(String texto, int limite, int corte) {
        String t = texto == null ? "" : texto;
        return t.length() > limite ? t.substring(0, corte) + "..." : t;
    }

    private static SoporteResponse aDto(Soporte s) {
        Departamento d = s.getDepartamento();
        Mantenimiento m = s.getMantenimiento();
        return new SoporteResponse(s.getId(), s.getDescripcion(), s.getFechaRegistro(), s.getFechaCierre(),
                s.getEstado(), s.getPrioridad(), s.getObservaciones(), d.getId(), s.getUsuario().getId(),
                m != null ? m.getId() : null,
                d.getEdificio().getNombre() + " - Piso " + d.getPiso() + " - Dpto " + d.getNumeroDepartamento(),
                s.getUsuario().getNombre(),
                m != null ? "Orden #" + m.getId() + " - " + recortar(m.getDescripcion(), 40, 37) : null);
    }

    // GET /api/soportes: todos, del más reciente al más antiguo
    @GetMapping
    public List<SoporteResponse> listar() {
        return soportes.findAllByOrderByFechaRegistroDescIdDesc().stream().map(SoporteController::aDto).toList();
    }

    // GET /api/soportes/mios: solo los tickets del inquilino que inició sesión
    @GetMapping("/mios")
    @RolesPermitidos("Inquilino")
    public List<SoporteResponse> mios(@RequestAttribute(Sesion.ATRIBUTO) Sesion sesion) {
        return soportes.findByUsuarioIdOrderByFechaRegistroDescIdDesc(sesion.usuarioId()).stream()
                .map(SoporteController::aDto).toList();
    }

    @GetMapping("/{id}")
    public SoporteResponse obtener(@PathVariable Integer id) {
        return aDto(buscar(id));
    }

    // POST /api/soportes: siempre nace Pendiente y sin orden vinculada
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RolesPermitidos({"Administrador", "Inquilino"})
    @Transactional
    public Map<String, String> crear(@Valid @RequestBody SoporteRequest r, @RequestAttribute(Sesion.ATRIBUTO) Sesion sesion) {
        boolean inquilino = "Inquilino".equals(sesion.rol());
        Integer usuarioId = inquilino ? sesion.usuarioId() : r.usuarioId();
        if (usuarioId == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Seleccione un inquilino.");
        if (inquilino && !contratos.existsByDepartamentoIdAndUsuarioId(r.departamentoId(), usuarioId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Solo puedes reportar tickets en los departamentos de tus contratos.");
        }

        Soporte s = new Soporte();
        s.setDescripcion(r.descripcion().trim());
        s.setPrioridad(r.prioridad());
        s.setObservaciones(r.observaciones() == null ? "" : r.observaciones().trim());
        s.setDepartamento(departamento(r.departamentoId()));
        s.setUsuario(usuario(usuarioId));
        s.setEstado("PENDIENTE");
        s.setFechaRegistro(LocalDateTime.now());
        soportes.save(s);
        return Map.of("mensaje", "Ticket de soporte registrado correctamente.");
    }

    // PUT /api/soportes/5: maneja la fecha de cierre y la orden vinculada
    @PutMapping("/{id}")
    @Transactional
    public Map<String, String> actualizar(@PathVariable Integer id, @Valid @RequestBody SoporteRequest r) {
        Soporte s = buscar(id);
        if (r.usuarioId() == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Seleccione un inquilino.");

        s.setDescripcion(r.descripcion().trim());
        s.setPrioridad(r.prioridad());
        s.setObservaciones(r.observaciones() == null ? "" : r.observaciones().trim());
        s.setDepartamento(departamento(r.departamentoId()));
        s.setUsuario(usuario(r.usuarioId()));
        if (r.estado() != null) s.setEstado(r.estado());
        s.setMantenimiento(r.mantenimientoId() == null ? null : mantenimientos.findById(r.mantenimientoId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "La orden de mantenimiento seleccionada no existe.")));

        // La fecha de cierre se pone al resolver y se borra al reabrir
        if ("RESUELTO".equals(s.getEstado())) {
            if (s.getFechaCierre() == null) s.setFechaCierre(LocalDateTime.now());
        } else {
            s.setFechaCierre(null);
        }
        soportes.save(s);
        return Map.of("mensaje", "Ticket #" + id + " actualizado con éxito.");
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Map<String, String> eliminar(@PathVariable Integer id) {
        soportes.delete(buscar(id));
        return Map.of("mensaje", "El ticket fue eliminado correctamente.");
    }

    private Soporte buscar(Integer id) {
        return soportes.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Ticket no encontrado."));
    }

    private Departamento departamento(Integer id) {
        return departamentos.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "El departamento seleccionado no existe."));
    }

    private Usuario usuario(Integer id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "El inquilino seleccionado no existe."));
    }
}