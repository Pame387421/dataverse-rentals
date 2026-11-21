import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Soportes } from '../soportes';
import { OpcionCombo } from '../../../nucleo/modelos/contrato';
import { EstadoSoporte, PrioridadSoporte } from '../../../nucleo/modelos/soporte';
import { Alerta } from '../../../compartido/alerta';
import { Autenticacion } from '../../../nucleo/autenticacion/autenticacion';

@Component({
  selector: 'app-soportes-formulario',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './soportes-formulario.html'
})
export class SoportesFormulario implements OnInit {
  private fb = inject(FormBuilder);
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private servicio = inject(Soportes);
  private alerta = inject(Alerta);
  auth = inject(Autenticacion);

  id: number | null = null;
  guardando = signal(false);
  usuarios = signal<OpcionCombo[]>([]);
  departamentos = signal<OpcionCombo[]>([]);
  mantenimientos = signal<OpcionCombo[]>([]);

  formulario = this.fb.nonNullable.group({
    usuarioId: [0, [Validators.required, Validators.min(1)]],
    departamentoId: [0, [Validators.required, Validators.min(1)]],
    prioridad: ['Media' as PrioridadSoporte, Validators.required],
    estado: ['PENDIENTE' as EstadoSoporte, Validators.required],
    mantenimientoId: [null as number | null],
    descripcion: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(500)]],
    observaciones: ['', Validators.maxLength(500)]
  });

  get volver(): string {
    return this.auth.esInquilino() ? '/mis-tickets' : '/soporte';
  }

  get esEdicion(): boolean {
    return this.id !== null;
  }

  ngOnInit(): void {
    if (this.auth.esInquilino()) {
      // El inquilino queda fijo como solicitante y solo ve los departamentos de sus contratos
      const usuarioId = this.auth.sesion()!.usuarioId!;
      this.formulario.controls.usuarioId.setValue(usuarioId);
      this.servicio.listarDepartamentosComboDeUsuario(usuarioId).subscribe(l => this.departamentos.set(l));
    } else {
      this.servicio.listarUsuariosCombo().subscribe(l => this.usuarios.set(l));
      this.servicio.listarDepartamentosCombo().subscribe(l => this.departamentos.set(l));
    }
    
    this.servicio.listarMantenimientosCombo().subscribe(l => this.mantenimientos.set(l));

    const idRuta = this.ruta.snapshot.paramMap.get('id');
    if (idRuta) {
      this.id = Number(idRuta);
      this.servicio.obtener(this.id).subscribe({
        next: s => this.formulario.patchValue({
          usuarioId: s.usuarioId,
          departamentoId: s.departamentoId,
          prioridad: s.prioridad,
          estado: s.estado,
          mantenimientoId: s.mantenimientoId,
          descripcion: s.descripcion,
          observaciones: s.observaciones ?? ''
        }),
        error: () => {
          this.alerta.error('No se encontró el ticket.');
          this.router.navigate([this.volver]);
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
        this.router.navigate([this.volver]);
      },
      error: err => {
        this.guardando.set(false);
        this.alerta.error(err.error?.error ?? 'No se pudo guardar el ticket.');
      }
    });
  }
}
