import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { IngresoMes, Reportes } from '../reportes';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-reportes-ingresos',
  imports: [RouterLink, DecimalPipe],
  templateUrl: './reportes-ingresos.html'
})
export class ReportesIngresos implements OnInit {
  private servicio = inject(Reportes);
  private alerta = inject(Alerta);

  meses = signal<IngresoMes[]>([]);
  cargando = signal(true);

  totalAbonado = computed(() => this.meses().reduce((s, m) => s + m.abonado, 0));
  totalSaldo = computed(() => this.meses().reduce((s, m) => s + m.saldo, 0));

  ngOnInit(): void {
    this.servicio.ingresosPorMes().subscribe({
      next: datos => { this.meses.set(datos); this.cargando.set(false); },
      error: () => { this.cargando.set(false); this.alerta.error('No se pudo cargar el historial de ingresos.'); }
    });
  }
}