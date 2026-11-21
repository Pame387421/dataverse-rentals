package com.dataverse.rentals.repository;

import com.dataverse.rentals.entity.Mantenimiento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MantenimientoRepository extends JpaRepository<Mantenimiento, Integer> {

    @EntityGraph(attributePaths = {"departamento", "departamento.edificio", "proveedor"})
    List<Mantenimiento> findAllByOrderByFechaRegistroDescIdDesc();

    boolean existsByProveedorId(Integer proveedorId);
}