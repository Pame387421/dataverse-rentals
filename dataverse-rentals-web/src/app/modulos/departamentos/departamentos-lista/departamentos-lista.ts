import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Departamentos } from '../departamentos';
import { Departamento } from '../../../nucleo/modelos/departamento';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-departamentos-lista',
  imports: [RouterLink, DecimalPipe],
  templateUrl: './departamentos-lista.html'
})
export class DepartamentosLista implements OnInit {
  private servicio = inject(Departamentos);
  private alerta = inject(Alerta);

  departamentos = signal<Departamento[]>([]);
  cargando = signal(true);

  // Métricas (equivalen a los cálculos al inicio de tu index.jsp)
  totalUnidades = computed(() => this.departamentos().length);
  areaTotal = computed(() => this.departamentos().reduce((s, d) => s + (d.areaM2 ?? 0), 0));
  areaPromedio = computed(() => this.totalUnidades() > 0 ? this.areaTotal() / this.totalUnidades() : 0);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.servicio.listar().subscribe({
      next: datos => { this.departamentos.set(datos); this.cargando.set(false); },
      error: () => { this.cargando.set(false); this.alerta.error('No se pudo cargar la lista de departamentos.'); }
    });
  }

  claseEstado(estado: string): string {
    const e = (estado ?? '').trim().toLowerCase();
    if (e === 'excelente' || e === 'nuevo') return 'bg-success bg-opacity-10 text-success';
    if (e === 'mantenimiento' || e === 'regular' || e === 'necesita reparaciones') return 'bg-warning bg-opacity-10 text-warning';
    return 'bg-secondary bg-opacity-10 text-secondary';
  }

  async eliminar(d: Departamento): Promise<void> {
    const ok = await this.alerta.confirmarEliminacion(
      `Se eliminará el departamento ${d.numeroDepartamento} (Piso ${d.piso}).`);
    if (!ok) return;

    this.servicio.eliminar(d.id!).subscribe({
      next: r => { this.alerta.exito(r.mensaje); this.cargar(); },
      error: err => this.alerta.error(err.error?.error ?? 'No se pudo eliminar el departamento.')
    });
  }
}