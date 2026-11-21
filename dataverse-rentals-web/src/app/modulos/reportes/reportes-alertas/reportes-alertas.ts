import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ContratoPorVencer, Reportes } from '../reportes';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-reportes-alertas',
  imports: [RouterLink, DatePipe],
  templateUrl: './reportes-alertas.html'
})
export class ReportesAlertas implements OnInit {
  private servicio = inject(Reportes);
  private alerta = inject(Alerta);

  contratos = signal<ContratoPorVencer[]>([]);
  cargando = signal(true);

  ngOnInit(): void {
    this.servicio.contratosPorVencer().subscribe({
      next: datos => { this.contratos.set(datos); this.cargando.set(false); },
      error: () => { this.cargando.set(false); this.alerta.error('No se pudieron cargar las alertas.'); }
    });
  }
}