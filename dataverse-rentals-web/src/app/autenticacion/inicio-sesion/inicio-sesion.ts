import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Autenticacion } from '../../nucleo/autenticacion/autenticacion';

@Component({
  selector: 'app-inicio-sesion',
  imports: [ReactiveFormsModule],
  templateUrl: './inicio-sesion.html'
})
export class InicioSesion {
  private fb = inject(FormBuilder);
  private auth = inject(Autenticacion);
  private router = inject(Router);

  error = signal('');
  cargando = signal(false);

  formulario = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  invalido(campo: 'email' | 'password'): boolean {
    const c = this.formulario.controls[campo];
    return c.touched && c.invalid;
  }

    ingresar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    this.error.set('');
    this.cargando.set(true);

    const { email, password } = this.formulario.getRawValue();
    this.auth.iniciarSesion(email, password).subscribe({
      next: r => this.router.navigate([r.requiereMfa ? '/verificar-2fa' : this.auth.rutaInicio()]),
      error: err => {
        this.cargando.set(false);
        this.error.set(err.error?.error ?? 'No se pudo iniciar sesión.');
      }
    });
  }
}

