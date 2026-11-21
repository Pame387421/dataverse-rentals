import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Edificios } from '../edificios';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-edificios-formulario',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './edificios-formulario.html'
})
export class EdificiosFormulario implements OnInit {
  private fb = inject(FormBuilder);
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private servicio = inject(Edificios);
  private alerta = inject(Alerta);

  id: number | null = null;
  guardando = signal(false);

  formulario = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    direccion: ['', [Validators.required, Validators.maxLength(200)]],
        pisos: [1, [Validators.required, Validators.min(1), Validators.max(100)]]
  });

  get esEdicion(): boolean {
    return this.id !== null;
  }

  ngOnInit(): void {
    const idRuta = this.ruta.snapshot.paramMap.get('id');
    if (idRuta) {
      this.id = Number(idRuta);
      this.servicio.obtener(this.id).subscribe({
        next: e => this.formulario.patchValue(e),
        error: () => {
          this.alerta.error('No se encontró el edificio.');
          this.router.navigate(['/edificios']);
        }
      });
    }
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
        this.router.navigate(['/edificios']);
      },
      error: err => {
        this.guardando.set(false);
        this.alerta.error(err.error?.error ?? 'No se pudo guardar el edificio.');
      }
    });
  }
}