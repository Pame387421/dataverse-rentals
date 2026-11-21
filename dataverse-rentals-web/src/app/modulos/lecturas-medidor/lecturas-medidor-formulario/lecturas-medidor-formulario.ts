import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { LecturasMedidor } from '../lecturas-medidor';
import { OpcionCombo } from '../../../nucleo/modelos/contrato';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-lecturas-medidor-formulario',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './lecturas-medidor-formulario.html'
})
export class LecturasMedidorFormulario implements OnInit {
  private fb = inject(FormBuilder);
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private servicio = inject(LecturasMedidor);
  private alerta = inject(Alerta);

  id: number | null = null;
  guardando = signal(false);
  medidores = signal<OpcionCombo[]>([]);

  formulario = this.fb.nonNullable.group({
    medidorId: [0, [Validators.required, Validators.min(1)]],
    fechaLectura: ['', Validators.required],
    valor: [0, [Validators.required, Validators.min(0)]]
  });

  get esEdicion(): boolean {
    return this.id !== null;
  }

  ngOnInit(): void {
    this.servicio.listarMedidoresCombo().subscribe(l => this.medidores.set(l));

    const idRuta = this.ruta.snapshot.paramMap.get('id');
    if (idRuta) {
      this.id = Number(idRuta);
      this.servicio.obtener(this.id).subscribe({
        next: l => this.formulario.patchValue(l),
        error: () => {
          this.alerta.error('No se encontró la lectura.');
          this.router.navigate(['/lecturas']);
        }
      });
    }
  }

  invalido(campo: 'medidorId' | 'fechaLectura' | 'valor'): boolean {
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
        this.router.navigate(['/lecturas']);
      },
      error: err => {
        this.guardando.set(false);
        this.alerta.error(err.error?.error ?? 'No se pudo guardar la lectura.');
      }
    });
  }
}