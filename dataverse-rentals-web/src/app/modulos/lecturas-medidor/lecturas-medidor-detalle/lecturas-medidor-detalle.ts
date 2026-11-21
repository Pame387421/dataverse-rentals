import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { LecturasMedidor } from '../lecturas-medidor';
import { LecturaMedidor, unidadMedida } from '../../../nucleo/modelos/lectura-medidor';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-lecturas-medidor-detalle',
  imports: [RouterLink, DatePipe, DecimalPipe],
  templateUrl: './lecturas-medidor-detalle.html'
})
export class LecturasMedidorDetalle implements OnInit {
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private servicio = inject(LecturasMedidor);
  private alerta = inject(Alerta);

  lectura = signal<LecturaMedidor | null>(null);
  unidad = unidadMedida;

  ngOnInit(): void {
    const id = Number(this.ruta.snapshot.paramMap.get('id'));
    this.servicio.obtener(id).subscribe({
      next: l => this.lectura.set(l),
      error: () => {
        this.alerta.error('No se encontró la lectura.');
        this.router.navigate(['/lecturas']);
      }
    });
  }
}