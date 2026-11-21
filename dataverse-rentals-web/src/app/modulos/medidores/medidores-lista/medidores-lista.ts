import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Medidores } from '../medidores';
import { Medidor } from '../../../nucleo/modelos/medidor';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-medidores-lista',
  imports: [RouterLink],
  templateUrl: './medidores-lista.html'
})
export class MedidoresLista implements OnInit {
  private servicio = inject(Medidores);
  private alerta = inject(Alerta);

  medidores = signal<Medidor[]>([]);
  cargando = signal(true);

  // Métricas (equivalen a los contadores al inicio de tu index.jsp)
  totalLuz = computed(() => this.medidores().filter(m => m.tipo === 'LUZ').length);
  totalAgua = computed(() => this.medidores().filter(m => m.tipo === 'AGUA').length);
  totalGas = computed(() => this.medidores().filter(m => m.tipo === 'GAS').length);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.servicio.listar().subscribe({
      next: datos => { this.medidores.set(datos); this.cargando.set(false); },
      error: () => { this.cargando.set(false); this.alerta.error('No se pudo cargar la lista de medidores.'); }
    });
  }

  async eliminar(m: Medidor): Promise<void> {
    const ok = await this.alerta.confirmarEliminacion(`Se eliminará el medidor ${m.numeroSerie} (${m.tipo}).`);
    if (!ok) return;

    this.servicio.eliminar(m.id!).subscribe({
      next: r => { this.alerta.exito(r.mensaje); this.cargar(); },
      error: err => this.alerta.error(err.error?.error ?? 'No se pudo eliminar el medidor.')
    });
  }
}