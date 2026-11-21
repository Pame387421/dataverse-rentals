import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Contratos } from '../contratos';
import { OpcionCombo } from '../../../nucleo/modelos/contrato';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-contratos-formulario',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './contratos-formulario.html'
})
export class ContratosFormulario implements OnInit {
  private fb = inject(FormBuilder);
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private servicio = inject(Contratos);
  private alerta = inject(Alerta);

  id: number | null = null;
  guardando = signal(false);
  departamentos = signal<OpcionCombo[]>([]);
  usuarios = signal<OpcionCombo[]>([]);

  formulario = this.fb.nonNullable.group({
    departamentoId: [0, [Validators.required, Validators.min(1)]],
    usuarioId: [0, [Validators.required, Validators.min(1)]],
    fechaInicioContrato: ['', Validators.required],
    fechaFinContrato: ['', Validators.required],
    montoAlquiler: [0, [Validators.required, Validators.min(0.01)]],
    urlVoucher: ['', [Validators.maxLength(500), Validators.pattern(/^https?:\/\/.+/)]]
  });

  get esEdicion(): boolean {
    return this.id !== null;
  }

  ngOnInit(): void {
    this.servicio.listarDepartamentosCombo().subscribe(l => this.departamentos.set(l));
    this.servicio.listarUsuariosCombo().subscribe(l => this.usuarios.set(l));

    const idRuta = this.ruta.snapshot.paramMap.get('id');
    if (idRuta) {
      this.id = Number(idRuta);
      this.servicio.obtener(this.id).subscribe({
        next: c => this.formulario.patchValue(c),
        error: () => {
          this.alerta.error('No se encontró el contrato.');
          this.router.navigate(['/contratos']);
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
    const { fechaInicioContrato, fechaFinContrato } = this.formulario.getRawValue();
    if (fechaFinContrato < fechaInicioContrato) {
      this.alerta.error('La fecha de finalización no puede ser anterior a la de inicio.');
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
        this.router.navigate(['/contratos']);
      },
      error: err => {
        this.guardando.set(false);
        this.alerta.error(err.error?.error ?? 'No se pudo guardar el contrato.');
      }
    });
  }
}