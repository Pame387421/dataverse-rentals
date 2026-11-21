import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Usuarios } from '../usuarios';
import { Usuario, tieneContratoPdf } from '../../../nucleo/modelos/usuario';
import { Alerta } from '../../../compartido/alerta';
import { VerContrato } from '../../../compartido/ver-contrato';

@Component({
  selector: 'app-usuarios-lista',
  imports: [RouterLink, VerContrato],
  templateUrl: './usuarios-lista.html'
})
export class UsuariosLista implements OnInit {
  private servicio = inject(Usuarios);
  private alerta = inject(Alerta);

  usuarios = signal<Usuario[]>([]);
  cargando = signal(true);
  tieneContrato = tieneContratoPdf;

  // Métricas (equivalen a los contadores al inicio de tu index.jsp)
  totalUsuarios = computed(() => this.usuarios().length);
  contratosDigitalizados = computed(() => this.usuarios().filter(u => tieneContratoPdf(u)).length);
  vouchersPorValidar = computed(() => this.usuarios().filter(u => !!u.urlVoucher?.trim()).length);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.servicio.listar().subscribe({
      next: datos => { this.usuarios.set(datos); this.cargando.set(false); },
      error: () => { this.cargando.set(false); this.alerta.error('No se pudo cargar la lista de usuarios.'); }
    });
  }

  async eliminar(u: Usuario): Promise<void> {
    const ok = await this.alerta.confirmarEliminacion(`Se eliminará al usuario "${u.nombre}" (${u.correo}).`);
    if (!ok) return;

    this.servicio.eliminar(u.id!).subscribe({
      next: r => { this.alerta.exito(r.mensaje); this.cargar(); },
      error: err => this.alerta.error(err.error?.error ?? 'No se pudo eliminar el usuario.')
    });
  }

    async generarAcceso(u: Usuario): Promise<void> {
    const reinicio = !!u.tieneCuenta;
    const ok = await this.alerta.confirmar(
      reinicio ? 'Reiniciar acceso' : 'Generar acceso',
      reinicio
        ? `Se generará una contraseña temporal nueva para ${u.correo}. La actual dejará de funcionar.`
        : `Se creará una cuenta para ${u.correo} con una contraseña temporal.`,
      reinicio ? 'Reiniciar' : 'Generar');
    if (!ok) return;

    // Si perdió el teléfono, también se puede desactivar su verificación en dos pasos
    let reiniciarMfa = false;
    if (reinicio && u.mfaActivo) {
      reiniciarMfa = await this.alerta.confirmar(
        'Verificación en dos pasos',
        '¿Desactivar también su verificación en dos pasos? Hazlo si perdió el teléfono. Podrá volver a activarla desde «Seguridad».',
        'Sí, desactivarla', 'No, mantenerla');
    }

    this.servicio.generarAcceso(u.id!, reiniciarMfa).subscribe({
      next: r => { this.alerta.claveTemporal(r.correo, r.claveTemporal); this.cargar(); },
      error: err => this.alerta.error(err.error?.error ?? 'No se pudo generar el acceso.')
    });
  }
}