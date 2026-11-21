package com.dataverse.rentals.repository;

import com.dataverse.rentals.entity.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Integer> {

    Optional<Cuenta> findByCorreoIgnoreCase(String correo);

    Optional<Cuenta> findByUsuarioId(Integer usuarioId);
}