package com.dataverse.rentals.repository;

import com.dataverse.rentals.entity.Departamento;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, Integer> {

    /** Cantidad de departamentos por edificio (para la lista de edificios). */
    interface ConteoEdificio {
        Integer getEdificioId();
        Long getTotal();
    }

    // El edificio se trae junto con cada departamento, en una sola consulta
    @Override
    @EntityGraph(attributePaths = "edificio")
    List<Departamento> findAll(Sort sort);

    @EntityGraph(attributePaths = "edificio")
    List<Departamento> findByEdificioId(Integer edificioId, Sort sort);

    long countByEdificioId(Integer edificioId);

    boolean existsByEdificioId(Integer edificioId);

    boolean existsByEdificioIdAndNumeroDepartamentoIgnoreCase(Integer edificioId, String numeroDepartamento);

    boolean existsByEdificioIdAndNumeroDepartamentoIgnoreCaseAndIdNot(Integer edificioId, String numeroDepartamento, Integer id);

    @Query("select d.edificio.id as edificioId, count(d) as total from Departamento d group by d.edificio.id")
    List<ConteoEdificio> contarPorEdificio();

    // Tablas que todavía no tienen entidad en el backend: se consultan directamente
    @Query(value = "SELECT COUNT(*) FROM Contratos WHERE DepartamentoId = :id", nativeQuery = true)
    long contarContratos(@Param("id") Integer id);

    @Query(value = "SELECT (SELECT COUNT(*) FROM Medidores WHERE DepartamentoId = :id) "
            + "+ (SELECT COUNT(*) FROM Soportes WHERE DepartamentoId = :id) "
            + "+ (SELECT COUNT(*) FROM Mantenimientos WHERE DepartamentoId = :id) "
            + "+ (SELECT COUNT(*) FROM Pagos WHERE DepartamentoId = :id)", nativeQuery = true)
    long contarOtrasRelaciones(@Param("id") Integer id);

        /** Los departamentos de los contratos de un usuario (para el inquilino). */
    @Query("select d from Departamento d join fetch d.edificio "
            + "where d.id in (select c.departamento.id from Contrato c where c.usuario.id = :usuarioId) "
            + "order by d.id")
    List<Departamento> findDeUsuario(@Param("usuarioId") Integer usuarioId);
}