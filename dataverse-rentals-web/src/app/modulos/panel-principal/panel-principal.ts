import { AfterViewInit, Component, ElementRef, OnDestroy, inject, signal, viewChild } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Chart, registerables } from 'chart.js';
import { Dashboard, DatosDashboard, dashboardVacio } from './dashboard';
import { Alerta } from '../../compartido/alerta';

Chart.register(...registerables);

@Component({
  selector: 'app-panel-principal',
  imports: [RouterLink, DatePipe, DecimalPipe],
  templateUrl: './panel-principal.html'
})
export class PanelPrincipal implements AfterViewInit, OnDestroy {
  private servicio = inject(Dashboard);
  private alerta = inject(Alerta);

  datos = signal<DatosDashboard>(dashboardVacio());
  cargando = signal(true);

  private canvasIngresos = viewChild.required<ElementRef<HTMLCanvasElement>>('canvasIngresos');
  private canvasConsumos = viewChild.required<ElementRef<HTMLCanvasElement>>('canvasConsumos');
  private graficos: Chart[] = [];

  ngAfterViewInit(): void {
    this.servicio.cargar().subscribe({
      next: d => {
        this.datos.set(d);
        this.cargando.set(false);
        this.dibujar(d);
      },
      error: () => {
        this.cargando.set(false);
        this.alerta.error('No se pudo cargar el panel de control.');
      }
    });
  }

  private dibujar(d: DatosDashboard): void {
    this.graficos.forEach(g => g.destroy());

    // 1) Barras: historial recaudado mensual
    const barras = new Chart(this.canvasIngresos().nativeElement, {
      type: 'bar',
      data: {
        labels: d.mesesLabels,
        datasets: [{
          label: 'Ingresos Mensuales (S/)',
          data: d.ingresosPorMes,
          backgroundColor: '#198754',
          borderRadius: 4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } }
      }
    });

    // 2) Anillo: consumos generales del edificio
    const anillo = new Chart(this.canvasConsumos().nativeElement, {
      type: 'doughnut',
      data: {
        labels: ['Agua (m³)', 'Luz (kWh)', 'Gas (m³)'],
        datasets: [{
          data: [d.totalAgua, d.totalLuz, d.totalGas],
          backgroundColor: ['#0d6efd', '#ffc107', '#dc3545'],
          borderWidth: 2,
          hoverOffset: 4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '65%',
        plugins: {
          legend: { position: 'right', labels: { boxWidth: 15, padding: 15, font: { size: 12 } } }
        }
      }
    });

    this.graficos = [barras, anillo];
  }

  ngOnDestroy(): void {
    this.graficos.forEach(g => g.destroy());
  }
}