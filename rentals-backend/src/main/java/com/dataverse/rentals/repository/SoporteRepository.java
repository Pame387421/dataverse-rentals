package com.dataverse.rentals.repository;

import com.dataverse.rentals.entity.Soporte;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SoporteRepository extends JpaRepository<Soporte, Integer> {

    @EntityGraph(attributePaths = {"departamento", "departamento.edificio", "usuario", "mantenimiento"})
    List<Soporte> findAllByOrderByFechaRegistroDescIdDesc();

    @EntityGraph(attributePaths = {"departamento", "departamento.edificio", "usuario", "mantenimiento"})
    List<Soporte> findByUsuarioIdOrderByFechaRegistroDescIdDesc(Integer usuarioId);

    /** Los tickets que ya apuntan a una orden (para deducir el ticket de cada orden). */
    @EntityGraph(attributePaths = {"mantenimiento"})
    List<Soporte> findByMantenimientoIdIsNotNull();

    boolean existsByUsuarioId(Integer usuarioId);

    /** Si se elimina una orden, sus tickets quedan sin orden vinculada. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Soporte s set s.mantenimiento = null where s.mantenimiento.id = :id")
    void desvincularDeMantenimiento(@Param("id") Integer id);

        /** Tickets que aún no están resueltos (los que cuenta el panel como incidencias). */
    long countByEstadoNot(String estado);
}