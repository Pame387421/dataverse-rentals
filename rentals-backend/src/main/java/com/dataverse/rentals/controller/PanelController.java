package com.dataverse.rentals.controller;

import com.dataverse.rentals.dto.PanelDtos.*;
import com.dataverse.rentals.security.RolesPermitidos;
import com.dataverse.rentals.service.PanelService;
import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
//@CrossOrigin(origins = "http://localhost:4200")
@RolesPermitidos("Administrador")
@RequiredArgsConstructor
public class PanelController {

    private final PanelService panel;

    @GetMapping("/panel")
    public DashboardResponse dashboard() {
        return panel.dashboard();
    }

    @GetMapping("/estado")
    public EstadoSistema estado() {
        return panel.estado();
    }

    @GetMapping("/reportes/resumen")
    public ResumenReportes resumen() {
        return panel.resumenReportes();
    }

    @GetMapping("/reportes/consumos")
    public List<ConsumoUnidad> consumos() {
        return panel.consumosPorUnidad();
    }

    @GetMapping("/reportes/ingresos")
    public List<IngresoMes> ingresos() {
        return panel.ingresosPorMes();
    }

    @GetMapping("/reportes/alertas")
    public List<ContratoPorVencer> alertas() {
        return panel.contratosPorVencer();
    }
}