import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Medidores } from '../medidores';
import { TIPOS_MEDIDOR, TipoMedidor } from '../../../nucleo/modelos/medidor';
import { OpcionCombo } from '../../../nucleo/modelos/contrato';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-medidores-formulario',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './medidores-formulario.html'
})
export class MedidoresFormulario implements OnInit {
  private fb = inject(FormBuilder);
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private servicio = inject(Medidores);
  private alerta = inject(Alerta);

  id: number | null = null;
  guardando = signal(false);
  departamentos = signal<OpcionCombo[]>([]);
  tipos = TIPOS_MEDIDOR;

  formulario = this.fb.nonNullable.group({
    tipo: ['' as TipoMedidor | '', Validators.required],
    numeroSerie: ['', [Validators.required, Validators.maxLength(50)]],
    departamentoId: [0, [Validators.required, Validators.min(1)]]
  });

  get esEdicion(): boolean {
    return this.id !== null;
  }

  ngOnInit(): void {
    this.servicio.listarDepartamentosCombo().subscribe(l => this.departamentos.set(l));

    const idRuta = this.ruta.snapshot.paramMap.get('id');
    if (idRuta) {
      this.id = Number(idRuta);
      this.servicio.obtener(this.id).subscribe({
        next: m => this.formulario.patchValue(m),
        error: () => {
          this.alerta.error('No se encontró el medidor.');
          this.router.navigate(['/medidores']);
        }
      });
    }
  }

  invalido(campo: 'tipo' | 'numeroSerie' | 'departamentoId'): boolean {
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
    const datos = { ...v, tipo: v.tipo as TipoMedidor };

    const peticion = this.esEdicion
      ? this.servicio.actualizar(this.id!, datos)
      : this.servicio.crear(datos);

    peticion.subscribe({
      next: r => {
        this.alerta.exito(r.mensaje);
        this.router.navigate(['/medidores']);
      },
      error: err => {
        this.guardando.set(false);
        this.alerta.error(err.error?.error ?? 'No se pudo guardar el medidor.');
      }
    });
  }
}