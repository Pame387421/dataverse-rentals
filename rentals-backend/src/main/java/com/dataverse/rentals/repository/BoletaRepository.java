package com.dataverse.rentals.repository;

import com.dataverse.rentals.entity.Boleta;
import com.dataverse.rentals.entity.EstadoBoleta;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BoletaRepository extends JpaRepository<Boleta, Integer> {

    @EntityGraph(attributePaths = {"contrato", "contrato.usuario", "contrato.departamento",
            "contrato.departamento.edificio", "detalles"})
    List<Boleta> findAllByOrderByFechaEmisionDescIdDesc();

    @EntityGraph(attributePaths = {"contrato", "contrato.usuario", "contrato.departamento",
            "contrato.departamento.edificio", "detalles"})
    List<Boleta> findByContratoUsuarioIdOrderByFechaEmisionDescIdDesc(Integer usuarioId);

    @EntityGraph(attributePaths = {"contrato", "contrato.usuario", "contrato.departamento",
            "contrato.departamento.edificio", "detalles"})
    List<Boleta> findByEstadoOrderByFechaEmisionDescIdDesc(EstadoBoleta estado);

    @EntityGraph(attributePaths = {"contrato", "contrato.usuario", "contrato.departamento",
            "contrato.departamento.edificio", "detalles"})
    List<Boleta> findByEstadoAndContratoUsuarioIdOrderByFechaEmisionDescIdDesc(EstadoBoleta estado, Integer usuarioId);

    @EntityGraph(attributePaths = {"contrato", "contrato.usuario", "contrato.departamento",
            "contrato.departamento.edificio", "detalles"})
    @Query("select b from Boleta b where b.id = :id")
    Optional<Boleta> buscarCompleta(@Param("id") Integer id);

    /** Bloquea la fila mientras dura la transacción: evita que dos pagos simultáneos pasen del saldo. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Boleta b where b.id = :id")
    Optional<Boleta> buscarParaActualizar(@Param("id") Integer id);

    boolean existsByContratoIdAndFechaEmisionGreaterThanEqualAndFechaEmisionLessThan(
            Integer contratoId, LocalDateTime desde, LocalDateTime hasta);
}