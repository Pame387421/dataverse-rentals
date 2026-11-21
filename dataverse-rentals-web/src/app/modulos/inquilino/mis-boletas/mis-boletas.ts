import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Boletas } from '../../boletas/boletas';
import { Boleta, periodo } from '../../../nucleo/modelos/boleta';
import { Autenticacion } from '../../../nucleo/autenticacion/autenticacion';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-mis-boletas',
  imports: [RouterLink, DatePipe, DecimalPipe],
  templateUrl: './mis-boletas.html'
})
export class MisBoletas implements OnInit {
  private servicio = inject(Boletas);
  private auth = inject(Autenticacion);
  private alerta = inject(Alerta);

  boletas = signal<Boleta[]>([]);
  cargando = signal(true);
  etiquetaPeriodo = periodo;

  ngOnInit(): void {
    const usuarioId = this.auth.sesion()?.usuarioId;
    if (!usuarioId) {
      this.cargando.set(false);
      return;
    }
    this.servicio.listarDeUsuario(usuarioId).subscribe({
      next: datos => { this.boletas.set(datos); this.cargando.set(false); },
      error: () => { this.cargando.set(false); this.alerta.error('No se pudieron cargar tus boletas.'); }
    });
  }
}