package com.dataverse.rentals.controller;

import com.dataverse.rentals.dto.UsuarioDtos.Creado;
import com.dataverse.rentals.dto.UsuarioDtos.PerfilRequest;
import com.dataverse.rentals.dto.UsuarioDtos.UsuarioRequest;
import com.dataverse.rentals.dto.UsuarioDtos.UsuarioResponse;
import com.dataverse.rentals.entity.TipoUsuario;
import com.dataverse.rentals.entity.Usuario;
import com.dataverse.rentals.exception.ApiException;
import com.dataverse.rentals.repository.CuentaRepository;
import com.dataverse.rentals.repository.EncuestaRepository;
import com.dataverse.rentals.repository.TipoUsuarioRepository;
import com.dataverse.rentals.repository.UsuarioRepository;
import com.dataverse.rentals.security.RolesPermitidos;
import com.dataverse.rentals.security.Sesion;
import com.dataverse.rentals.service.AlmacenContratos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.multipart.MultipartFile;
import com.dataverse.rentals.repository.ContratoRepository;
import com.dataverse.rentals.repository.SoporteRepository;
import com.dataverse.rentals.entity.Cuenta;
import com.dataverse.rentals.service.CuentaService;
import com.dataverse.rentals.service.CuentaService.ClaveGenerada;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
// @CrossOrigin(origins = "http://localhost:4200")
@RolesPermitidos("Administrador")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarios;
    private final TipoUsuarioRepository tipos;
    private final CuentaRepository cuentas;
    private final EncuestaRepository encuestas;
    private final AlmacenContratos almacen;
    private final ContratoRepository contratos;
    private final SoporteRepository soportes;
    private final CuentaService cuentaService;

    static UsuarioResponse aDto(Usuario u, Cuenta c) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getTipoDocumento(), u.getNumeroDocumento(),
                u.getCorreo(), u.getUrlVoucher(), u.getContratoPdf(),
                u.getTipoUsuario().getId(), u.getTipoUsuario().getNombre(),
                c != null, c != null && c.isMfaActivo(), c != null && c.isDebeCambiarClave());
    }

    // ---------------------------------------------------------------- Mi perfil
    // (Administrador e Inquilino)

    /** GET /api/usuarios/me: el perfil del usuario que inició sesión. */
    @GetMapping("/me")
    @RolesPermitidos({ "Administrador", "Inquilino" })
    public UsuarioResponse miPerfil(@RequestAttribute(Sesion.ATRIBUTO) Sesion sesion) {
        Usuario u = buscarPropio(sesion);
        return aDto(u, cuentas.findByUsuarioId(u.getId()).orElse(null));
    }

    /**
     * PUT /api/usuarios/me: solo nombre y documento (no el correo, el rol ni el
     * contrato).
     */
    @PutMapping("/me")
    @RolesPermitidos({ "Administrador", "Inquilino" })
    @Transactional
    public Map<String, String> actualizarPerfil(@Valid @RequestBody PerfilRequest r,
            @RequestAttribute(Sesion.ATRIBUTO) Sesion sesion) {
        Usuario u = buscarPropio(sesion);
        String documento = r.numeroDocumento().trim();
        if (usuarios.existsByTipoDocumentoAndNumeroDocumentoAndIdNot(r.tipoDocumento(), documento, u.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe otro usuario con ese documento.");
        }

        u.setNombre(r.nombre().trim());
        u.setTipoDocumento(r.tipoDocumento());
        u.setNumeroDocumento(documento);
        usuarios.save(u);

        cuentas.findByUsuarioId(u.getId()).ifPresent(c -> {
            c.setNombreCompleto(u.getNombre());
            cuentas.save(c);
        });
        return Map.of("mensaje", "Perfil actualizado correctamente");
    }

    // ---------------------------------------------------------------- Contrato en
    // PDF

    /**
     * POST /api/usuarios/5/contrato (multipart, campo "archivo"): sube o reemplaza
     * el contrato.
     */
    @PostMapping(path = "/{id}/contrato", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public Map<String, String> subirContrato(@PathVariable Integer id, @RequestParam("archivo") MultipartFile archivo) {
        Usuario u = buscar(id);
        String anterior = u.getContratoPdf();

        u.setContratoPdf(almacen.guardar(archivo));
        usuarios.save(u);
        almacen.eliminar(anterior);
        return Map.of("mensaje", "Contrato cargado correctamente");
    }

    /**
     * GET /api/usuarios/5/contrato: el Administrador abre cualquiera; un Inquilino,
     * solo el suyo.
     */
    @GetMapping("/{id}/contrato")
    @RolesPermitidos({ "Administrador", "Inquilino" })
    public ResponseEntity<Resource> verContrato(@PathVariable Integer id,
            @RequestAttribute(Sesion.ATRIBUTO) Sesion sesion) {
        boolean inquilino = "Inquilino".equals(sesion.rol());
        if (inquilino && !id.equals(sesion.usuarioId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No tienes permiso para ver este contrato.");
        }

        Usuario u = buscar(id);
        if (!tieneContrato(u)) {
            throw new ApiException(HttpStatus.NOT_FOUND, inquilino
                    ? "Tu contrato digital aun se encuentra en proceso de revision por parte de la administracion."
                    : "Este usuario aún no tiene un contrato cargado.");
        }

        Resource recurso = almacen.cargar(u.getContratoPdf());
        if (recurso == null) {
            throw new ApiException(HttpStatus.NOT_FOUND,
                    "El archivo físico del contrato no está disponible en el servidor. Contacte a soporte de Dataverse Rentals.");
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"contrato.pdf\"")
                .header("X-Content-Type-Options", "nosniff")
                .body(recurso);
    }

    // ----------------------------------------------------------------
    // Administración de usuarios

    @GetMapping
    public List<UsuarioResponse> listar() {
        Map<Integer, Cuenta> porUsuario = new HashMap<>();
        for (Cuenta c : cuentas.findAll()) {
            if (c.getUsuario() != null)
                porUsuario.put(c.getUsuario().getId(), c);
        }
        return usuarios.findAll(Sort.by("id")).stream()
                .map(u -> aDto(u, porUsuario.get(u.getId()))).toList();
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Integer id) {
        return aDto(buscar(id), cuentas.findByUsuarioId(id).orElse(null));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Creado crear(@Valid @RequestBody UsuarioRequest r) {
        TipoUsuario tipo = tipo(r.tipoUsuarioId());
        if (usuarios.existsByCorreoIgnoreCase(r.correo().trim())) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo.");
        }
        if (usuarios.existsByTipoDocumentoAndNumeroDocumento(r.tipoDocumento(), r.numeroDocumento().trim())) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un usuario con ese documento.");
        }

        Usuario u = new Usuario();
        copiar(r, u, tipo);
        Usuario guardado = usuarios.save(u);
        return new Creado("Usuario creado correctamente", guardado.getId());
    }

    @PutMapping("/{id}")
    @Transactional
    public Map<String, String> actualizar(@PathVariable Integer id, @Valid @RequestBody UsuarioRequest r) {
        Usuario u = buscar(id);
        TipoUsuario tipo = tipo(r.tipoUsuarioId());
        if (usuarios.existsByCorreoIgnoreCaseAndIdNot(r.correo().trim(), id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe otro usuario con ese correo.");
        }
        if (usuarios.existsByTipoDocumentoAndNumeroDocumentoAndIdNot(r.tipoDocumento(), r.numeroDocumento().trim(),
                id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe otro usuario con ese documento.");
        }

        copiar(r, u, tipo); // el contrato PDF no se toca aquí
        usuarios.save(u);
        // La cuenta de acceso sigue al perfil: correo, nombre y rol. Sin rol válido,
        // queda inactiva.
        cuentas.findByUsuarioId(id).ifPresent(c -> {
            String rol = u.getTipoUsuario().getNombre();
            boolean conAcceso = CuentaService.ROLES_CON_ACCESO.contains(rol);
            c.setCorreo(u.getCorreo());
            c.setNombreCompleto(u.getNombre());
            if (conAcceso)
                c.setRol(rol);
            c.setActiva(conAcceso);
            cuentas.save(c);
        });
        return Map.of("mensaje", "Usuario actualizado correctamente");
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Map<String, String> eliminar(@PathVariable Integer id, @RequestAttribute(Sesion.ATRIBUTO) Sesion sesion) {
        Usuario u = buscar(id);
        if (contratos.existsByUsuarioId(id) || soportes.existsByUsuarioId(id) || encuestas.existsByUsuarioId(id)) {
            throw new ApiException(HttpStatus.CONFLICT, "No puedes eliminar tu propio usuario.");
        }
        if (encuestas.existsByUsuarioId(id)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "No se puede eliminar el usuario porque tiene contratos, tickets o encuestas asociados.");
        }

        String contrato = u.getContratoPdf();
        cuentas.findByUsuarioId(id).ifPresent(cuentas::delete);
        usuarios.delete(u);
        usuarios.flush(); // si tiene contratos o tickets, la base lo rechaza aquí y se responde 409
        almacen.eliminar(contrato);
        return Map.of("mensaje", "Usuario eliminado correctamente");
    }

        /** POST /api/usuarios/5/cuenta: crea o reinicia el acceso con una contraseña temporal (se muestra una sola vez). */
    @PostMapping("/{id}/cuenta")
    public ResponseEntity<ClaveGenerada> generarAcceso(@PathVariable Integer id,
                                                      @RequestParam(defaultValue = "false") boolean reiniciarMfa,
                                                      @RequestAttribute(Sesion.ATRIBUTO) Sesion sesion) {
        if (id.equals(sesion.usuarioId())) {
            throw new ApiException(HttpStatus.CONFLICT, "Para tu propia cuenta usa «Contraseña» en la barra superior.");
        }
        ClaveGenerada r = cuentaService.generarAcceso(buscar(id), reiniciarMfa);
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store").body(r);
    }

    // ---------------------------------------------------------------- Apoyo

    private Usuario buscar(Integer id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
    }

    private Usuario buscarPropio(Sesion sesion) {
        if (sesion.usuarioId() == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Tu perfil de negocio no está vinculado a la cuenta.");
        }
        return buscar(sesion.usuarioId());
    }

    private TipoUsuario tipo(Integer id) {
        return tipos.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "El rol seleccionado no existe."));
    }

    /** Misma regla de Angular: ruta no vacía y distinta de "pendiente.pdf". */
    private static boolean tieneContrato(Usuario u) {
        String ruta = u.getContratoPdf() == null ? "" : u.getContratoPdf().trim();
        return !ruta.isEmpty() && !ruta.equalsIgnoreCase("pendiente.pdf");
    }

    private static void copiar(UsuarioRequest r, Usuario u, TipoUsuario tipo) {
        u.setNombre(r.nombre().trim());
        u.setCorreo(r.correo().trim());
        u.setTipoDocumento(r.tipoDocumento());
        u.setNumeroDocumento(r.numeroDocumento().trim());
        u.setUrlVoucher(r.urlVoucher() == null || r.urlVoucher().isBlank() ? null : r.urlVoucher().trim());
        u.setTipoUsuario(tipo);
    }
}