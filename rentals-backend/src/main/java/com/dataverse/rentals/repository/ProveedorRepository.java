package com.dataverse.rentals.repository;

import com.dataverse.rentals.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {

    /** Igual que tu DAO: del más reciente al más antiguo. */
    List<Proveedor> findAllByOrderByIdDesc();

    boolean existsByRuc(String ruc);

    boolean existsByRucAndIdNot(String ruc, Integer id);
}