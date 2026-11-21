import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Contratos } from '../contratos';
import { Contrato } from '../../../nucleo/modelos/contrato';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-contratos-detalle',
  imports: [RouterLink, DatePipe, DecimalPipe],
  templateUrl: './contratos-detalle.html'
})
export class ContratosDetalle implements OnInit {
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private servicio = inject(Contratos);
  private alerta = inject(Alerta);

  contrato = signal<Contrato | null>(null);

  ngOnInit(): void {
    const id = Number(this.ruta.snapshot.paramMap.get('id'));
    this.servicio.obtener(id).subscribe({
      next: c => this.contrato.set(c),
      error: () => {
        this.alerta.error('No se encontró el contrato.');
        this.router.navigate(['/contratos']);
      }
    });
  }
}