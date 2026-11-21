import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Boletas } from '../boletas';
import { Pagos } from '../../pagos/pagos';
import { Boleta, periodo } from '../../../nucleo/modelos/boleta';
import { Pago } from '../../../nucleo/modelos/pago';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-boletas-lista',
  imports: [RouterLink, DatePipe, DecimalPipe],
  templateUrl: './boletas-lista.html'
})
export class BoletasLista implements OnInit {
  private servicio = inject(Boletas);
  private servicioPagos = inject(Pagos);
  private alerta = inject(Alerta);

  boletas = signal<Boleta[]>([]);
  pagos = signal<Pago[]>([]);
  cargando = signal(true);
  etiquetaPeriodo = periodo;

  // Métricas (equivalen a los cálculos al inicio de tu index.jsp)
  totalRecibos = computed(() => this.boletas().length);
  montoFacturado = computed(() => this.boletas().reduce((s, b) => s + b.montoTotal, 0));
  pendientes = computed(() => this.boletas().filter(b => b.estado === 'PENDIENTE').length);
  pagadas = computed(() => this.boletas().filter(b => b.estado === 'PAGADA').length);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    forkJoin({ boletas: this.servicio.listar(), pagos: this.servicioPagos.listar() }).subscribe({
      next: ({ boletas, pagos }) => {
        this.boletas.set(boletas);
        this.pagos.set(pagos);
        this.cargando.set(false);
      },
      error: () => { this.cargando.set(false); this.alerta.error('No se pudo cargar el historial de boletas.'); }
    });
  }

  pagoDe(b: Boleta): Pago | undefined {
    return this.pagos().find(p => p.boletaId === b.id);
  }

  async eliminar(b: Boleta): Promise<void> {
    const ok = await this.alerta.confirmarEliminacion(
      `Se eliminará la boleta #${b.id} de ${b.inquilino}. El pago asociado también será eliminado.`);
    if (!ok) return;

    this.servicio.eliminar(b.id!).subscribe({
      next: r => { this.alerta.exito(r.mensaje); this.cargar(); },
      error: err => this.alerta.error(err.error?.error ?? 'No se pudo eliminar la boleta.')
    });
  }
}