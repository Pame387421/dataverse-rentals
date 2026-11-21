import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Soportes } from '../soportes';
import { Soporte } from '../../../nucleo/modelos/soporte';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-soportes-detalle',
  imports: [RouterLink, DatePipe],
  templateUrl: './soportes-detalle.html'
})
export class SoportesDetalle implements OnInit {
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private servicio = inject(Soportes);
  private alerta = inject(Alerta);

  ticket = signal<Soporte | null>(null);

  ngOnInit(): void {
    const id = Number(this.ruta.snapshot.paramMap.get('id'));
    this.servicio.obtener(id).subscribe({
      next: s => this.ticket.set(s),
      error: () => {
        this.alerta.error('No se encontró el ticket.');
        this.router.navigate(['/soporte']);
      }
    });
  }

  async eliminar(): Promise<void> {
    const t = this.ticket();
    if (!t) return;

    const ok = await this.alerta.confirmarEliminacion(`Se eliminará el ticket #${t.id}: ${t.descripcion}`);
    if (!ok) return;

    this.servicio.eliminar(t.id!).subscribe({
      next: r => { this.alerta.exito(r.mensaje); this.router.navigate(['/soporte']); },
      error: err => this.alerta.error(err.error?.error ?? 'No se pudo eliminar el ticket.')
    });
  }
}