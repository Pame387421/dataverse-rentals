package com.dataverse.rentals.controller;

import com.dataverse.rentals.dto.LecturaDtos.LecturaRequest;
import com.dataverse.rentals.dto.LecturaDtos.LecturaResponse;
import com.dataverse.rentals.entity.Departamento;
import com.dataverse.rentals.entity.LecturaMedidor;
import com.dataverse.rentals.entity.Medidor;
import com.dataverse.rentals.exception.ApiException;
import com.dataverse.rentals.repository.LecturaMedidorRepository;
import com.dataverse.rentals.repository.MedidorRepository;
import com.dataverse.rentals.security.RolesPermitidos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lecturas")
//@CrossOrigin(origins = "http://localhost:4200")
@RolesPermitidos("Administrador")
@RequiredArgsConstructor
public class LecturaMedidorController {

    private final LecturaMedidorRepository lecturas;
    private final MedidorRepository medidores;

    private static LecturaResponse aDto(LecturaMedidor l) {
        Medidor m = l.getMedidor();
        Departamento d = m.getDepartamento();
        return new LecturaResponse(l.getId(), m.getId(), l.getFechaLectura().toLocalDate(), l.getValor(),
                m.getTipo().name(), m.getNumeroSerie(), d.getEdificio().getNombre(), d.getPiso(),
                d.getNumeroDepartamento());
    }

    @GetMapping
    public List<LecturaResponse> listar() {
        return lecturas.findAllByOrderByFechaLecturaDescIdDesc().stream()
                .map(LecturaMedidorController::aDto).toList();
    }

    @GetMapping("/{id}")
    public LecturaResponse obtener(@PathVariable Integer id) {
        return aDto(buscar(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> registrar(@Valid @RequestBody LecturaRequest r) {
        LecturaMedidor l = new LecturaMedidor();
        copiar(r, l);
        lecturas.save(l);
        return Map.of("mensaje", "Lectura registrada correctamente");
    }

    @PutMapping("/{id}")
    @Transactional
    public Map<String, String> actualizar(@PathVariable Integer id, @Valid @RequestBody LecturaRequest r) {
        LecturaMedidor l = buscar(id);
        copiar(r, l);
        lecturas.save(l);
        return Map.of("mensaje", "Lectura actualizada correctamente");
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Map<String, String> eliminar(@PathVariable Integer id) {
        lecturas.delete(buscar(id));
        return Map.of("mensaje", "Lectura eliminada correctamente");
    }

    private LecturaMedidor buscar(Integer id) {
        return lecturas.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Lectura no encontrada."));
    }

    private void copiar(LecturaRequest r, LecturaMedidor l) {
        Medidor m = medidores.findById(r.medidorId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "El medidor seleccionado no existe."));
        l.setMedidor(m);
        l.setFechaLectura(r.fechaLectura().atStartOfDay());
        l.setValor(r.valor());
    }
}