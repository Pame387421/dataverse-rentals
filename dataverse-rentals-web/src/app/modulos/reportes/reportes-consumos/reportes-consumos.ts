import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ConsumoUnidad, Reportes } from '../reportes';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-reportes-consumos',
  imports: [RouterLink, DecimalPipe],
  templateUrl: './reportes-consumos.html'
})
export class ReportesConsumos implements OnInit {
  private servicio = inject(Reportes);
  private alerta = inject(Alerta);

  consumos = signal<ConsumoUnidad[]>([]);
  cargando = signal(true);

  ngOnInit(): void {
    this.servicio.consumosPorUnidad().subscribe({
      next: datos => { this.consumos.set(datos); this.cargando.set(false); },
      error: () => { this.cargando.set(false); this.alerta.error('No se pudo cargar la auditoría de consumos.'); }
    });
  }
}