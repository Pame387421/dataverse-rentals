import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Pagos } from '../pagos';
import { BoletaPendiente } from '../../../nucleo/modelos/pago';
import { Alerta } from '../../../compartido/alerta';
import { Autenticacion } from '../../../nucleo/autenticacion/autenticacion';

/** Fecha y hora actual en formato yyyy-MM-ddTHH:mm (para el campo datetime-local). */
function ahoraLocal(): string {
  const f = new Date();
  const p = (n: number) => String(n).padStart(2, '0');
  return `${f.getFullYear()}-${p(f.getMonth() + 1)}-${p(f.getDate())}T${p(f.getHours())}:${p(f.getMinutes())}`;
}

@Component({
  selector: 'app-pagos-formulario',
  imports: [ReactiveFormsModule, RouterLink, DecimalPipe],
  templateUrl: './pagos-formulario.html'
})
export class PagosFormulario implements OnInit {
  private fb = inject(FormBuilder);
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private servicio = inject(Pagos);
  private alerta = inject(Alerta);
  private auth = inject(Autenticacion);

  guardando = signal(false);
  boletas = signal<BoletaPendiente[]>([]);
  boleta = signal<BoletaPendiente | null>(null);
  abonado = signal(0);
  errorArchivo = signal('');
  archivo: File | null = null;
  fechaPago = ahoraLocal();

  /** A dónde vuelve cada rol. */
  get volver(): string {
    return this.auth.esInquilino() ? '/mis-pagos' : '/pagos';
  }

  /** Saldo que se recalcula al escribir el abono (igual que el script de tu create.jsp). */
  saldo = computed(() => {
    const b = this.boleta();
        return b ? Math.round((b.saldoPendiente - this.abonado()) * 100) / 100 : 0;
  });

  formulario = this.fb.nonNullable.group({
    boletaId: [0, [Validators.required, Validators.min(1)]],
    montoAbonado: [0, [Validators.required, Validators.min(0.01)]],
    urlVoucher: ['', [Validators.maxLength(500), Validators.pattern(/^https?:\/\/.+/)]]
  });

  ngOnInit(): void {
    const origen = this.auth.esInquilino()
      ? this.servicio.listarBoletasPendientesDeUsuario(this.auth.sesion()!.usuarioId!)
      : this.servicio.listarBoletasPendientes();
      
    origen.subscribe(l => this.boletas.set(l));

    this.formulario.controls.boletaId.valueChanges.subscribe(id => this.alCambiarBoleta(id));
    this.formulario.controls.montoAbonado.valueChanges.subscribe(v => this.abonado.set(v ?? 0));

    // Permite llegar desde Boletas con /pagos/nuevo?boletaId=3
    const param = this.ruta.snapshot.queryParamMap.get('boletaId');
    if (param) this.formulario.patchValue({ boletaId: Number(param) });
  }

  private alCambiarBoleta(id: number): void {
    const monto = this.formulario.controls.montoAbonado;
    if (!id) {
      this.boleta.set(null);
      monto.setValidators([Validators.required, Validators.min(0.01)]);
      monto.updateValueAndValidity();
      return;
    }
    this.servicio.obtenerBoleta(id).subscribe(b => {
      this.boleta.set(b);
      monto.setValidators([Validators.required, Validators.min(0.01), Validators.max(b.saldoPendiente)]);
      monto.updateValueAndValidity();
    });
  }

  alElegirArchivo(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    const f = input.files?.[0] ?? null;
    if (f && f.type !== 'application/pdf') {
      this.errorArchivo.set('Solo se permiten archivos PDF.');
      input.value = '';
      this.archivo = null;
      return;
    }
    this.errorArchivo.set('');
    this.archivo = f;
  }

  invalido(campo: 'boletaId' | 'montoAbonado' | 'urlVoucher'): boolean {
    const c = this.formulario.controls[campo];
    return c.touched && c.invalid;
  }

  guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    // Regla agregada: solo se puede pagar una boleta de la lista (las del propio inquilino)
    if (!this.boletas().some(b => b.id === this.formulario.getRawValue().boletaId)) {
      this.alerta.error('La boleta seleccionada no está disponible para pago.');
      return;
    }

    this.guardando.set(true);
    const v = this.formulario.getRawValue();

    this.servicio.crear({
      boletaId: v.boletaId,
      montoAbonado: v.montoAbonado,
      archivoPdf: this.archivo,
      urlVoucher: v.urlVoucher
    }).subscribe({
      next: r => {
        this.alerta.exito(r.mensaje);
        this.router.navigate([this.volver]);
      },
      error: err => {
        this.guardando.set(false);
        this.alerta.error(err.error?.error ?? 'No se pudo registrar el pago.');
      }
    });
  }
}
