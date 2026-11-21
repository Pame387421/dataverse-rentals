import { Component, computed, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Autenticacion } from '../../nucleo/autenticacion/autenticacion';
import { Alerta } from '../../compartido/alerta';

function coinciden(g: AbstractControl): ValidationErrors | null {
  return g.get('claveNueva')?.value === g.get('confirmar')?.value ? null : { noCoinciden: true };
}

@Component({
  selector: 'app-cambiar-clave',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './cambiar-clave.html'
})
export class CambiarClave {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private alerta = inject(Alerta);
  auth = inject(Autenticacion);

  guardando = signal(false);
  error = signal('');
  forzado = computed(() => !!this.auth.sesion()?.debeCambiarClave);

  formulario = this.fb.nonNullable.group({
    claveActual: ['', Validators.required],
    claveNueva: ['', [
      Validators.required, Validators.minLength(10), Validators.maxLength(100),
      Validators.pattern(/^(?=.*[A-Za-z])(?=.*\d).+$/)
    ]],
    confirmar: ['', Validators.required]
  }, { validators: coinciden });

  invalido(campo: 'claveActual' | 'claveNueva' | 'confirmar'): boolean {
    const c = this.formulario.controls[campo];
    return c.touched && c.invalid;
  }

  guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    this.error.set('');
    this.guardando.set(true);

    const { claveActual, claveNueva } = this.formulario.getRawValue();
    this.auth.cambiarClave(claveActual, claveNueva).subscribe({
      next: () => {
        this.alerta.exito('Contraseña actualizada correctamente.');
        this.router.navigateByUrl(this.auth.rutaInicio());
      },
      error: err => {
        this.guardando.set(false);
        this.error.set(err.error?.error ?? 'No se pudo cambiar la contraseña.');
      }
    });
  }
}