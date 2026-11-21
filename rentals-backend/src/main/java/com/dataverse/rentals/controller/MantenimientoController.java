package com.dataverse.rentals.controller;

import com.dataverse.rentals.dto.OperacionDtos.MantenimientoRequest;
import com.dataverse.rentals.dto.OperacionDtos.MantenimientoResponse;
import com.dataverse.rentals.entity.Departamento;
import com.dataverse.rentals.entity.Mantenimiento;
import com.dataverse.rentals.entity.Proveedor;
import com.dataverse.rentals.entity.Soporte;
import com.dataverse.rentals.exception.ApiException;
import com.dataverse.rentals.repository.DepartamentoRepository;
import com.dataverse.rentals.repository.MantenimientoRepository;
import com.dataverse.rentals.repository.ProveedorRepository;
import com.dataverse.rentals.repository.SoporteRepository;
import com.dataverse.rentals.security.RolesPermitidos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mantenimientos")
//@CrossOrigin(origins = "http://localhost:4200")
@RolesPermitidos("Administrador")
@RequiredArgsConstructor
public class MantenimientoController {

    private final MantenimientoRepository mantenimientos;
    private final DepartamentoRepository departamentos;
    private final ProveedorRepository proveedores;
    private final SoporteRepository soportes;

    /** El ticket de una orden se deduce del ticket que apunta a ella (como en tu base). */
    private static MantenimientoResponse aDto(Mantenimiento m, Soporte ticket) {
        Departamento d = m.getDepartamento();
        return new MantenimientoResponse(m.getId(), m.getDescripcion(), m.getCategoria(), m.getCostoMateriales(),
                m.getCostoManoObra(), m.getCostoMateriales().add(m.getCostoManoObra()),
                m.getFechaRegistro().toLocalDate(),
                m.getFechaEjecucion() != null ? m.getFechaEjecucion().toLocalDate() : null,
                m.getFechaFinalizacion() != null ? m.getFechaFinalizacion().toLocalDate() : null,
                m.getEstado(), m.getObservaciones(), d.getId(),
                m.getEdificio() != null ? m.getEdificio().getId() : null,
                m.getBoleta() != null ? m.getBoleta().getId() : null,
                m.getProveedor().getId(),
                ticket != null ? ticket.getId() : null,
                d.getEdificio().getNombre() + " - Piso " + d.getPiso() + " - Dpto " + d.getNumeroDepartamento(),
                m.getProveedor().getNombre(),
                ticket != null ? "Ticket #" + ticket.getId() + " - " + SoporteController.recortar(ticket.getDescripcion(), 25, 25) : null);
    }

    private Map<Integer, Soporte> ticketsPorOrden() {
        Map<Integer, Soporte> mapa = new HashMap<>();
        for (Soporte s : soportes.findByMantenimientoIdIsNotNull()) mapa.put(s.getMantenimiento().getId(), s);
        return mapa;
    }

    // GET /api/mantenimientos: de la más reciente a la más antigua
    @GetMapping
    public List<MantenimientoResponse> listar() {
        Map<Integer, Soporte> tickets = ticketsPorOrden();
        return mantenimientos.findAllByOrderByFechaRegistroDescIdDesc().stream()
                .map(m -> aDto(m, tickets.get(m.getId()))).toList();
    }

    @GetMapping("/{id}")
    public MantenimientoResponse obtener(@PathVariable Integer id) {
        return aDto(buscar(id), ticketsPorOrden().get(id));
    }

    // POST /api/mantenimientos: el edificio sale del departamento y el ticket pasa a EN_PROCESO
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Map<String, String> crear(@Valid @RequestBody MantenimientoRequest r) {
        Departamento d = departamento(r.departamentoId());
        Proveedor p = proveedor(r.proveedorId());

        Soporte ticket = null;
        if (r.ticketSoporteId() != null) {
            ticket = soportes.findById(r.ticketSoporteId())
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "El ticket seleccionado no existe."));
            if ("RESUELTO".equals(ticket.getEstado())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "El ticket seleccionado ya está resuelto.");
            }
            if (ticket.getMantenimiento() != null) {
                throw new ApiException(HttpStatus.CONFLICT, "El ticket seleccionado ya está vinculado a otra orden.");
            }
            if (!ticket.getDepartamento().getId().equals(d.getId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "El ticket seleccionado pertenece a otro departamento.");
            }
        }

        Mantenimiento m = new Mantenimiento();
        m.setFechaRegistro(LocalDateTime.now());
        copiar(r, m, d, p);
        Mantenimiento guardado = mantenimientos.save(m);

        if (ticket != null) {
            ticket.setMantenimiento(guardado);
            ticket.setEstado("EN_PROCESO");
            soportes.save(ticket);
        }
        return Map.of("mensaje", "Orden de mantenimiento #" + guardado.getId() + " generada exitosamente.");
    }

    // PUT /api/mantenimientos/5: conserva la fecha de registro, la boleta y el ticket
    @PutMapping("/{id}")
    @Transactional
    public Map<String, String> actualizar(@PathVariable Integer id, @Valid @RequestBody MantenimientoRequest r) {
        Mantenimiento m = buscar(id);
        copiar(r, m, departamento(r.departamentoId()), proveedor(r.proveedorId()));
        mantenimientos.save(m);
        return Map.of("mensaje", "Orden #" + id + " actualizada correctamente.");
    }

    // DELETE /api/mantenimientos/5: sus tickets quedan sin orden vinculada
    @DeleteMapping("/{id}")
    @Transactional
    public Map<String, String> eliminar(@PathVariable Integer id) {
        Mantenimiento m = buscar(id);
        soportes.desvincularDeMantenimiento(id);
        mantenimientos.delete(m);
        mantenimientos.flush();
        return Map.of("mensaje", "La orden de mantenimiento fue eliminada correctamente.");
    }

    private Mantenimiento buscar(Integer id) {
        return mantenimientos.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Orden de mantenimiento no encontrada."));
    }

    private Departamento departamento(Integer id) {
        return departamentos.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "El departamento seleccionado no existe."));
    }

    private Proveedor proveedor(Integer id) {
        return proveedores.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "El proveedor seleccionado no existe."));
    }

    private static void copiar(MantenimientoRequest r, Mantenimiento m, Departamento d, Proveedor p) {
        m.setDescripcion(r.descripcion().trim());
        m.setCategoria(r.categoria());
        m.setCostoMateriales(r.costoMateriales());
        m.setCostoManoObra(r.costoManoObra());
        m.setEstado(r.estado());
        m.setObservaciones(r.observaciones() == null ? null : r.observaciones().trim());
        m.setDepartamento(d);
        m.setEdificio(d.getEdificio());          // el edificio se deduce del departamento
        m.setProveedor(p);
        m.setFechaEjecucion(r.fechaEjecucion() == null ? null : r.fechaEjecucion().atStartOfDay());

        LocalDate fin = r.fechaFinalizacion();
        // Al finalizar sin fecha se registra la de hoy; si el usuario escribió una, se respeta
        if ("FINALIZADO".equals(r.estado()) && fin == null) fin = LocalDate.now();
        m.setFechaFinalizacion(fin == null ? null : fin.atStartOfDay());
    }
}   