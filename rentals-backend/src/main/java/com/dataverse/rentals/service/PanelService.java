package com.dataverse.rentals.service;

import com.dataverse.rentals.dto.PanelDtos.*;
import com.dataverse.rentals.entity.Boleta;
import com.dataverse.rentals.entity.Contrato;
import com.dataverse.rentals.entity.Departamento;
import com.dataverse.rentals.entity.LecturaMedidor;
import com.dataverse.rentals.entity.Medidor;
import com.dataverse.rentals.entity.Pago;
import com.dataverse.rentals.repository.BoletaRepository;
import com.dataverse.rentals.repository.ContratoRepository;
import com.dataverse.rentals.repository.DepartamentoRepository;
import com.dataverse.rentals.repository.LecturaMedidorRepository;
import com.dataverse.rentals.repository.PagoRepository;
import com.dataverse.rentals.repository.SoporteRepository;
import com.dataverse.rentals.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/** Las métricas del panel, los reportes y el estado del sistema (antes se calculaban en el navegador). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PanelService {

    private static final String[] MESES_CORTOS = {"ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "set", "oct", "nov", "dic"};
    private static final int DIAS_ALERTA = 30;

    private final PagoRepository pagos;
    private final BoletaRepository boletas;
    private final ContratoRepository contratos;
    private final DepartamentoRepository departamentos;
    private final UsuarioRepository usuarios;
    private final SoporteRepository soportes;
    private final LecturaMedidorRepository lecturas;
    private final DataSource dataSource;

    // ---------------------------------------------------------------- Apoyo

    private static BigDecimal r2(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    private static String clave(LocalDate f) {
        return String.format("%d-%02d", f.getYear(), f.getMonthValue());
    }

    /** "2026-10" -> "oct 2026". */
    private static String etiqueta(String clave) {
        String[] partes = clave.split("-");
        return MESES_CORTOS[Integer.parseInt(partes[1]) - 1] + " " + partes[0];
    }

    private static String ubicacion(Departamento d) {
        return d.getEdificio().getNombre() + " - Piso " + d.getPiso() + " - Dpto " + d.getNumeroDepartamento();
    }

    /** Total abonado por mes (yyyy-MM), del más antiguo al más reciente. */
    private static Map<String, BigDecimal> abonadoPorMes(List<Pago> lista) {
        Map<String, BigDecimal> mapa = new TreeMap<>();
        for (Pago p : lista) mapa.merge(clave(p.getFechaPago().toLocalDate()), p.getMontoAbonado(), BigDecimal::add);
        return mapa;
    }

    // ---------------------------------------------------------------- Panel principal

    public DashboardResponse dashboard() {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(DIAS_ALERTA);

        List<Pago> todos = pagos.findAllByOrderByFechaPagoDescIdDesc();
        Map<String, BigDecimal> porMes = abonadoPorMes(todos);
        List<String> claves = new ArrayList<>(porMes.keySet());
        List<String> ultimas = claves.subList(Math.max(0, claves.size() - 6), claves.size());

        // Última lectura de cada tipo en cada departamento (la lista ya viene de la más reciente a la más antigua)
        BigDecimal agua = BigDecimal.ZERO, luz = BigDecimal.ZERO, gas = BigDecimal.ZERO;
        Set<String> vistos = new HashSet<>();
        for (LecturaMedidor l : lecturas.findAllByOrderByFechaLecturaDescIdDesc()) {
            Medidor m = l.getMedidor();
            if (!vistos.add(m.getDepartamento().getId() + "|" + m.getTipo())) continue;
            switch (m.getTipo()) {
                case AGUA -> agua = agua.add(l.getValor());
                case LUZ -> luz = luz.add(l.getValor());
                case GAS -> gas = gas.add(l.getValor());
            }
        }

        List<Contrato> todosContratos = contratos.findAll(Sort.by("id"));
        Set<Integer> conContratoVigente = new HashSet<>();
        int porVencer = 0;
        for (Contrato c : todosContratos) {
            if (!c.getFechaFinContrato().isBefore(hoy)) conContratoVigente.add(c.getDepartamento().getId());
            if (!c.getFechaFinContrato().isBefore(hoy) && !c.getFechaFinContrato().isAfter(limite)) porVencer++;
        }

        List<PagoResumen> recientes = todos.stream().limit(5)
                .map(p -> new PagoResumen(p.getFechaPago(), p.getContrato().getUsuario().getNombre(),
                        ubicacion(p.getDepartamento()), r2(p.getMontoAbonado())))
                .toList();

        return new DashboardResponse(
                CalculoBoletaService.periodo(hoy), "Central",
                r2(porMes.getOrDefault(clave(hoy), BigDecimal.ZERO)),
                r2(agua.add(gas)),
                porVencer,
                usuarios.count(),
                (int) (departamentos.count() - conContratoVigente.size()),
                soportes.countByEstadoNot("RESUELTO"),
                ultimas.stream().map(PanelService::etiqueta).toList(),
                ultimas.stream().map(c -> r2(porMes.get(c))).toList(),
                r2(agua), r2(luz), r2(gas), recientes);
    }

    // ---------------------------------------------------------------- Reportes

    public ResumenReportes resumenReportes() {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(DIAS_ALERTA);

        // Ingresos de los últimos 3 meses; un mes sin pagos cuenta como cero
        Map<String, BigDecimal> porMes = abonadoPorMes(pagos.findAllByOrderByFechaPagoDescIdDesc());
        List<String> claves = new ArrayList<>();
        for (int i = 2; i >= 0; i--) claves.add(clave(hoy.withDayOfMonth(1).minusMonths(i)));

        BigDecimal luz = BigDecimal.ZERO, agua = BigDecimal.ZERO, gas = BigDecimal.ZERO;
        for (Boleta b : boletas.findAllByOrderByFechaEmisionDescIdDesc()) {
            luz = luz.add(b.getConsumoLuz());
            agua = agua.add(b.getConsumoAgua());
            gas = gas.add(b.getConsumoGas());
        }

        int vigentes = 0, porVencer = 0;
        for (Contrato c : contratos.findAll(Sort.by("id"))) {
            LocalDate fin = c.getFechaFinContrato();
            if (fin.isAfter(limite)) vigentes++;                          // los ya vencidos no cuentan como vigentes
            else if (!fin.isBefore(hoy)) porVencer++;
        }

        return new ResumenReportes(
                new IngresosTresMeses(claves.stream().map(PanelService::etiqueta).toList(),
                        claves.stream().map(c -> r2(porMes.getOrDefault(c, BigDecimal.ZERO))).toList()),
                new ConsumosGlobales(r2(luz), r2(agua), r2(gas)),
                new MetricasContratos(vigentes, porVencer));
    }

    /** Montos facturados por departamento (agrupado por departamento, no por nombre de edificio). */
    public List<ConsumoUnidad> consumosPorUnidad() {
        Map<Integer, BigDecimal[]> acumulado = new HashMap<>();   // luz, agua, gas, arbitrios
        Map<Integer, String> nombres = new HashMap<>();

        for (Boleta b : boletas.findAllByOrderByFechaEmisionDescIdDesc()) {
            Departamento d = b.getContrato().getDepartamento();
            BigDecimal[] a = acumulado.computeIfAbsent(d.getId(),
                    k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
            a[0] = a[0].add(b.getConsumoLuz());
            a[1] = a[1].add(b.getConsumoAgua());
            a[2] = a[2].add(b.getConsumoGas());
            a[3] = a[3].add(b.getArbitrios());
            nombres.putIfAbsent(d.getId(), d.getEdificio().getNombre() + " - P" + d.getPiso() + " - " + d.getNumeroDepartamento());
        }

        return acumulado.entrySet().stream()
                .map(e -> {
                    BigDecimal[] a = e.getValue();
                    return new ConsumoUnidad(nombres.get(e.getKey()), r2(a[0]), r2(a[1]), r2(a[2]), r2(a[3]),
                            r2(a[0].add(a[1]).add(a[2]).add(a[3])));
                })
                .sorted(Comparator.comparing(ConsumoUnidad::departamento))
                .toList();
    }

    /** Meses con pagos, del más reciente al más antiguo. El saldo cuenta cada boleta una sola vez por mes. */
    public List<IngresoMes> ingresosPorMes() {
        Map<String, List<Pago>> porMes = new TreeMap<>(Comparator.reverseOrder());
        for (Pago p : pagos.findAllByOrderByFechaPagoDescIdDesc()) {   // de más reciente a más antiguo
            porMes.computeIfAbsent(clave(p.getFechaPago().toLocalDate()), k -> new ArrayList<>()).add(p);
        }

        List<IngresoMes> resultado = new ArrayList<>();
        for (Map.Entry<String, List<Pago>> e : porMes.entrySet()) {
            BigDecimal abonado = BigDecimal.ZERO, saldo = BigDecimal.ZERO;
            Set<Integer> boletasVistas = new HashSet<>();
            for (Pago p : e.getValue()) {
                abonado = abonado.add(p.getMontoAbonado());
                // El primero que aparece de cada boleta es su último pago del mes: ese saldo es el que quedó
                if (boletasVistas.add(p.getBoleta().getId())) saldo = saldo.add(p.getSaldoPendiente());
            }
            resultado.add(new IngresoMes(e.getKey(), etiqueta(e.getKey()), e.getValue().size(), r2(abonado), r2(saldo)));
        }
        return resultado;
    }

    public List<ContratoPorVencer> contratosPorVencer() {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(DIAS_ALERTA);
        return contratos.findAll(Sort.by("id")).stream()
                .filter(c -> !c.getFechaFinContrato().isBefore(hoy) && !c.getFechaFinContrato().isAfter(limite))
                .sorted(Comparator.comparing(Contrato::getFechaFinContrato))
                .map(c -> new ContratoPorVencer(c.getId(), c.getUsuario().getNombre(), ubicacion(c.getDepartamento()),
                        c.getFechaFinContrato(), ChronoUnit.DAYS.between(hoy, c.getFechaFinContrato())))
                .toList();
    }

    // ---------------------------------------------------------------- Estado del sistema

    public EstadoSistema estado() {
        boolean conectada;
        try (Connection c = dataSource.getConnection()) {
            conectada = c.isValid(2);
        } catch (Exception e) {
            conectada = false;
        }

        long totalContratos = 0, totalPagos = 0, totalSoportes = 0;
        if (conectada) {
            try {
                totalContratos = contratos.count();
                totalPagos = pagos.count();
                totalSoportes = soportes.count();
            } catch (Exception e) {
                conectada = false;
            }
        }
        return new EstadoSistema(conectada, totalContratos, totalPagos, totalSoportes,
                conectada ? "Operativo" : "Con incidencias", LocalDateTime.now());
    }
}