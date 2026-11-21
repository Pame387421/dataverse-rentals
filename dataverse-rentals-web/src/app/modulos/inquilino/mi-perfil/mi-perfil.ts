import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Usuarios } from '../../usuarios/usuarios';
import { Usuario, tieneContratoPdf } from '../../../nucleo/modelos/usuario';
import { Alerta } from '../../../compartido/alerta';
import { VerContrato } from '../../../compartido/ver-contrato';

@Component({
  selector: 'app-mi-perfil',
  imports: [RouterLink, VerContrato],
  templateUrl: './mi-perfil.html'
})
export class MiPerfil implements OnInit {
  private servicio = inject(Usuarios);
  private alerta = inject(Alerta);

  usuario = signal<Usuario | null>(null);
  tieneContrato = tieneContratoPdf;

  ngOnInit(): void {
    this.servicio.obtenerPropio().subscribe({
      next: u => this.usuario.set(u),
      error: err => this.alerta.error(err.error?.error ?? 'Tu perfil administrativo de negocio no se encuentra vinculado.')
    });
  }
}