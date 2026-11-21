package com.dataverse.rentals.controller;

import com.dataverse.rentals.dto.BoletaDtos.BoletaPreviaResponse;
import com.dataverse.rentals.dto.BoletaDtos.BoletaRequest;
import com.dataverse.rentals.dto.BoletaDtos.BoletaResponse;
import com.dataverse.rentals.dto.BoletaDtos.DetalleResponse;
import com.dataverse.rentals.entity.Boleta;
import com.dataverse.rentals.entity.Contrato;
import com.dataverse.rentals.entity.Departamento;
import com.dataverse.rentals.entity.Pago;
import com.dataverse.rentals.exception.ApiException;
import com.dataverse.rentals.repository.BoletaRepository;
import com.dataverse.rentals.repository.ContratoRepository;
import com.dataverse.rentals.repository.PagoRepository;
import com.dataverse.rentals.repository.PagoRepository.SumaBoleta;
import com.dataverse.rentals.security.RolesPermitidos;
import com.dataverse.rentals.security.Sesion;
import com.dataverse.rentals.service.AlmacenVouchers;
import com.dataverse.rentals.service.CalculoBoletaService;
import com.dataverse.rentals.service.CalculoBoletaService.Resultado;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/boletas")
//@CrossOrigin(origins = "http://localhost:4200")
@RolesPermitidos("Administrador")
@RequiredArgsConstructor
public class BoletaController {

    private static final int DIAS_VENCIMIENTO = 7;

    private final BoletaRepository boletas;
    private final PagoRepository pagos;
    private final ContratoRepository contratos;
    private final CalculoBoletaService calculo;
    private final AlmacenVouchers vouchers;

    private static String ubicacion(Departamento d) {
        return d.getEdificio().getNombre() + " - Piso " + d.getPiso() + " - Dpto " + d.getNumeroDepartamento();
    }

    private static BoletaResponse aDto(Boleta b, BigDecimal abonado) {
        Contrato c = b.getContrato();
        Departamento d = c.getDepartamento();
        // El alquiler de la boleta es lo que queda del total (así no cambia si el contrato se edita después)
        BigDecimal alquiler = b.getMontoTotal().subtract(b.getConsumoLuz()).subtract(b.getConsumoAgua())
                .subtract(b.getConsumoGas()).subtract(b.getArbitrios());
        List<DetalleResponse> detalles = b.getDetalles().stream()
                .map(x -> new DetalleResponse(x.getConcepto(), x.getMonto())).toList();

        return new BoletaResponse(b.getId(), c.getId(), b.getFechaEmision().toLocalDate(),
                b.getFechaVencimiento().toLocalDate(), alquiler, b.getConsumoLuz(), b.getConsumoAgua(),
                b.getConsumoGas(), b.getArbitrios(), b.getMontoTotal(), b.getEstado().name(),
                b.getMontoTotal().subtract(abonado), detalles, c.getUsuario().getNombre(), ubicacion(d),
                d.getEdificio().getNombre(), d.getPiso(), d.getNumeroDepartamento());
    }

    private Map<Integer, BigDecimal> abonados() {
        Map<Integer, BigDecimal> mapa = new HashMap<>();
        for (SumaBoleta s : pagos.sumarPorBoleta()) mapa.put(s.getBoletaId(), s.getTotal());
        return mapa;
    }

    private List<BoletaResponse> aLista(List<Boleta> lista) {
        Map<Integer, BigDecimal> abonado = abonados();
        return lista.stream().map(b -> aDto(b, abonado.getOrDefault(b.getId(), BigDecimal.ZERO))).toList();
    }

    // GET /api/boletas: todas, de la más reciente a la más antigua
    @GetMapping
    public List<BoletaResponse> listar() {
        return aLista(boletas.findAllByOrderByFechaEmisionDescIdDesc());
    }

    // GET /api/boletas/mias: solo las del inquilino que inició sesión
    @GetMapping("/mias")
    @RolesPermitidos("Inquilino")
    public List<BoletaResponse> mias(@RequestAttribute(Sesion.ATRIBUTO) Sesion sesion) {
        return aLista(boletas.findByContratoUsuarioIdOrderByFechaEmisionDescIdDesc(sesion.usuarioId()));
    }

