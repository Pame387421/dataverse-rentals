import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { LecturasMedidor } from '../lecturas-medidor';
import { LecturaMedidor, unidadMedida } from '../../../nucleo/modelos/lectura-medidor';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-lecturas-medidor-lista',
  imports: [RouterLink, DatePipe, DecimalPipe],
  templateUrl: './lecturas-medidor-lista.html'
})
export class LecturasMedidorLista implements OnInit {
  private servicio = inject(LecturasMedidor);
  private alerta = inject(Alerta);

  lecturas = signal<LecturaMedidor[]>([]);
  cargando = signal(true);
  unidad = unidadMedida;

  // Métricas (equivalen a los cálculos al inicio de tu index.jsp)
  totalRegistros = computed(() => this.lecturas().length);
  ultimaActualizacion = computed(() => {
    const fechas = this.lecturas().map(l => l.fechaLectura).sort();
    return fechas.length > 0 ? fechas[fechas.length - 1] : null;
  });
  metricaMaxima = computed(() => this.lecturas().reduce((max, l) => Math.max(max, l.valor ?? 0), 0));

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.servicio.listar().subscribe({
      next: datos => { this.lecturas.set(datos); this.cargando.set(false); },
      error: () => { this.cargando.set(false); this.alerta.error('No se pudo cargar el historial de lecturas.'); }
    });
  }

  async eliminar(l: LecturaMedidor): Promise<void> {
    const ok = await this.alerta.confirmarEliminacion(
      `Se eliminará la lectura del medidor ${l.numeroSerie} (${l.valor} ${unidadMedida(l.tipo)}).`);
    if (!ok) return;

    this.servicio.eliminar(l.id!).subscribe({
      next: r => { this.alerta.exito(r.mensaje); this.cargar(); },
      error: err => this.alerta.error(err.error?.error ?? 'No se pudo eliminar la lectura.')
    });
  }
}