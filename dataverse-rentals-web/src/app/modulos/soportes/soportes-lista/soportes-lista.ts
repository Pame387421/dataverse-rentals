import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Soportes } from '../soportes';
import { Soporte } from '../../../nucleo/modelos/soporte';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-soportes-lista',
  imports: [RouterLink, DatePipe],
  templateUrl: './soportes-lista.html'
})
export class SoportesLista implements OnInit {
  private servicio = inject(Soportes);
  private alerta = inject(Alerta);

  tickets = signal<Soporte[]>([]);
  cargando = signal(true);

  // Métricas (equivalen a los contadores al inicio de tu index.jsp)
  casosEnEspera = computed(() => this.tickets().filter(t => t.estado === 'PENDIENTE').length);
  emergenciasCriticas = computed(() =>
    this.tickets().filter(t => t.prioridad === 'Alta' && t.estado !== 'RESUELTO').length);
  ticketsResueltos = computed(() => this.tickets().filter(t => t.estado === 'RESUELTO').length);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.servicio.listar().subscribe({
      next: datos => { this.tickets.set(datos); this.cargando.set(false); },
      error: () => { this.cargando.set(false); this.alerta.error('No se pudo cargar la lista de tickets.'); }
    });
  }

  async eliminar(t: Soporte): Promise<void> {
    const ok = await this.alerta.confirmarEliminacion(`Se eliminará el ticket #${t.id}: ${t.descripcion}`);
    if (!ok) return;

    this.servicio.eliminar(t.id!).subscribe({
      next: r => { this.alerta.exito(r.mensaje); this.cargar(); },
      error: err => this.alerta.error(err.error?.error ?? 'No se pudo eliminar el ticket.')
    });
  }
}