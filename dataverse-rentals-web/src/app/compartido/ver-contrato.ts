import { Directive, HostListener, inject, input } from '@angular/core';
import { Usuarios } from '../modulos/usuarios/usuarios';
import { Alerta } from './alerta';

/** Uso: <button type="button" [appVerContrato]="usuario.id!">Ver contrato</button> */
@Directive({ selector: '[appVerContrato]' })
export class VerContrato {
  private usuarios = inject(Usuarios);
  private alerta = inject(Alerta);

  usuarioId = input.required<number>({ alias: 'appVerContrato' });

  @HostListener('click', ['$event'])
  abrir(evento: Event): void {
    evento.preventDefault();

    // La pestaña se abre ahora, dentro del clic, para que el navegador no la bloquee
    const pestana = window.open('', '_blank');

    this.usuarios.verContrato(this.usuarioId()).subscribe({
      next: url => {
        if (pestana) pestana.location.href = url;
        else window.open(url, '_blank');
        setTimeout(() => URL.revokeObjectURL(url), 60_000);
      },
      error: err => {
        pestana?.close();
        this.alerta.error(err.error?.error ?? 'No se pudo abrir el contrato.');
      }
    });
  }
}