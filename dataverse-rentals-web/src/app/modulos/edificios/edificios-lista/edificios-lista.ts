import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Edificios } from '../edificios';
import { Edificio } from '../../../nucleo/modelos/edificio';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-edificios-lista',
  imports: [RouterLink, DecimalPipe],
  templateUrl: './edificios-lista.html'
})
export class EdificiosLista implements OnInit {
  private servicio = inject(Edificios);
  private alerta = inject(Alerta);

  edificios = signal<Edificio[]>([]);
  cargando = signal(true);

  // Métricas (equivalen a los cálculos al inicio de tu index.jsp)
  totalComplejos = computed(() => this.edificios().length);
  totalDepartamentos = computed(() =>
    this.edificios().reduce((s, e) => s + (e.cantidadDepartamentos ?? 0), 0));
  promedioCapacidad = computed(() =>
    this.totalComplejos() > 0 ? this.totalDepartamentos() / this.totalComplejos() : 0);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.servicio.listar().subscribe({
      next: datos => { this.edificios.set(datos); this.cargando.set(false); },
      error: () => { this.cargando.set(false); this.alerta.error('No se pudo cargar la lista de edificios.'); }
    });
  }

  async eliminar(edificio: Edificio): Promise<void> {
    const confirmado = await this.alerta.confirmarEliminacion(`Se eliminará el edificio "${edificio.nombre}".`);
    if (!confirmado) return;

    this.servicio.eliminar(edificio.id!).subscribe({
      next: r => { this.alerta.exito(r.mensaje); this.cargar(); },
      error: err => this.alerta.error(err.error?.error ?? 'No se pudo eliminar el edificio.')
    });
  }
}