package com.dataverse.rentals.controller;

import com.dataverse.rentals.dto.OperacionDtos.ProveedorRequest;
import com.dataverse.rentals.dto.OperacionDtos.ProveedorResponse;
import com.dataverse.rentals.entity.Proveedor;
import com.dataverse.rentals.exception.ApiException;
import com.dataverse.rentals.repository.MantenimientoRepository;
import com.dataverse.rentals.repository.ProveedorRepository;
import com.dataverse.rentals.security.RolesPermitidos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/proveedores")
//@CrossOrigin(origins = "http://localhost:4200")
@RolesPermitidos("Administrador")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorRepository proveedores;
    private final MantenimientoRepository mantenimientos;

    private static ProveedorResponse aDto(Proveedor p) {
        return new ProveedorResponse(p.getId(), p.getNombre(), p.getRuc(), p.getCorreo(), p.getTelefono());
    }

    @GetMapping
    public List<ProveedorResponse> listar() {
        return proveedores.findAllByOrderByIdDesc().stream().map(ProveedorController::aDto).toList();
    }

    @GetMapping("/{id}")
    public ProveedorResponse obtener(@PathVariable Integer id) {
        return aDto(buscar(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> crear(@Valid @RequestBody ProveedorRequest r) {
        if (proveedores.existsByRuc(r.ruc().trim())) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un proveedor con ese RUC.");
        }
        Proveedor p = new Proveedor();
        copiar(r, p);
        proveedores.save(p);
        return Map.of("mensaje", "El proveedor se registró correctamente");
    }

    @PutMapping("/{id}")
    @Transactional
    public Map<String, String> actualizar(@PathVariable Integer id, @Valid @RequestBody ProveedorRequest r) {
        Proveedor p = buscar(id);
        if (proveedores.existsByRucAndIdNot(r.ruc().trim(), id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe otro proveedor con ese RUC.");
        }
        copiar(r, p);
        proveedores.save(p);
        return Map.of("mensaje", "El proveedor fue actualizado correctamente");
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Map<String, String> eliminar(@PathVariable Integer id) {
        Proveedor p = buscar(id);
        if (mantenimientos.existsByProveedorId(id)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "No se pudo eliminar el registro (puede estar asociado a una orden técnica).");
        }
        proveedores.delete(p);
        proveedores.flush();
        return Map.of("mensaje", "El proveedor fue eliminado correctamente");
    }

    private Proveedor buscar(Integer id) {
        return proveedores.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Proveedor no encontrado."));
    }

    private static void copiar(ProveedorRequest r, Proveedor p) {
        p.setNombre(r.nombre().trim());
        p.setRuc(r.ruc().trim());
        p.setCorreo(r.correo().trim());
        p.setTelefono(r.telefono().trim());
    }
}