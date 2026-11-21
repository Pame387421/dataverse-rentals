import { Directive, HostListener, inject, input } from '@angular/core';
import { Pagos } from '../modulos/pagos/pagos';
import { Alerta } from './alerta';

/** Uso: <button type="button" [appVerVoucher]="pago.id!">Ver PDF</button> */
@Directive({ selector: '[appVerVoucher]' })
export class VerVoucher {
  private pagos = inject(Pagos);
  private alerta = inject(Alerta);

  pagoId = input.required<number>({ alias: 'appVerVoucher' });

  @HostListener('click', ['$event'])
  abrir(evento: Event): void {
    evento.preventDefault();

    // La pestaña se abre ahora, dentro del clic, para que el navegador no la bloquee
    const pestana = window.open('', '_blank');

    this.pagos.verVoucher(this.pagoId()).subscribe({
      next: url => {
        if (pestana) pestana.location.href = url;
        else window.open(url, '_blank');
        setTimeout(() => URL.revokeObjectURL(url), 60_000);
      },
      error: err => {
        pestana?.close();
        this.alerta.error(err.error?.error ?? 'No se pudo abrir el comprobante.');
      }
    });
  }
}