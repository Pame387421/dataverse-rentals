package com.dataverse.rentals.repository;

import com.dataverse.rentals.entity.Encuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EncuestaRepository extends JpaRepository<Encuesta, Integer> {

    List<Encuesta> findAllByOrderByFechaRegistroDescIdDesc();

    List<Encuesta> findByUsuarioIdOrderByFechaRegistroDescIdDesc(Integer usuarioId);

    boolean existsByUsuarioId(Integer usuarioId);
}