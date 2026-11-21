import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Encuestas } from '../encuestas';
import { Encuesta } from '../../../nucleo/modelos/encuesta';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-encuestas-lista',
  imports: [RouterLink, DatePipe, DecimalPipe],
  templateUrl: './encuestas-lista.html'
})
export class EncuestasLista implements OnInit {
  private servicio = inject(Encuestas);
  private alerta = inject(Alerta);

  encuestas = signal<Encuesta[]>([]);
  cargando = signal(true);
  estrellas = [1, 2, 3, 4, 5];

  // Métricas (equivalen a los cálculos al inicio de tu index.jsp)
  totalFeedbacks = computed(() => this.encuestas().length);
  calificacionesExcelentes = computed(() => this.encuestas().filter(e => e.calificacion === 5).length);
  promedioSatisfaccion = computed(() => {
    const total = this.totalFeedbacks();
    return total > 0 ? this.encuestas().reduce((s, e) => s + e.calificacion, 0) / total : 0;
  });

  ngOnInit(): void {
    this.servicio.listar().subscribe({
      next: datos => { this.encuestas.set(datos); this.cargando.set(false); },
      error: () => { this.cargando.set(false); this.alerta.error('No se pudo cargar la lista de encuestas.'); }
    });
  }
}