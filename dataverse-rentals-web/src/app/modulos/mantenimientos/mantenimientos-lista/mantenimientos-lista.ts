import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Mantenimientos } from '../mantenimientos';
import { Mantenimiento } from '../../../nucleo/modelos/mantenimiento';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-mantenimientos-lista',
  imports: [RouterLink, DatePipe, DecimalPipe],
  templateUrl: './mantenimientos-lista.html'
})
export class MantenimientosLista implements OnInit {
  private servicio = inject(Mantenimientos);
  private alerta = inject(Alerta);

  mantenimientos = signal<Mantenimiento[]>([]);
  cargando = signal(true);

  // Tarjetas de métricas
  ordenesPendientes = computed(() => this.mantenimientos().filter(m => m.estado !== 'FINALIZADO').length);
  inversionTotal = computed(() => this.mantenimientos().reduce((s, m) => s + m.costoTotal, 0));
  costoMaximo = computed(() => this.mantenimientos().reduce((max, m) => Math.max(max, m.costoTotal), 0));

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.servicio.listar().subscribe({
      next: datos => { this.mantenimientos.set(datos); this.cargando.set(false); },
      error: () => { this.cargando.set(false); this.alerta.error('No se pudo cargar la lista de mantenimientos.'); }
    });
  }

  async eliminar(m: Mantenimiento): Promise<void> {
    const ok = await this.alerta.confirmarEliminacion(`Se eliminará la orden #${m.id}: ${m.descripcion}`);
    if (!ok) return;

    this.servicio.eliminar(m.id!).subscribe({
      next: r => { this.alerta.exito(r.mensaje); this.cargar(); },
      error: err => this.alerta.error(err.error?.error ?? 'No se pudo eliminar la orden.')
    });
  }
}