import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Encuestas } from '../../encuestas/encuestas';
import { Encuesta } from '../../../nucleo/modelos/encuesta';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-mis-encuestas',
  imports: [RouterLink, DatePipe],
  templateUrl: './mis-encuestas.html'
})
export class MisEncuestas implements OnInit {
  private servicio = inject(Encuestas);
  private alerta = inject(Alerta);

  encuestas = signal<Encuesta[]>([]);
  cargando = signal(true);
  estrellas = [1, 2, 3, 4, 5];

  ngOnInit(): void {
    this.servicio.listarMias().subscribe({
      next: datos => { this.encuestas.set(datos); this.cargando.set(false); },
      error: () => { this.cargando.set(false); this.alerta.error('No se pudo cargar tu historial.'); }
    });
  }
}