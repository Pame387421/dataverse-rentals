import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Medidores } from '../medidores';
import { Medidor } from '../../../nucleo/modelos/medidor';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-medidores-detalle',
  imports: [RouterLink],
  templateUrl: './medidores-detalle.html'
})
export class MedidoresDetalle implements OnInit {
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private servicio = inject(Medidores);
  private alerta = inject(Alerta);

  medidor = signal<Medidor | null>(null);

  ngOnInit(): void {
    const id = Number(this.ruta.snapshot.paramMap.get('id'));
    this.servicio.obtener(id).subscribe({
      next: m => this.medidor.set(m),
      error: () => {
        this.alerta.error('No se encontró el medidor.');
        this.router.navigate(['/medidores']);
      }
    });
  }

  async eliminar(): Promise<void> {
    const m = this.medidor();
    if (!m) return;
    const ok = await this.alerta.confirmarEliminacion(`Se eliminará el medidor ${m.numeroSerie} (${m.tipo}).`);
    if (!ok) return;

    this.servicio.eliminar(m.id!).subscribe({
      next: r => { this.alerta.exito(r.mensaje); this.router.navigate(['/medidores']); },
      error: err => this.alerta.error(err.error?.error ?? 'No se pudo eliminar el medidor.')
    });
  }
}