import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Mantenimientos } from '../mantenimientos';
import { Mantenimiento } from '../../../nucleo/modelos/mantenimiento';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-mantenimientos-detalle',
  imports: [RouterLink, DatePipe, DecimalPipe],
  templateUrl: './mantenimientos-detalle.html'
})
export class MantenimientosDetalle implements OnInit {
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private servicio = inject(Mantenimientos);
  private alerta = inject(Alerta);

  mantenimiento = signal<Mantenimiento | null>(null);

  ngOnInit(): void {
    const id = Number(this.ruta.snapshot.paramMap.get('id'));
    this.servicio.obtener(id).subscribe({
      next: m => this.mantenimiento.set(m),
      error: () => {
        this.alerta.error('No se encontró la orden de mantenimiento.');
        this.router.navigate(['/mantenimientos']);
      }
    });
  }

  async eliminar(): Promise<void> {
    const m = this.mantenimiento();
    if (!m) return;

    const ok = await this.alerta.confirmarEliminacion(`Se eliminará la orden #${m.id}.`);
    if (!ok) return;

    this.servicio.eliminar(m.id!).subscribe({
      next: r => { this.alerta.exito(r.mensaje); this.router.navigate(['/mantenimientos']); },
      error: err => this.alerta.error(err.error?.error ?? 'No se pudo eliminar la orden.')
    });
  }
}