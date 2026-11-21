import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Usuarios } from '../usuarios';
import { Usuario, tieneContratoPdf } from '../../../nucleo/modelos/usuario';
import { Alerta } from '../../../compartido/alerta';
import { VerContrato } from '../../../compartido/ver-contrato';

@Component({
  selector: 'app-usuarios-detalle',
  imports: [RouterLink, VerContrato],
  templateUrl: './usuarios-detalle.html'
})
export class UsuariosDetalle implements OnInit {
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private servicio = inject(Usuarios);
  private alerta = inject(Alerta);

  usuario = signal<Usuario | null>(null);
  tieneContrato = tieneContratoPdf;

  ngOnInit(): void {
    const id = Number(this.ruta.snapshot.paramMap.get('id'));
    this.servicio.obtener(id).subscribe({
      next: u => this.usuario.set(u),
      error: () => {
        this.alerta.error('No se encontró el usuario.');
        this.router.navigate(['/usuarios']);
      }
    });
  }
}