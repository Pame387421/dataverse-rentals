package com.dataverse.rentals.controller;

import com.dataverse.rentals.dto.ContratoDtos.ContratoRequest;
import com.dataverse.rentals.dto.ContratoDtos.ContratoResponse;
import com.dataverse.rentals.entity.Contrato;
import com.dataverse.rentals.entity.Departamento;
import com.dataverse.rentals.entity.Usuario;
import com.dataverse.rentals.exception.ApiException;
import com.dataverse.rentals.repository.ContratoRepository;
import com.dataverse.rentals.repository.DepartamentoRepository;
import com.dataverse.rentals.repository.UsuarioRepository;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contratos")
//@CrossOrigin(origins = "http://localhost:4200")
@RolesPermitidos("Administrador")
@RequiredArgsConstructor
public class ContratoController {

    private final ContratoRepository contratos;
    private final DepartamentoRepository departamentos;
    private final UsuarioRepository usuarios;

    private static ContratoResponse aDto(Contrato c) {
        Departamento d = c.getDepartamento();
        String ubicacion = d.getEdificio().getNombre() + " - Piso " + d.getPiso() + " - Dpto " + d.getNumeroDepartamento();
        return new ContratoResponse(c.getId(), d.getId(), c.getUsuario().getId(), c.getFechaInicioContrato(),
                c.getFechaFinContrato(), c.getMontoAlquiler(), c.getUrlVoucher(), ubicacion,
                c.getUsuario().getNombre());
    }

    @GetMapping
    public List<ContratoResponse> listar() {
        return contratos.findAll(Sort.by("id")).stream().map(ContratoController::aDto).toList();
    }

    /** GET /api/contratos/mios: solo los contratos del inquilino que inició sesión. */
    @GetMapping("/mios")
    @RolesPermitidos("Inquilino")
    public List<ContratoResponse> mios(@RequestAttribute(Sesion.ATRIBUTO) Sesion sesion) {
        return contratos.findByUsuarioIdOrderByIdAsc(sesion.usuarioId()).stream()
                .map(ContratoController::aDto).toList();
    }

    @GetMapping("/{id}")
    public ContratoResponse obtener(@PathVariable Integer id) {
        return aDto(buscar(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> crear(@Valid @RequestBody ContratoRequest r) {
        Contrato c = new Contrato();
        copiar(r, c);
        contratos.save(c);
        return Map.of("mensaje", "Contrato creado correctamente.");
    }

    @PutMapping("/{id}")
    @Transactional
    public Map<String, String> actualizar(@PathVariable Integer id, @Valid @RequestBody ContratoRequest r) {
        Contrato c = buscar(id);
        copiar(r, c);
        contratos.save(c);
        return Map.of("mensaje", "Contrato actualizado correctamente.");
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Map<String, String> eliminar(@PathVariable Integer id) {
        Contrato c = buscar(id);
        if (contratos.contarBoletasYPagos(id) > 0) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "No se puede eliminar el contrato porque tiene boletas o pagos asociados.");
        }
        contratos.delete(c);
        contratos.flush();
        return Map.of("mensaje", "Contrato eliminado correctamente.");
    }

    private Contrato buscar(Integer id) {
        return contratos.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Contrato no encontrado."));
    }

    private void copiar(ContratoRequest r, Contrato c) {
        if (r.fechaFinContrato().isBefore(r.fechaInicioContrato())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "La fecha de finalización no puede ser anterior a la de inicio.");
        }
        Departamento d = departamentos.findById(r.departamentoId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "El departamento seleccionado no existe."));
        Usuario u = usuarios.findById(r.usuarioId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "El inquilino seleccionado no existe."));

        c.setDepartamento(d);
        c.setUsuario(u);
        c.setFechaInicioContrato(r.fechaInicioContrato());
        c.setFechaFinContrato(r.fechaFinContrato());
        c.setMontoAlquiler(r.montoAlquiler());
        c.setUrlVoucher(r.urlVoucher() == null || r.urlVoucher().isBlank() ? null : r.urlVoucher().trim());
    }
}