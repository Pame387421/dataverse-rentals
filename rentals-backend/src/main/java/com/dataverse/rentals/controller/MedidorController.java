package com.dataverse.rentals.controller;

import com.dataverse.rentals.dto.MedidorDtos.MedidorRequest;
import com.dataverse.rentals.dto.MedidorDtos.MedidorResponse;
import com.dataverse.rentals.entity.Departamento;
import com.dataverse.rentals.entity.Medidor;
import com.dataverse.rentals.entity.TipoMedidor;
import com.dataverse.rentals.exception.ApiException;
import com.dataverse.rentals.repository.DepartamentoRepository;
import com.dataverse.rentals.repository.LecturaMedidorRepository;
import com.dataverse.rentals.repository.MedidorRepository;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medidores")
//@CrossOrigin(origins = "http://localhost:4200")
@RolesPermitidos("Administrador")
@RequiredArgsConstructor
public class MedidorController {

    private final MedidorRepository medidores;
    private final DepartamentoRepository departamentos;
    private final LecturaMedidorRepository lecturas;

    private static MedidorResponse aDto(Medidor m) {
        Departamento d = m.getDepartamento();
        return new MedidorResponse(m.getId(), m.getNumeroSerie(), m.getTipo().name(), d.getId(),
                d.getEdificio().getNombre(), d.getPiso(), d.getNumeroDepartamento());
    }

    @GetMapping
    public List<MedidorResponse> listar() {
        return medidores.findAll(Sort.by("id")).stream().map(MedidorController::aDto).toList();
    }

    @GetMapping("/{id}")
    public MedidorResponse obtener(@PathVariable Integer id) {
        return aDto(buscar(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> crear(@Valid @RequestBody MedidorRequest r) {
        Departamento d = departamento(r.departamentoId());
        if (medidores.existsByNumeroSerieIgnoreCase(r.numeroSerie().trim())) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un medidor con ese número de serie.");
        }
        Medidor m = new Medidor();
        copiar(r, m, d);
        medidores.save(m);
        return Map.of("mensaje", "Medidor registrado correctamente");
    }

    @PutMapping("/{id}")
    @Transactional
    public Map<String, String> actualizar(@PathVariable Integer id, @Valid @RequestBody MedidorRequest r) {
        Medidor m = buscar(id);
        Departamento d = departamento(r.departamentoId());
        if (medidores.existsByNumeroSerieIgnoreCaseAndIdNot(r.numeroSerie().trim(), id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe otro medidor con ese número de serie.");
        }
        copiar(r, m, d);
        medidores.save(m);
        return Map.of("mensaje", "Medidor actualizado correctamente");
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Map<String, String> eliminar(@PathVariable Integer id) {
        Medidor m = buscar(id);
        if (lecturas.existsByMedidorId(id)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "No se puede eliminar el medidor porque tiene lecturas registradas.");
        }
        medidores.delete(m);
        medidores.flush();
        return Map.of("mensaje", "Medidor eliminado correctamente");
    }

    private Medidor buscar(Integer id) {
        return medidores.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Medidor no encontrado."));
    }

    private Departamento departamento(Integer id) {
        return departamentos.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "El departamento seleccionado no existe."));
    }

    private static void copiar(MedidorRequest r, Medidor m, Departamento d) {
        m.setNumeroSerie(r.numeroSerie().trim());
        m.setTipo(TipoMedidor.valueOf(r.tipo()));
        m.setDepartamento(d);
    }
}