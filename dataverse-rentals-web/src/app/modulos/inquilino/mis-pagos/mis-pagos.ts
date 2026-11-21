import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Pagos } from '../../pagos/pagos';
import { Pago } from '../../../nucleo/modelos/pago';
import { Autenticacion } from '../../../nucleo/autenticacion/autenticacion';
import { Alerta } from '../../../compartido/alerta';
import { VerVoucher } from '../../../compartido/ver-voucher';

@Component({
  selector: 'app-mis-pagos',
  imports: [RouterLink, DatePipe, DecimalPipe, VerVoucher],
  templateUrl: './mis-pagos.html'
})
export class MisPagos implements OnInit {
  private servicio = inject(Pagos);
  private auth = inject(Autenticacion);
  private alerta = inject(Alerta);

  pagos = signal<Pago[]>([]);
  cargando = signal(true);

  ngOnInit(): void {
    const usuarioId = this.auth.sesion()?.usuarioId;
    if (!usuarioId) {
      this.cargando.set(false);
      return;
    }
    this.servicio.listarDeUsuario(usuarioId).subscribe({
      next: datos => { this.pagos.set(datos); this.cargando.set(false); },
      error: () => { this.cargando.set(false); this.alerta.error('No se pudieron cargar tus pagos.'); }
    });
  }
}