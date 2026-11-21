import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Usuarios } from '../../usuarios/usuarios';
import { TIPOS_DOCUMENTO, TipoDocumento } from '../../../nucleo/modelos/usuario';
import { Autenticacion } from '../../../nucleo/autenticacion/autenticacion';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-mi-perfil-editar',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './mi-perfil-editar.html'
})
export class MiPerfilEditar implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private servicio = inject(Usuarios);
  private auth = inject(Autenticacion);
  private alerta = inject(Alerta);

  guardando = signal(false);
  correo = signal('');
  tiposDocumento = TIPOS_DOCUMENTO;

  formulario = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)]],
    tipoDocumento: ['DNI' as TipoDocumento, Validators.required],
    numeroDocumento: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(20)]]
  });

  ngOnInit(): void {
    this.servicio.obtenerPropio().subscribe({
      next: u => {
        this.correo.set(u.correo);
        this.formulario.patchValue({
          nombre: u.nombre, tipoDocumento: u.tipoDocumento, numeroDocumento: u.numeroDocumento
        });
      },
      error: () => {
        this.alerta.error('No se encontró tu perfil.');
        this.router.navigate(['/mi-perfil']);
      }
    });
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
    const datos = this.formulario.getRawValue();
    this.servicio.actualizarPerfil(datos).subscribe({
      next: r => {
        this.auth.actualizarNombre(datos.nombre);
        this.alerta.exito(r.mensaje);
        this.router.navigate(['/mi-perfil']);
      },
      error: err => {
        this.guardando.set(false);
        this.alerta.error(err.error?.error ?? 'No se pudo actualizar el perfil.');
      }
    });
  }
}