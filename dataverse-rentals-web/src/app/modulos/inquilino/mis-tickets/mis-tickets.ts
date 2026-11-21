import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Soportes } from '../../soportes/soportes';
import { Soporte } from '../../../nucleo/modelos/soporte';
import { Autenticacion } from '../../../nucleo/autenticacion/autenticacion';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-mis-tickets',
  imports: [RouterLink, DatePipe],
  templateUrl: './mis-tickets.html'
})
export class MisTickets implements OnInit {
  private servicio = inject(Soportes);
  private auth = inject(Autenticacion);
  private alerta = inject(Alerta);

  tickets = signal<Soporte[]>([]);
  cargando = signal(true);

  ngOnInit(): void {
    const usuarioId = this.auth.sesion()?.usuarioId;
    if (!usuarioId) {
      this.cargando.set(false);
      return;
    }
    this.servicio.listarDeUsuario(usuarioId).subscribe({
      next: datos => { this.tickets.set(datos); this.cargando.set(false); },
      error: () => { this.cargando.set(false); this.alerta.error('No se pudieron cargar tus tickets.'); }
    });
  }
}