    // GET /api/boletas/previa?contratoId=3: calcula la boleta sin guardarla
    @GetMapping("/previa")
    @Transactional(readOnly = true)
    public BoletaPreviaResponse previa(@RequestParam Integer contratoId) {
        Contrato c = contrato(contratoId);
        Resultado r = calculo.calcular(c);
        LocalDate hoy = LocalDate.now();
        return new BoletaPreviaResponse(c.getId(), c.getUsuario().getNombre(), ubicacion(c.getDepartamento()),
                hoy, hoy.plusDays(DIAS_VENCIMIENTO), r.alquiler(), r.arbitrios(), r.agua(), r.luz(), r.gas(), r.total());
    }

    // GET /api/boletas/5
    @GetMapping("/{id}")
    @RolesPermitidos({"Administrador", "Inquilino"})
    public BoletaResponse obtener(@PathVariable Integer id, @RequestAttribute(Sesion.ATRIBUTO) Sesion sesion) {
        Boleta b = boletas.buscarCompleta(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Boleta no encontrada."));
        if ("Inquilino".equals(sesion.rol()) && !b.getContrato().getUsuario().getId().equals(sesion.usuarioId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No tienes permiso para ver esta boleta.");
        }
        BigDecimal abonado = pagos.sumaAbonado(id);
        return aDto(b, abonado == null ? BigDecimal.ZERO : abonado);
    }

    // POST /api/boletas  { contratoId, fechaEmision, fechaVencimiento }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Map<String, String> crear(@Valid @RequestBody BoletaRequest r) {
        if (r.fechaVencimiento().isBefore(r.fechaEmision())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "La fecha de vencimiento no puede ser anterior a la fecha de emisión.");
        }
        Contrato c = contrato(r.contratoId());

        // Bloqueo de duplicados: una boleta por contrato y por mes
        LocalDate primero = r.fechaEmision().withDayOfMonth(1);
        if (boletas.existsByContratoIdAndFechaEmisionGreaterThanEqualAndFechaEmisionLessThan(
                c.getId(), primero.atStartOfDay(), primero.plusMonths(1).atStartOfDay())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Ya se encuentra registrada una boleta para el periodo seleccionado en este contrato.");
        }

       Resultado res = calculo.calcular(c);
        String etiqueta = CalculoBoletaService.periodo(r.fechaEmision());

        Boleta b = new Boleta();
        b.setContrato(c);
        b.setFechaEmision(r.fechaEmision().atStartOfDay());
        b.setFechaVencimiento(r.fechaVencimiento().atStartOfDay());
        b.setConsumoLuz(res.luz());
        b.setConsumoAgua(res.agua());
        b.setConsumoGas(res.gas());
        b.setArbitrios(res.arbitrios());
        b.setMontoTotal(res.total());
        calculo.armarDetalles(b, res, etiqueta);
        boletas.save(b);

        return Map.of("mensaje", "Boleta generada con éxito para el periodo de " + etiqueta + ".");
    }

    // DELETE /api/boletas/5: elimina la boleta y sus pagos (como tu delete.jsp)
    @DeleteMapping("/{id}")
    @Transactional
    public Map<String, String> eliminar(@PathVariable Integer id) {
        Boleta b = boletas.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Boleta no encontrada."));

        List<Pago> delPago = pagos.findByBoletaIdOrderByFechaPagoAscIdAsc(id);
        List<String> archivos = delPago.stream().map(Pago::getVoucherPdf).filter(Objects::nonNull).toList();

        try {
            pagos.deleteAll(delPago);
            boletas.delete(b);
            boletas.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "No se puede eliminar la boleta porque está vinculada a otros registros (por ejemplo, mantenimientos).");
        }
        archivos.forEach(vouchers::eliminar);
        return Map.of("mensaje", "La boleta y sus registros asociados fueron removidos del historial de forma permanente.");
    }

    private Contrato contrato(Integer id) {
        return contratos.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Contrato no encontrado."));
    }
}