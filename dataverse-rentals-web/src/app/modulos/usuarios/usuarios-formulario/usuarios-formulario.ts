import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Usuarios } from '../usuarios';
import { TiposUsuario } from '../../tipos-usuario/tipos-usuario';
import { TipoUsuario } from '../../../nucleo/modelos/tipo-usuario';
import { DatosUsuario, TIPOS_DOCUMENTO, TipoDocumento, tieneContratoPdf } from '../../../nucleo/modelos/usuario';
import { Alerta } from '../../../compartido/alerta';
import { VerContrato } from '../../../compartido/ver-contrato';

const MAX_PDF = 10 * 1024 * 1024; // 10 MB, como tu @MultipartConfig

@Component({
  selector: 'app-usuarios-formulario',
  imports: [ReactiveFormsModule, RouterLink,VerContrato],
  templateUrl: './usuarios-formulario.html'
})
export class UsuariosFormulario implements OnInit {
  private fb = inject(FormBuilder);
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private servicio = inject(Usuarios);
  private serviciotipos = inject(TiposUsuario);
  private alerta = inject(Alerta);

  id: number | null = null;
  guardando = signal(false);
  tipos = signal<TipoUsuario[]>([]);
  tiposDocumento = TIPOS_DOCUMENTO;
  contratoActual = signal('');
  errorArchivo = signal('');
  archivo: File | null = null;

  formulario = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)]],
    correo: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
    tipoDocumento: ['' as TipoDocumento | '', Validators.required],
    numeroDocumento: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(20)]],
    tipoUsuarioId: [0, [Validators.required, Validators.min(1)]],
    urlVoucher: ['', [Validators.maxLength(500), Validators.pattern(/^https?:\/\/.+/)]]
  });

  get esEdicion(): boolean {
    return this.id !== null;
  }

  ngOnInit(): void {
    this.serviciotipos.listar().subscribe(l => this.tipos.set(l));

    const idRuta = this.ruta.snapshot.paramMap.get('id');
    if (idRuta) {
      this.id = Number(idRuta);
      this.servicio.obtener(this.id).subscribe({
        next: u => {
          this.contratoActual.set(tieneContratoPdf(u) ? u.contratoPdf : '');
          this.formulario.patchValue({
            nombre: u.nombre,
            correo: u.correo,
            tipoDocumento: u.tipoDocumento,
            numeroDocumento: u.numeroDocumento,
            tipoUsuarioId: u.tipoUsuarioId,
            urlVoucher: u.urlVoucher ?? ''
          });
        },
        error: () => {
          this.alerta.error('No se encontró el usuario.');
          this.router.navigate(['/usuarios']);
        }
      });
    }
  }

  alElegirArchivo(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    const f = input.files?.[0] ?? null;
    this.archivo = null;
    this.errorArchivo.set('');

    if (!f) return;
    if (!f.name.toLowerCase().endsWith('.pdf')) {
      this.errorArchivo.set('Solo se permiten archivos PDF.');
      input.value = '';
      return;
    }
    if (f.size > MAX_PDF) {
      this.errorArchivo.set('El archivo supera los 10 MB permitidos.');
      input.value = '';
      return;
    }
    this.archivo = f;
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
    const v = this.formulario.getRawValue();
    const datos: DatosUsuario = { ...v, tipoDocumento: v.tipoDocumento as TipoDocumento, archivoPdf: this.archivo };

    const peticion = this.esEdicion
      ? this.servicio.actualizar(this.id!, datos)
      : this.servicio.crear(datos);

    peticion.subscribe({
      next: r => {
        this.alerta.exito(r.mensaje);
        this.router.navigate(['/usuarios']);
      },
      error: err => {
        this.guardando.set(false);
        this.alerta.error(err.error?.error ?? 'No se pudo guardar el usuario.');
      }
    });
  }
}