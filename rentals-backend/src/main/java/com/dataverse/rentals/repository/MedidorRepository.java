package com.dataverse.rentals.repository;

import com.dataverse.rentals.entity.Medidor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedidorRepository extends JpaRepository<Medidor, Integer> {

    @Override
    @EntityGraph(attributePaths = {"departamento", "departamento.edificio"})
    List<Medidor> findAll(Sort sort);

    boolean existsByNumeroSerieIgnoreCase(String numeroSerie);

    boolean existsByNumeroSerieIgnoreCaseAndIdNot(String numeroSerie, Integer id);
}