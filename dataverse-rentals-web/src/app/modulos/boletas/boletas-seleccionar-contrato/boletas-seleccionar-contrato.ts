import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Contratos } from '../../contratos/contratos';
import { Contrato } from '../../../nucleo/modelos/contrato';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-boletas-seleccionar-contrato',
  imports: [RouterLink],
  templateUrl: './boletas-seleccionar-contrato.html'
})
export class BoletasSeleccionarContrato implements OnInit {
  private servicio = inject(Contratos);
  private alerta = inject(Alerta);

  contratos = signal<Contrato[]>([]);
  cargando = signal(true);

  ngOnInit(): void {
    this.servicio.listar().subscribe({
      next: datos => { this.contratos.set(datos); this.cargando.set(false); },
      error: () => { this.cargando.set(false); this.alerta.error('No se pudo cargar la lista de contratos.'); }
    });
  }
}