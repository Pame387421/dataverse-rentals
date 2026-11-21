package com.dataverse.rentals.controller;

import com.dataverse.rentals.dto.PagoDtos.PagoResponse;
import com.dataverse.rentals.entity.Boleta;
import com.dataverse.rentals.entity.Departamento;
import com.dataverse.rentals.entity.EstadoBoleta;
import com.dataverse.rentals.entity.Pago;
import com.dataverse.rentals.exception.ApiException;
import com.dataverse.rentals.repository.BoletaRepository;
import com.dataverse.rentals.repository.PagoRepository;
import com.dataverse.rentals.security.RolesPermitidos;
import com.dataverse.rentals.security.Sesion;
import com.dataverse.rentals.service.AlmacenVouchers;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
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
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
//@CrossOrigin(origins = "http://localhost:4200")
@RolesPermitidos("Administrador")
@RequiredArgsConstructor
public class PagoController {

    private final PagoRepository pagos;
    private final BoletaRepository boletas;
    private final AlmacenVouchers vouchers;

    private static PagoResponse aDto(Pago p) {
        Boleta b = p.getBoleta();
        Departamento d = p.getDepartamento();
        BigDecimal alquiler = b.getMontoTotal().subtract(b.getConsumoLuz()).subtract(b.getConsumoAgua())
                .subtract(b.getConsumoGas()).subtract(b.getArbitrios());
        String ubicacion = d.getEdificio().getNombre() + " - Piso " + d.getPiso() + " - Dpto " + d.getNumeroDepartamento();
        boolean conPdf = p.getVoucherPdf() != null && !p.getVoucherPdf().isBlank();

        return new PagoResponse(p.getId(), b.getId(), p.getContrato().getUsuario().getNombre(), ubicacion,
                p.getFechaPago(), alquiler, p.getConsumoLuz(), p.getConsumoAgua(), p.getConsumoGas(),
                p.getArbitrios(), p.getMontoTotal(), p.getMontoAbonado(), p.getSaldoPendiente(),
                conPdf, p.getUrlVoucher());
    }

    private static boolean esInquilino(Sesion sesion) {
        return "Inquilino".equals(sesion.rol());
    }

    private static void exigirDueno(Pago p, Sesion sesion) {
        if (esInquilino(sesion) && !p.getContrato().getUsuario().getId().equals(sesion.usuarioId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No tienes permiso para ver este pago.");
        }
    }

    // GET /api/pagos: todos, del más reciente al más antiguo
    @GetMapping
    public List<PagoResponse> listar() {
        return pagos.findAllByOrderByFechaPagoDescIdDesc().stream().map(PagoController::aDto).toList();
    }

    // GET /api/pagos/mios: solo los del inquilino que inició sesión
    @GetMapping("/mios")
    @RolesPermitidos("Inquilino")
    public List<PagoResponse> mios(@RequestAttribute(Sesion.ATRIBUTO) Sesion sesion) {
        return pagos.findByContratoUsuarioIdOrderByFechaPagoDescIdDesc(sesion.usuarioId()).stream()
                .map(PagoController::aDto).toList();
    }

    // GET /api/pagos/5
     @GetMapping("/{id}")
    @RolesPermitidos({"Administrador", "Inquilino"})
    public PagoResponse obtener(@PathVariable Integer id, @RequestAttribute(Sesion.ATRIBUTO) Sesion sesion) {
        Pago p = buscar(id);
        exigirDueno(p, sesion);
        return aDto(p);
    }

    // GET /api/pagos/5/voucher: el comprobante en PDF (ruta protegida)
    @GetMapping("/{id}/voucher")
    @RolesPermitidos({"Administrador", "Inquilino"})
    public ResponseEntity<Resource> verVoucher(@PathVariable Integer id, @RequestAttribute(Sesion.ATRIBUTO) Sesion sesion) {
        Pago p = buscar(id);
        exigirDueno(p, sesion);
        if (p.getVoucherPdf() == null || p.getVoucherPdf().isBlank()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Este pago no tiene un comprobante en PDF.");
        }
        Resource recurso = vouchers.cargar(p.getVoucherPdf());
        if (recurso == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "El archivo del comprobante no está disponible en el servidor.");
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"voucher.pdf\"")
                .header("X-Content-Type-Options", "nosniff")
                .body(recurso);
    }

