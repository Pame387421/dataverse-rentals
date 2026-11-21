import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Departamentos } from '../departamentos';
import { Edificios } from '../../edificios/edificios';
import { Edificio } from '../../../nucleo/modelos/edificio';
import { ESTADOS_ACABADOS } from '../../../nucleo/modelos/departamento';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-departamentos-formulario',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './departamentos-formulario.html'
})
export class DepartamentosFormulario implements OnInit {
  private fb = inject(FormBuilder);
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private servicio = inject(Departamentos);
  private servicioEdificios = inject(Edificios);
  private alerta = inject(Alerta);

  id: number | null = null;
  guardando = signal(false);
  edificios = signal<Edificio[]>([]);
  estadosAcabados = ESTADOS_ACABADOS;

  formulario = this.fb.nonNullable.group({
    edificioId: [0, [Validators.required, Validators.min(1)]],
    piso: [1, [Validators.required, Validators.min(1), Validators.max(100)]],
    numeroDepartamento: ['', [Validators.required, Validators.maxLength(20)]],
    areaM2: [0, [Validators.required, Validators.min(0.01)]],
    estadoAcabados: ['', Validators.required]
  });

  get esEdicion(): boolean {
    return this.id !== null;
  }

  ngOnInit(): void {
    // Carga de edificios para el desplegable
    this.servicioEdificios.listar().subscribe(lista => this.edificios.set(lista));

    const idRuta = this.ruta.snapshot.paramMap.get('id');
    if (idRuta) {
      this.id = Number(idRuta);
      this.servicio.obtener(this.id).subscribe({
        next: d => this.formulario.patchValue(d),
        error: () => {
          this.alerta.error('No se encontró el departamento.');
          this.router.navigate(['/departamentos']);
        }
      });
    }
  }

  invalido(campo: 'edificioId' | 'piso' | 'numeroDepartamento' | 'areaM2' | 'estadoAcabados'): boolean {
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
        this.router.navigate(['/departamentos']);
      },
      error: err => {
        this.guardando.set(false);
        this.alerta.error(err.error?.error ?? 'No se pudo guardar el departamento.');
      }
    });
  }
}