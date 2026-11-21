package com.dataverse.rentals.controller;

import com.dataverse.rentals.dto.DepartamentoDtos.DepartamentoRequest;
import com.dataverse.rentals.dto.DepartamentoDtos.DepartamentoResponse;
import com.dataverse.rentals.entity.Departamento;
import com.dataverse.rentals.entity.Edificio;
import com.dataverse.rentals.exception.ApiException;
import com.dataverse.rentals.repository.ContratoRepository;
import com.dataverse.rentals.repository.DepartamentoRepository;
import com.dataverse.rentals.repository.EdificioRepository;
import com.dataverse.rentals.security.RolesPermitidos;
import com.dataverse.rentals.security.Sesion;
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
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departamentos")
//@CrossOrigin(origins = "http://localhost:4200")
@RolesPermitidos("Administrador")
@RequiredArgsConstructor
public class DepartamentoController {

    private final DepartamentoRepository departamentos;
    private final EdificioRepository edificios;
    private final ContratoRepository contratos;

    private static DepartamentoResponse aDto(Departamento d) {
        return new DepartamentoResponse(d.getId(), d.getPiso(), d.getNumeroDepartamento(), d.getAreaM2(),
                d.getEstadoAcabados(), d.getEdificio().getId(), d.getEdificio().getNombre());
    }

    private static boolean esInquilino(Sesion sesion) {
        return "Inquilino".equals(sesion.rol());
    }

    // GET /api/departamentos  |  GET /api/departamentos?edificioId=1
    // Administrador: todos. Inquilino: solo los departamentos de sus contratos.
    @GetMapping
    @RolesPermitidos({"Administrador", "Inquilino"})
    public List<DepartamentoResponse> listar(@RequestParam(required = false) Integer edificioId,
                                             @RequestAttribute(Sesion.ATRIBUTO) Sesion sesion) {
        if (esInquilino(sesion)) {
            return departamentos.findDeUsuario(sesion.usuarioId()).stream().map(DepartamentoController::aDto).toList();
        }
        Sort orden = Sort.by("id");
        List<Departamento> lista = edificioId == null
                ? departamentos.findAll(orden)
                : departamentos.findByEdificioId(edificioId, orden);
        return lista.stream().map(DepartamentoController::aDto).toList();
    }

    // GET /api/departamentos/5
    @GetMapping("/{id}")
    @RolesPermitidos({"Administrador", "Inquilino"})
    public DepartamentoResponse obtener(@PathVariable Integer id, @RequestAttribute(Sesion.ATRIBUTO) Sesion sesion) {
        if (esInquilino(sesion) && !contratos.existsByDepartamentoIdAndUsuarioId(id, sesion.usuarioId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No tienes permiso para ver este departamento.");
        }
        return aDto(buscar(id));
    }

    // POST /api/departamentos
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> crear(@Valid @RequestBody DepartamentoRequest r) {
        Edificio edificio = edificio(r.edificioId());
        if (departamentos.existsByEdificioIdAndNumeroDepartamentoIgnoreCase(edificio.getId(), r.numeroDepartamento().trim())) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un departamento con ese número en el edificio.");
        }
        Departamento d = new Departamento();
        copiar(r, d, edificio);
        departamentos.save(d);
        return Map.of("mensaje", "Departamento registrado correctamente");
    }

    // PUT /api/departamentos/5
    @PutMapping("/{id}")
    @Transactional
    public Map<String, String> actualizar(@PathVariable Integer id, @Valid @RequestBody DepartamentoRequest r) {
        Departamento d = buscar(id);
        Edificio edificio = edificio(r.edificioId());
        if (departamentos.existsByEdificioIdAndNumeroDepartamentoIgnoreCaseAndIdNot(
                edificio.getId(), r.numeroDepartamento().trim(), id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe otro departamento con ese número en el edificio.");
        }
        copiar(r, d, edificio);
        departamentos.save(d);
        return Map.of("mensaje", "El departamento fue actualizado correctamente");
    }

    // DELETE /api/departamentos/5
    @DeleteMapping("/{id}")
    @Transactional
    public Map<String, String> eliminar(@PathVariable Integer id) {
        Departamento d = buscar(id);
        if (departamentos.contarContratos(id) > 0) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "No se puede eliminar el departamento porque tiene contratos asociados.");
        }
        if (departamentos.contarOtrasRelaciones(id) > 0) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "No se puede eliminar el departamento porque tiene medidores, tickets, mantenimientos o pagos asociados.");
        }
        departamentos.delete(d);
        departamentos.flush();
        return Map.of("mensaje", "El departamento fue eliminado correctamente");
    }

    private Departamento buscar(Integer id) {
        return departamentos.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Departamento no encontrado."));
    }

    private Edificio edificio(Integer id) {
        return edificios.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "El edificio seleccionado no existe."));
    }

    private static void copiar(DepartamentoRequest r, Departamento d, Edificio edificio) {
        d.setPiso(r.piso());
        d.setNumeroDepartamento(r.numeroDepartamento().trim());
        d.setAreaM2(r.areaM2());
        d.setEstadoAcabados(r.estadoAcabados());
        d.setEdificio(edificio);
    }
}