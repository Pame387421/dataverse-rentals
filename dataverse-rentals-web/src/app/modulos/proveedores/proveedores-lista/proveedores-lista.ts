import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Proveedores } from '../proveedores';
import { Proveedor } from '../../../nucleo/modelos/proveedor';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-proveedores-lista',
  imports: [RouterLink],
  templateUrl: './proveedores-lista.html'
})
export class ProveedoresLista implements OnInit {
  private servicio = inject(Proveedores);
  private alerta = inject(Alerta);

  proveedores = signal<Proveedor[]>([]);
  cargando = signal(true);

  totalEmpresas = computed(() => this.proveedores().length);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.servicio.listar().subscribe({
      next: datos => { this.proveedores.set(datos); this.cargando.set(false); },
      error: () => { this.cargando.set(false); this.alerta.error('No se pudo cargar el catálogo de proveedores.'); }
    });
  }

  async eliminar(p: Proveedor): Promise<void> {
    const ok = await this.alerta.confirmarEliminacion(`Se eliminará el proveedor "${p.nombre}" (RUC ${p.ruc}).`);
    if (!ok) return;

    this.servicio.eliminar(p.id!).subscribe({
      next: r => { this.alerta.exito(r.mensaje); this.cargar(); },
      error: err => this.alerta.error(err.error?.error ?? 'No se pudo eliminar el proveedor.')
    });
  }
}