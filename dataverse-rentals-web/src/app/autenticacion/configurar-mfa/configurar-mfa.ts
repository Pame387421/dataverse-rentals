import { HttpClient } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { toDataURL } from 'qrcode';
import { environment } from '../../../environments/environment';
import { Autenticacion } from '../../nucleo/autenticacion/autenticacion';
import { Alerta } from '../../compartido/alerta';

@Component({
  selector: 'app-configurar-mfa',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './configurar-mfa.html'
})
export class ConfigurarMfa implements OnInit {
  private http = inject(HttpClient);
  private fb = inject(FormBuilder);
  private alerta = inject(Alerta);
  auth = inject(Autenticacion);

  private url = `${environment.apiUrl}/auth/mfa`;

  cargando = signal(true);
  yaActivo = signal(false);
  clave = signal('');
  qr = signal('');
  error = signal('');
  activando = signal(false);

  formulario = this.fb.nonNullable.group({
    codigo: ['', [Validators.required, Validators.pattern(/^[0-9]{6}$/)]]
  });

  ngOnInit(): void {
    this.http.post<{ clave: string; uri: string }>(`${this.url}/iniciar`, {}).subscribe({
      next: async r => {
        this.clave.set(r.clave);
        try {
          this.qr.set(await toDataURL(r.uri, { width: 240, margin: 1 }));
        } catch {
          // sin imagen QR, la clave manual sigue sirviendo
        }
        this.cargando.set(false);
      },
      error: err => {
        this.cargando.set(false);
        if (err.status === 409) this.yaActivo.set(true);
        else this.error.set(err.error?.error ?? 'No se pudo generar la clave de configuración.');
      }
    });
  }

  activar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    this.error.set('');
    this.activando.set(true);

    this.http.post<{ mensaje: string }>(`${this.url}/activar`, { codigo: this.formulario.getRawValue().codigo }).subscribe({
      next: r => {
        this.activando.set(false);
        this.yaActivo.set(true);
        this.alerta.exito(r.mensaje);
      },
      error: err => {
        this.activando.set(false);
        this.error.set(err.error?.error ?? 'No se pudo activar la verificación.');
      }
    });
  }
}