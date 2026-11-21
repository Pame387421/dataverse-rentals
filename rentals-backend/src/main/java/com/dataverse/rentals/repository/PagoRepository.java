package com.dataverse.rentals.repository;

import com.dataverse.rentals.entity.Pago;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Integer> {

    /** Total abonado por boleta (para saber cuánto falta pagar). */
    interface SumaBoleta {
        Integer getBoletaId();
        BigDecimal getTotal();
    }

    @EntityGraph(attributePaths = {"contrato", "contrato.usuario", "departamento", "departamento.edificio", "boleta"})
    List<Pago> findAllByOrderByFechaPagoDescIdDesc();

    @EntityGraph(attributePaths = {"contrato", "contrato.usuario", "departamento", "departamento.edificio", "boleta"})
    List<Pago> findByContratoUsuarioIdOrderByFechaPagoDescIdDesc(Integer usuarioId);

    @EntityGraph(attributePaths = {"contrato", "contrato.usuario", "departamento", "departamento.edificio", "boleta"})
    @Query("select p from Pago p where p.id = :id")
    Optional<Pago> buscarCompleto(@Param("id") Integer id);

    List<Pago> findByBoletaIdOrderByFechaPagoAscIdAsc(Integer boletaId);

    /** null si la boleta aún no tiene pagos. */
    @Query("select sum(p.montoAbonado) from Pago p where p.boleta.id = :boletaId")
    BigDecimal sumaAbonado(@Param("boletaId") Integer boletaId);

    @Query("select p.boleta.id as boletaId, sum(p.montoAbonado) as total from Pago p group by p.boleta.id")
    List<SumaBoleta> sumarPorBoleta();
}