package com.dataverse.rentals.repository;

import com.dataverse.rentals.entity.Contrato;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Integer> {

    @Override
    @EntityGraph(attributePaths = {"departamento", "departamento.edificio", "usuario"})
    List<Contrato> findAll(Sort sort);

    @EntityGraph(attributePaths = {"departamento", "departamento.edificio", "usuario"})
    List<Contrato> findByUsuarioIdOrderByIdAsc(Integer usuarioId);

    boolean existsByUsuarioId(Integer usuarioId);

    boolean existsByDepartamentoIdAndUsuarioId(Integer departamentoId, Integer usuarioId);

    // Boletas y pagos todavía no tienen entidad en el backend: se consultan directamente
    @Query(value = "SELECT (SELECT COUNT(*) FROM Boletas WHERE ContratoId = :id) "
            + "+ (SELECT COUNT(*) FROM Pagos WHERE ContratoId = :id)", nativeQuery = true)
    long contarBoletasYPagos(@Param("id") Integer id);
}