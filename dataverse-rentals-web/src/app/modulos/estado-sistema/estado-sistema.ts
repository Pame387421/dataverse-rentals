import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Diagnostico, EstadoSistemaDatos } from './diagnostico';
import { Alerta } from '../../compartido/alerta';

@Component({
  selector: 'app-estado-sistema',
  imports: [DatePipe],
  templateUrl: './estado-sistema.html'
})
export class EstadoSistema implements OnInit {
  private servicio = inject(Diagnostico);
  private alerta = inject(Alerta);

  estado = signal<EstadoSistemaDatos | null>(null);
  cargando = signal(false);

  dbOk = computed(() => this.estado()?.baseDatosConectada ?? false);
  esOperativo = computed(() => this.estado()?.estadoGeneral.toLowerCase() === 'operativo');

  ngOnInit(): void {
    this.chequear();
  }

  chequear(): void {
    this.cargando.set(true);
    this.servicio.chequear().subscribe({
      next: e => { this.estado.set(e); this.cargando.set(false); },
      error: () => { this.cargando.set(false); this.alerta.error('No se pudo completar el diagnóstico.'); }
    });
  }
}