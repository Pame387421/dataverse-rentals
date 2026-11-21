import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Boletas } from '../boletas';
import { Pagos } from '../../pagos/pagos';
import { Boleta } from '../../../nucleo/modelos/boleta';
import { Pago } from '../../../nucleo/modelos/pago';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-boletas-detalle',
  imports: [RouterLink, DatePipe, DecimalPipe],
  templateUrl: './boletas-detalle.html'
})
export class BoletasDetalle implements OnInit {
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private servicio = inject(Boletas);
  private servicioPagos = inject(Pagos);
  private alerta = inject(Alerta);

  boleta = signal<Boleta | null>(null);
  pago = signal<Pago | null>(null);

  /** Rubros del pago (equivale a ViewBag.Pago.Detalles de tu details.jsp). */
  rubrosPago = computed(() => {
    const p = this.pago();
    if (!p) return [];
    return [
      { concepto: 'Consumo de Luz', monto: p.consumoLuz },
      { concepto: 'Consumo de Agua', monto: p.consumoAgua },
      { concepto: 'Consumo de Gas', monto: p.consumoGas },
      { concepto: 'Arbitrios Municipales', monto: p.arbitrios }
    ].filter(r => r.monto > 0);
  });

  ngOnInit(): void {
    const id = Number(this.ruta.snapshot.paramMap.get('id'));
    forkJoin({ boleta: this.servicio.obtener(id), pagos: this.servicioPagos.listar() }).subscribe({
      next: ({ boleta, pagos }) => {
        this.boleta.set(boleta);
        this.pago.set(pagos.find(p => p.boletaId === id) ?? null);
      },
      error: () => {
        this.alerta.error('No se encontró la boleta.');
        this.router.navigate(['/boletas']);
      }
    });
  }

  async anular(): Promise<void> {
    const b = this.boleta();
    if (!b) return;
    const ok = await this.alerta.confirmarEliminacion(
      `Se eliminará la boleta #${b.id} de ${b.inquilino}. El pago asociado también será eliminado.`);
    if (!ok) return;

    this.servicio.eliminar(b.id!).subscribe({
      next: r => { this.alerta.exito(r.mensaje); this.router.navigate(['/boletas']); },
      error: err => this.alerta.error(err.error?.error ?? 'No se pudo eliminar la boleta.')
    });
  }
}