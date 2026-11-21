import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Autenticacion } from '../../nucleo/autenticacion/autenticacion';

@Component({
  selector: 'app-verificar-2fa',
  imports: [ReactiveFormsModule],
  templateUrl: './verificar-2fa.html',
  styles: [`.tracking-widest { letter-spacing: 0.25em; font-size: 1.6rem; }`]
})
export class Verificar2fa implements OnInit {
  private fb = inject(FormBuilder);
  private auth = inject(Autenticacion);
  private router = inject(Router);

  error = signal('');
  cargando = signal(false);

  formulario = this.fb.nonNullable.group({
    token: ['', [Validators.required, Validators.pattern(/^[0-9]{6}$/)]]
  });

  ngOnInit(): void {
    // Igual que tu servlet: sin el paso 1 hecho, se vuelve al inicio de sesión
    if (!this.auth.tienePreautenticacion()) this.router.navigate(['/inicio-sesion']);
  }

  validar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    this.error.set('');
    this.cargando.set(true);

    this.auth.verificarMfa(this.formulario.getRawValue().token).subscribe({
      next: () => this.router.navigateByUrl(this.auth.rutaInicio()),
      error: err => {
        this.cargando.set(false);
        this.error.set(err.error?.error ?? 'No se pudo verificar el código.');
      }
    });
  }
}