package com.dataverse.rentals.controller;

import com.dataverse.rentals.dto.EdificioDtos.EdificioRequest;
import com.dataverse.rentals.dto.EdificioDtos.EdificioResponse;
import com.dataverse.rentals.entity.Edificio;
import com.dataverse.rentals.exception.ApiException;
import com.dataverse.rentals.repository.DepartamentoRepository;
import com.dataverse.rentals.repository.DepartamentoRepository.ConteoEdificio;
import com.dataverse.rentals.repository.EdificioRepository;
import com.dataverse.rentals.security.RolesPermitidos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/edificios")
//@CrossOrigin(origins = "http://localhost:4200")
@RolesPermitidos("Administrador")
@RequiredArgsConstructor
public class EdificioController {

    private final EdificioRepository edificios;
    private final DepartamentoRepository departamentos;

    private static EdificioResponse aDto(Edificio e, long cantidad) {
        return new EdificioResponse(e.getId(), e.getNombre(), e.getDireccion(), e.getPisos(), cantidad);
    }

    // GET /api/edificios
    @GetMapping
    public List<EdificioResponse> listar() {
        Map<Integer, Long> conteo = new HashMap<>();
        for (ConteoEdificio c : departamentos.contarPorEdificio()) {
            conteo.put(c.getEdificioId(), c.getTotal());
        }
        return edificios.findAll(Sort.by("id")).stream()
                .map(e -> aDto(e, conteo.getOrDefault(e.getId(), 0L)))
                .toList();
    }

    // GET /api/edificios/5
    @GetMapping("/{id}")
    public EdificioResponse obtener(@PathVariable Integer id) {
        return aDto(buscar(id), departamentos.countByEdificioId(id));
    }

    // POST /api/edificios
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> crear(@Valid @RequestBody EdificioRequest r) {
        Edificio e = new Edificio();
        copiar(r, e);
        edificios.save(e);
        return Map.of("mensaje", "Edificio registrado correctamente");
    }

    // PUT /api/edificios/5
    @PutMapping("/{id}")
    @Transactional
    public Map<String, String> actualizar(@PathVariable Integer id, @Valid @RequestBody EdificioRequest r) {
        Edificio e = buscar(id);
        copiar(r, e);
        edificios.save(e);
        return Map.of("mensaje", "Edificio actualizado correctamente");
    }

    // DELETE /api/edificios/5
    @DeleteMapping("/{id}")
    @Transactional
    public Map<String, String> eliminar(@PathVariable Integer id) {
        Edificio e = buscar(id);
        if (departamentos.existsByEdificioId(id)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "No se puede eliminar el edificio porque tiene departamentos asociados.");
        }
        edificios.delete(e);
        edificios.flush();   // si lo rechaza la base (por ejemplo, tiene mantenimientos), se responde 409
        return Map.of("mensaje", "Edificio eliminado correctamente");
    }

    private Edificio buscar(Integer id) {
        return edificios.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Edificio no encontrado."));
    }

    private static void copiar(EdificioRequest r, Edificio e) {
        e.setNombre(r.nombre().trim());
        e.setDireccion(r.direccion().trim());
        e.setPisos(r.pisos());
    }
}