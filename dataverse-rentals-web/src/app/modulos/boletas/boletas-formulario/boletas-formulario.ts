import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Boletas } from '../boletas';
import { BoletaPrevia } from '../../../nucleo/modelos/boleta';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-boletas-formulario',
  imports: [ReactiveFormsModule, RouterLink, DecimalPipe],
  templateUrl: './boletas-formulario.html'
})
export class BoletasFormulario implements OnInit {
  private fb = inject(FormBuilder);
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private servicio = inject(Boletas);
  private alerta = inject(Alerta);

  previa = signal<BoletaPrevia | null>(null);
  guardando = signal(false);
  errorServidor = signal('');

  formulario = this.fb.nonNullable.group({
    fechaEmision: ['', Validators.required],
    fechaVencimiento: ['', Validators.required]
  });

  ngOnInit(): void {
    const contratoId = Number(this.ruta.snapshot.paramMap.get('contratoId'));
    this.servicio.previsualizar(contratoId).subscribe({
      next: v => {
        this.previa.set(v);
        this.formulario.patchValue({ fechaEmision: v.fechaEmision, fechaVencimiento: v.fechaVencimiento });
      },
      error: () => {
        this.alerta.error('No se encontró el contrato.');
        this.router.navigate(['/boletas/seleccionar-contrato']);
      }
    });
  }

  invalido(campo: 'fechaEmision' | 'fechaVencimiento'): boolean {
    const c = this.formulario.controls[campo];
    return c.touched && c.invalid;
  }

  guardar(): void {
    const v = this.previa();
    if (!v) return;
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const fechas = this.formulario.getRawValue();
    if (fechas.fechaVencimiento < fechas.fechaEmision) {
      this.errorServidor.set('La fecha de vencimiento no puede ser anterior a la fecha de emisión.');
      return;
    }

    this.errorServidor.set('');
    this.guardando.set(true);
    this.servicio.crear({ ...v, ...fechas }).subscribe({
      next: r => {
        this.alerta.exito(r.mensaje);
        this.router.navigate(['/boletas']);
      },
      error: err => {
        this.guardando.set(false);
        this.errorServidor.set(err.error?.error ?? 'Error interno al procesar la emisión de la boleta.');
      }
    });
  }
}