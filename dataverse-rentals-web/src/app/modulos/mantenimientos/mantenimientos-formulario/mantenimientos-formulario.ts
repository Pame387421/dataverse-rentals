import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Mantenimientos } from '../mantenimientos';
import { OpcionCombo } from '../../../nucleo/modelos/contrato';
import {
  CategoriaMantenimiento, EstadoMantenimiento, OpcionTicket
} from '../../../nucleo/modelos/mantenimiento';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-mantenimientos-formulario',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './mantenimientos-formulario.html'
})
export class MantenimientosFormulario implements OnInit {
  private fb = inject(FormBuilder);
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private servicio = inject(Mantenimientos);
  private alerta = inject(Alerta);

  id: number | null = null;
  guardando = signal(false);

  departamentos = signal<OpcionCombo[]>([]);
  proveedores = signal<OpcionCombo[]>([]);
  tickets = signal<OpcionTicket[]>([]);
  departamentoSel = signal(0);
  textoTicketActual = signal('Ninguno');

  mat = signal(0);
  obra = signal(0);

  /** Costo total que se recalcula al escribir (equivale al script de tu create.jsp). */
  costoTotalCalculado = computed(() => (this.mat() + this.obra()).toFixed(2));

  /** Solo los tickets del departamento elegido (o todos si aún no hay uno). */
  ticketsFiltrados = computed(() => {
    const dpto = this.departamentoSel();
    return dpto > 0 ? this.tickets().filter(t => t.departamentoId === dpto) : this.tickets();
  });

  formulario = this.fb.nonNullable.group({
    departamentoId: [0, [Validators.required, Validators.min(1)]],
    proveedorId: [0, [Validators.required, Validators.min(1)]],
    categoria: ['Correctivo' as CategoriaMantenimiento, Validators.required],
    estado: ['PENDIENTE' as EstadoMantenimiento, Validators.required],
    ticketSoporteId: [null as number | null],
    costoMateriales: [0, [Validators.required, Validators.min(0)]],
    costoManoObra: [0, [Validators.required, Validators.min(0)]],
    fechaEjecucion: [''],
    fechaFinalizacion: [''],
    descripcion: ['', [Validators.required, Validators.maxLength(500)]],
    observaciones: ['', Validators.maxLength(500)]
  });

  get esEdicion(): boolean {
    return this.id !== null;
  }

  ngOnInit(): void {
    this.servicio.listarDepartamentosCombo().subscribe(l => this.departamentos.set(l));
    this.servicio.listarProveedoresCombo().subscribe(l => this.proveedores.set(l));
    this.servicio.listarTicketsCombo().subscribe(l => this.tickets.set(l));

    this.formulario.controls.costoMateriales.valueChanges.subscribe(v => this.mat.set(v ?? 0));
    this.formulario.controls.costoManoObra.valueChanges.subscribe(v => this.obra.set(v ?? 0));

    this.formulario.controls.departamentoId.valueChanges.subscribe(v => {
      this.departamentoSel.set(v ?? 0);
      // Al crear, si el ticket elegido ya no corresponde al departamento, se limpia
      const ticket = this.formulario.controls.ticketSoporteId.value;
      if (!this.esEdicion && ticket !== null && !this.ticketsFiltrados().some(t => t.valor === ticket)) {
        this.formulario.controls.ticketSoporteId.setValue(null);
      }
    });

    const idRuta = this.ruta.snapshot.paramMap.get('id');
    if (idRuta) {
      this.id = Number(idRuta);
      this.servicio.obtener(this.id).subscribe({
        next: m => {
          this.textoTicketActual.set(m.textoTicket ?? 'Ninguno');
          this.formulario.patchValue({
            departamentoId: m.departamentoId,
            proveedorId: m.proveedorId,
            categoria: m.categoria,
            estado: m.estado,
            ticketSoporteId: m.ticketSoporteId,
            costoMateriales: m.costoMateriales,
            costoManoObra: m.costoManoObra,
            fechaEjecucion: m.fechaEjecucion?.substring(0, 10) ?? '',
            fechaFinalizacion: m.fechaFinalizacion?.substring(0, 10) ?? '',
            descripcion: m.descripcion,
            observaciones: m.observaciones ?? ''
          });
        },
        error: () => {
          this.alerta.error('No se encontró la orden de mantenimiento.');
          this.router.navigate(['/mantenimientos']);
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
        this.router.navigate(['/mantenimientos']);
      },
      error: err => {
        this.guardando.set(false);
        this.alerta.error(err.error?.error ?? 'No se pudo guardar la orden de mantenimiento.');
      }
    });
  }
}