package com.dataverse.rentals.service;

import com.dataverse.rentals.entity.Boleta;
import com.dataverse.rentals.entity.Contrato;
import com.dataverse.rentals.entity.LecturaMedidor;
import com.dataverse.rentals.entity.TipoMedidor;
import com.dataverse.rentals.repository.LecturaMedidorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/** Las mismas reglas de tu BoletaController: tarifas, consumo = última lectura - anterior, y arbitrios residenciales. */
@Service
@RequiredArgsConstructor
public class CalculoBoletaService {

    public static final BigDecimal TARIFA_LUZ = new BigDecimal("0.85");
    public static final BigDecimal TARIFA_AGUA = new BigDecimal("4.50");
    public static final BigDecimal TARIFA_GAS = new BigDecimal("2.50");

    private static final String[] MESES = {"enero", "febrero", "marzo", "abril", "mayo", "junio", "julio",
            "agosto", "setiembre", "octubre", "noviembre", "diciembre"};

    private final LecturaMedidorRepository lecturas;

    public record Resultado(BigDecimal alquiler, BigDecimal arbitrios, BigDecimal luz, BigDecimal agua, BigDecimal gas) {
        public BigDecimal total() {
            return alquiler.add(arbitrios).add(luz).add(agua).add(gas);
        }
    }

    public Resultado calcular(Contrato contrato) {
        var departamento = contrato.getDepartamento();
        List<LecturaMedidor> todas =
                lecturas.findByMedidorDepartamentoIdOrderByFechaLecturaDescIdDesc(departamento.getId());

        return new Resultado(
                dinero(contrato.getMontoAlquiler()),
                arbitrios(departamento.getAreaM2()),
                montoDe(todas, TipoMedidor.LUZ, TARIFA_LUZ),
                montoDe(todas, TipoMedidor.AGUA, TARIFA_AGUA),
                montoDe(todas, TipoMedidor.GAS, TARIFA_GAS));
    }

    /** Dos lecturas o más: (última - anterior) x tarifa. Con una sola lectura: su valor x tarifa. */
    private static BigDecimal montoDe(List<LecturaMedidor> todas, TipoMedidor tipo, BigDecimal tarifa) {
        List<LecturaMedidor> delTipo = todas.stream().filter(l -> l.getMedidor().getTipo() == tipo).toList();
        if (delTipo.size() >= 2) {
            BigDecimal consumo = delTipo.get(0).getValor().subtract(delTipo.get(1).getValor());
            return consumo.signum() > 0 ? dinero(consumo.multiply(tarifa)) : dinero(BigDecimal.ZERO);
        }
        if (delTipo.size() == 1) return dinero(delTipo.get(0).getValor().multiply(tarifa));
        return dinero(BigDecimal.ZERO);
    }

    /** CalculoArbitrios.calcularMensual("Residencial", area, 0): limpieza 0.35 + parques 0.15 + serenazgo 0.50 por m². */
    private static BigDecimal arbitrios(BigDecimal areaM2) {
        BigDecimal area = areaM2 == null ? BigDecimal.ZERO : areaM2;
        BigDecimal total = area.multiply(new BigDecimal("0.35"))
                .add(area.multiply(new BigDecimal("0.15")))
                .add(area.multiply(new BigDecimal("0.50")));
        return dinero(total);
    }

    private static BigDecimal dinero(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    /** "octubre 2026". */
    public static String periodo(LocalDate fecha) {
        return MESES[fecha.getMonthValue() - 1] + " " + fecha.getYear();
    }

    /** Desglose igual al de tu doPost: los rubros en cero no se incluyen. */
    public void armarDetalles(Boleta boleta, Resultado r, String etiqueta) {
        boleta.agregarDetalle("Alquiler Base - " + etiqueta, r.alquiler());
        if (r.arbitrios().signum() > 0) boleta.agregarDetalle("Arbitrios Municipales - " + etiqueta, r.arbitrios());
        if (r.agua().signum() > 0) boleta.agregarDetalle("Consumo Agua - " + etiqueta, r.agua());
        if (r.luz().signum() > 0) boleta.agregarDetalle("Consumo Luz - " + etiqueta, r.luz());
        if (r.gas().signum() > 0) boleta.agregarDetalle("Consumo Gas - " + etiqueta, r.gas());
    }
}