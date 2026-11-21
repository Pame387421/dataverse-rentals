import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Pagos } from '../pagos';
import { Pago } from '../../../nucleo/modelos/pago';
import { Alerta } from '../../../compartido/alerta';
import { VerVoucher } from '../../../compartido/ver-voucher';

@Component({
  selector: 'app-pagos-lista',
  imports: [RouterLink, DatePipe, DecimalPipe, VerVoucher],
  templateUrl: './pagos-lista.html'
})
export class PagosLista implements OnInit {
  private servicio = inject(Pagos);
  private alerta = inject(Alerta);

  pagos = signal<Pago[]>([]);
  cargando = signal(true);

  // Métricas (equivalen a los cálculos al inicio de tu index.jsp)
  totalTransacciones = computed(() => this.pagos().length);
  totalAbonado = computed(() => this.pagos().reduce((s, p) => s + p.montoAbonado, 0));
  totalPorCobrar = computed(() => this.pagos().reduce((s, p) => s + p.saldoPendiente, 0));
  conciliadas = computed(() => this.pagos().filter(p => p.saldoPendiente === 0).length);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.servicio.listar().subscribe({
      next: datos => { this.pagos.set(datos); this.cargando.set(false); },
      error: () => { this.cargando.set(false); this.alerta.error('No se pudo cargar la lista de pagos.'); }
    });
  }

  async anular(p: Pago): Promise<void> {
    const ok = await this.alerta.confirmarEliminacion(
      `Se anulará el pago de ${p.inquilino} (Boleta #${p.boletaId}). La boleta volverá a estado Pendiente.`);
    if (!ok) return;

    this.servicio.eliminar(p.id!).subscribe({
      next: r => { this.alerta.exito(r.mensaje); this.cargar(); },
      error: err => this.alerta.error(err.error?.error ?? 'No se pudo anular el pago.')
    });
  }
}