    /**
     * POST /api/pagos (multipart): boletaId, montoAbonado, y opcionalmente archivo (PDF) o urlVoucher.
     * El abono no puede superar el saldo que queda de la boleta; al llegar a cero la boleta pasa a Pagada.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @RolesPermitidos({"Administrador", "Inquilino"})
    @Transactional
    public Map<String, String> registrar(@RequestParam Integer boletaId,
                                         @RequestParam BigDecimal montoAbonado,
                                         @RequestParam(required = false) String urlVoucher,
                                         @RequestParam(value = "archivo", required = false) MultipartFile archivo,
                                         @RequestAttribute(Sesion.ATRIBUTO) Sesion sesion) {
        Boleta b = boletas.buscarParaActualizar(boletaId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "La boleta seleccionada no existe."));

        if (esInquilino(sesion) && !b.getContrato().getUsuario().getId().equals(sesion.usuarioId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No puedes registrar pagos de una boleta que no es tuya.");
        }
        if (b.getEstado() != EstadoBoleta.PENDIENTE) {
            throw new ApiException(HttpStatus.CONFLICT, "No existe la boleta seleccionada o ya ha sido procesada.");
        }

        BigDecimal abonadoPrevio = pagos.sumaAbonado(boletaId);
        BigDecimal saldoAntes = b.getMontoTotal().subtract(abonadoPrevio == null ? BigDecimal.ZERO : abonadoPrevio);
        BigDecimal monto = montoAbonado.setScale(2, RoundingMode.HALF_UP);
        if (monto.signum() <= 0 || monto.compareTo(saldoAntes) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "El monto abonado debe ser mayor a 0 y no puede superar el saldo pendiente (S/ "
                            + saldoAntes.toPlainString() + ").");
        }

        // Igual que tu controller: si hay PDF se usa el PDF, y si no, la URL
        boolean hayArchivo = archivo != null && !archivo.isEmpty();
        String url = null;
        if (!hayArchivo && urlVoucher != null && !urlVoucher.isBlank()) {
            url = urlVoucher.trim();
            if (url.length() > 500 || !url.matches("^https?://.+")) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "La URL del voucher debe empezar con http:// o https://.");
            }
        }
        String nombrePdf = hayArchivo ? vouchers.guardar(archivo) : null;

        BigDecimal saldoDespues = saldoAntes.subtract(monto);
        Pago p = new Pago();
        p.setContrato(b.getContrato());
        p.setDepartamento(b.getContrato().getDepartamento());
        p.setBoleta(b);
        p.setConsumoLuz(b.getConsumoLuz());
        p.setConsumoAgua(b.getConsumoAgua());
        p.setConsumoGas(b.getConsumoGas());
        p.setArbitrios(b.getArbitrios());
        p.setMontoTotal(b.getMontoTotal());
        p.setMontoAbonado(monto);
        p.setSaldoPendiente(saldoDespues);
        p.setVoucherPdf(nombrePdf);
        p.setUrlVoucher(url);
        p.setFechaPago(LocalDateTime.now());

        b.setEstado(saldoDespues.signum() == 0 ? EstadoBoleta.PAGADA : EstadoBoleta.PENDIENTE);
        b.setFechaActualizacion(LocalDateTime.now());

        try {
            pagos.saveAndFlush(p);
            boletas.saveAndFlush(b);
        } catch (RuntimeException e) {
            if (nombrePdf != null) vouchers.eliminar(nombrePdf);   // no dejar archivos huérfanos
            throw e;
        }
        return Map.of("mensaje", "Pago registrado exitosamente. Historial bancario actualizado.");
    }

       /** DELETE /api/pagos/5: anula el pago, recalcula los saldos de la boleta y la reabre si hace falta. */
    @DeleteMapping("/{id}")
    @Transactional
    public Map<String, String> anular(@PathVariable Integer id) {
        Pago p = buscar(id);
        Boleta b = boletas.buscarParaActualizar(p.getBoleta().getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "La boleta del pago ya no existe."));
        String archivo = p.getVoucherPdf();

        pagos.delete(p);
        pagos.flush();

        // Recalcula el saldo acumulado de los pagos que quedan, en orden cronológico
        List<Pago> restantes = pagos.findByBoletaIdOrderByFechaPagoAscIdAsc(b.getId());
        BigDecimal acumulado = BigDecimal.ZERO;
        for (Pago r : restantes) {
            acumulado = acumulado.add(r.getMontoAbonado());
            r.setSaldoPendiente(b.getMontoTotal().subtract(acumulado));
        }
        pagos.saveAll(restantes);

        b.setEstado(acumulado.compareTo(b.getMontoTotal()) >= 0 ? EstadoBoleta.PAGADA : EstadoBoleta.PENDIENTE);
        b.setFechaActualizacion(LocalDateTime.now());
        boletas.save(b);

        vouchers.eliminar(archivo);   // el archivo se borra solo si todo lo anterior salió bien
        return Map.of("mensaje", "El pago fue anulado exitosamente y la boleta asociada volvió a estado Pendiente.");
    }

    private Pago buscar(Integer id) {
        return pagos.buscarCompleto(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pago no encontrado."));
    }
}

