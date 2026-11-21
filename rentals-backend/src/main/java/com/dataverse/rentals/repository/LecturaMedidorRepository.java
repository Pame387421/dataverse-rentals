package com.dataverse.rentals.repository;

import com.dataverse.rentals.entity.LecturaMedidor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LecturaMedidorRepository extends JpaRepository<LecturaMedidor, Integer> {

    /** Igual que tu DAO antiguo: de la más reciente a la más antigua. */
    @EntityGraph(attributePaths = {"medidor", "medidor.departamento", "medidor.departamento.edificio"})
    List<LecturaMedidor> findAllByOrderByFechaLecturaDescIdDesc();

    boolean existsByMedidorId(Integer medidorId);

    /** Lecturas de todos los medidores de un departamento, de la más reciente a la más antigua. */
    @EntityGraph(attributePaths = "medidor")
    List<LecturaMedidor> findByMedidorDepartamentoIdOrderByFechaLecturaDescIdDesc(Integer departamentoId);
}