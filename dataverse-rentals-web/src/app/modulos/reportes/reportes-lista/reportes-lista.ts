import { AfterViewInit, Component, ElementRef, OnDestroy, inject, viewChild } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Chart, registerables } from 'chart.js';
import { Reportes, ResumenReportes } from '../reportes';
import { Alerta } from '../../../compartido/alerta';

Chart.register(...registerables);

@Component({
  selector: 'app-reportes-lista',
  imports: [RouterLink],
  templateUrl: './reportes-lista.html'
})
export class ReportesLista implements AfterViewInit, OnDestroy {
  private servicio = inject(Reportes);
  private alerta = inject(Alerta);

  private canvasIngresos = viewChild.required<ElementRef<HTMLCanvasElement>>('canvasIngresos');
  private canvasConsumos = viewChild.required<ElementRef<HTMLCanvasElement>>('canvasConsumos');
  private canvasContratos = viewChild.required<ElementRef<HTMLCanvasElement>>('canvasContratos');
  private graficos: Chart[] = [];

  ngAfterViewInit(): void {
    this.servicio.resumen().subscribe({
      next: r => this.dibujar(r),
      error: () => this.alerta.error('No se pudieron cargar los reportes.')
    });
  }

  private dibujar(r: ResumenReportes): void {
    this.graficos.forEach(g => g.destroy());

    const leyenda = {
      legend: { position: 'bottom' as const, labels: { boxWidth: 10, padding: 8, font: { size: 11 } } }
    };

    // 1) Barras: ingresos de los últimos 3 meses
    const ingresos = new Chart(this.canvasIngresos().nativeElement, {
      type: 'bar',
      data: {
        labels: r.ingresos.etiquetas,
        datasets: [{ label: 'Recaudado (S/)', data: r.ingresos.valores, backgroundColor: '#0d6efd', borderRadius: 3 }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: { x: { grid: { display: false } }, y: { display: false } }
      }
    });

    // 2) Rosca: distribución de los gastos de consumo
    const consumos = new Chart(this.canvasConsumos().nativeElement, {
      type: 'doughnut',
      data: {
        labels: ['Luz', 'Agua', 'Gas'],
        datasets: [{
          data: [r.consumos.luz, r.consumos.agua, r.consumos.gas],
          backgroundColor: ['#ffc107', '#0dcaf0', '#fd7e14'],
          borderWidth: 1
        }]
      },
      options: { responsive: true, maintainAspectRatio: false, plugins: leyenda }
    });

    // 3) Anillo: contratos vigentes frente a los que están por vencer
    const contratos = new Chart(this.canvasContratos().nativeElement, {
      type: 'doughnut',
      data: {
        labels: ['Vigentes', 'Por Vencer'],
        datasets: [{
          data: [r.contratos.vigentes, r.contratos.porVencer],
          backgroundColor: ['#198754', '#dc3545'],
          borderWidth: 1
        }]
      },
      options: { responsive: true, maintainAspectRatio: false, cutout: '72%', plugins: leyenda }
    });

    this.graficos = [ingresos, consumos, contratos];
  }

  ngOnDestroy(): void {
    this.graficos.forEach(g => g.destroy());
  }
}