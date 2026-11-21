import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Contratos } from '../contratos';
import { Contrato } from '../../../nucleo/modelos/contrato';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-contratos-lista',
  imports: [RouterLink, DatePipe, DecimalPipe],
  templateUrl: './contratos-lista.html'
})
export class ContratosLista implements OnInit {
  private servicio = inject(Contratos);
  private alerta = inject(Alerta);

  contratos = signal<Contrato[]>([]);
  cargando = signal(true);

  // Fecha límite de alerta: hoy + 60 días (formato yyyy-MM-dd, comparable como texto)
  private limiteAlerta = (() => {
    const f = new Date();
    f.setDate(f.getDate() + 60);
    return `${f.getFullYear()}-${String(f.getMonth() + 1).padStart(2, '0')}-${String(f.getDate()).padStart(2, '0')}`;
  })();

  // Métricas (equivalen a los cálculos al inicio de tu index.jsp)
  totalContratos = computed(() => this.contratos().length);
  rentaProyectada = computed(() => this.contratos().reduce((s, c) => s + (c.montoAlquiler ?? 0), 0));
  rentaPromedio = computed(() => this.totalContratos() > 0 ? this.rentaProyectada() / this.totalContratos() : 0);
  alertasVencimiento = computed(() => this.contratos().filter(c => this.estaPorVencer(c)).length);

  ngOnInit(): void {
    this.cargar();
  }

  estaPorVencer(c: Contrato): boolean {
    return c.fechaFinContrato <= this.limiteAlerta;
  }

  cargar(): void {
    this.cargando.set(true);
    this.servicio.listar().subscribe({
      next: datos => { this.contratos.set(datos); this.cargando.set(false); },
      error: () => { this.cargando.set(false); this.alerta.error('No se pudo cargar la lista de contratos.'); }
    });
  }

  async eliminar(c: Contrato): Promise<void> {
    const ok = await this.alerta.confirmarEliminacion(
      `Se eliminará el contrato #${c.id} de ${c.usuarioNombre} (${c.departamentoNombre}).`);
    if (!ok) return;

    this.servicio.eliminar(c.id!).subscribe({
      next: r => { this.alerta.exito(r.mensaje); this.cargar(); },
      error: err => this.alerta.error(err.error?.error ?? 'No se pudo eliminar el contrato.')
    });
  }
}