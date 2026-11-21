import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Encuestas } from '../encuestas';
import { OpcionCombo } from '../../../nucleo/modelos/contrato';
import { Alerta } from '../../../compartido/alerta';
import { Autenticacion } from '../../../nucleo/autenticacion/autenticacion';

@Component({
  selector: 'app-encuestas-formulario',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './encuestas-formulario.html'
})
export class EncuestasFormulario implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private servicio = inject(Encuestas);
  private alerta = inject(Alerta);
  auth = inject(Autenticacion);

  guardando = signal(false);
  usuarios = signal<OpcionCombo[]>([]);

  formulario = this.fb.nonNullable.group({
    usuarioId: [0, [Validators.required, Validators.min(1)]],
    calificacion: [null as number | null, [Validators.required, Validators.min(1), Validators.max(5)]],
    comentario: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(500)]]
  });

  get volver(): string {
    return this.auth.esInquilino() ? '/mis-encuestas' : '/encuestas';
  }

  ngOnInit(): void {
    if (this.auth.esInquilino()) {
      this.formulario.controls.usuarioId.setValue(this.auth.sesion()!.usuarioId!);   // el inquilino responde por sí mismo
    } else {
      this.servicio.listarUsuariosCombo().subscribe(l => this.usuarios.set(l));
    }
  }

  invalido(campo: keyof typeof this.formulario.controls): boolean {
    const c = this.formulario.controls[campo];
    return c.touched && c.invalid;
  }

  guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    this.guardando.set(true);
    const v = this.formulario.getRawValue();

    this.servicio.crear({ usuarioId: v.usuarioId, calificacion: v.calificacion!, comentario: v.comentario }).subscribe({
      next: r => {
        this.alerta.exito(r.mensaje);
        this.router.navigate([this.volver]);
      },
      error: err => {
        this.guardando.set(false);
        this.alerta.error(err.error?.error ?? 'No se pudo registrar la encuesta.');
      }
    });
  }
}
