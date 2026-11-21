import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Boletas } from '../boletas';
import { Boleta, periodo } from '../../../nucleo/modelos/boleta';
import { Alerta } from '../../../compartido/alerta';
import { Autenticacion } from '../../../nucleo/autenticacion/autenticacion';

@Component({
  selector: 'app-boletas-imprimir',
  imports: [RouterLink, DatePipe, DecimalPipe],
  templateUrl: './boletas-imprimir.html',
  styles: [`:host { -webkit-print-color-adjust: exact; print-color-adjust: exact; }`]
  
})
export class BoletasImprimir implements OnInit {
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private servicio = inject(Boletas);
  private alerta = inject(Alerta);
  public auth = inject(Autenticacion); // <-- Agregada aquí

  boleta = signal<Boleta | null>(null);
  etiquetaPeriodo = periodo;

  ngOnInit(): void {
    const id = Number(this.ruta.snapshot.paramMap.get('id'));
    this.servicio.obtener(id).subscribe({
      next: b => this.boleta.set(b),
      error: () => {
        this.alerta.error('No se encontró la boleta.');
        this.router.navigate(['/boletas']);
      }
    });
  }

  imprimir(): void {
    window.print();
  }
}