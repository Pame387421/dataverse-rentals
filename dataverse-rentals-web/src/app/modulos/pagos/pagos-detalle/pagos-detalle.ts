import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Pagos } from '../pagos';
import { Pago } from '../../../nucleo/modelos/pago';
import { Alerta } from '../../../compartido/alerta';
import { VerVoucher } from '../../../compartido/ver-voucher';

@Component({
  selector: 'app-pagos-detalle',
  imports: [RouterLink, DatePipe, DecimalPipe, VerVoucher],
  templateUrl: './pagos-detalle.html'
})
export class PagosDetalle implements OnInit {
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private servicio = inject(Pagos);
  private alerta = inject(Alerta);

  pago = signal<Pago | null>(null);

  ngOnInit(): void {
    const id = Number(this.ruta.snapshot.paramMap.get('id'));
    this.servicio.obtener(id).subscribe({
      next: p => this.pago.set(p),
      error: () => {
        this.alerta.error('No se encontró el pago.');
        this.router.navigate(['/pagos']);
      }
    });
  }

  async anular(): Promise<void> {
    const p = this.pago();
    if (!p) return;
    const ok = await this.alerta.confirmarEliminacion(
      `Se anulará el pago de ${p.inquilino} (Boleta #${p.boletaId}). La boleta volverá a estado Pendiente.`);
    if (!ok) return;

    this.servicio.eliminar(p.id!).subscribe({
      next: r => { this.alerta.exito(r.mensaje); this.router.navigate(['/pagos']); },
      error: err => this.alerta.error(err.error?.error ?? 'No se pudo anular el pago.')
    });
  }
}