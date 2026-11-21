import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Proveedores } from '../proveedores';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-proveedores-formulario',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './proveedores-formulario.html'
})
export class ProveedoresFormulario implements OnInit {
  private fb = inject(FormBuilder);
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private servicio = inject(Proveedores);
  private alerta = inject(Alerta);

  id: number | null = null;
  guardando = signal(false);

  formulario = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)]],
    // El RUC peruano tiene exactamente 11 dígitos, como en tus JSP
    ruc: ['', [Validators.required, Validators.pattern(/^[0-9]{11}$/)]],
    correo: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
    telefono: ['', [Validators.required, Validators.maxLength(20)]]
  });

  get esEdicion(): boolean {
    return this.id !== null;
  }

  ngOnInit(): void {
    const idRuta = this.ruta.snapshot.paramMap.get('id');
    if (idRuta) {
      this.id = Number(idRuta);
      this.servicio.obtener(this.id).subscribe({
        next: p => this.formulario.patchValue({
          nombre: p.nombre, ruc: p.ruc, correo: p.correo, telefono: p.telefono
        }),
        error: () => {
          this.alerta.error('No se encontró el proveedor.');
          this.router.navigate(['/proveedores']);
        }
      });
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
    const datos = this.formulario.getRawValue();

    const peticion = this.esEdicion
      ? this.servicio.actualizar(this.id!, datos)
      : this.servicio.crear(datos);

    peticion.subscribe({
      next: r => {
        this.alerta.exito(r.mensaje);
        this.router.navigate(['/proveedores']);
      },
      error: err => {
        this.guardando.set(false);
        this.alerta.error(err.error?.error ?? 'No se pudo guardar el proveedor.');
      }
    });
  }
}