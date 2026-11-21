import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Edificios } from '../edificios';
import { Departamentos } from '../../departamentos/departamentos';
import { Edificio } from '../../../nucleo/modelos/edificio';
import { Departamento } from '../../../nucleo/modelos/departamento';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-edificios-detalle',
  imports: [RouterLink, DecimalPipe],
  templateUrl: './edificios-detalle.html'
})
export class EdificiosDetalle implements OnInit {
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private servicio = inject(Edificios);
  private servicioDepartamentos = inject(Departamentos);
  private alerta = inject(Alerta);

  edificio = signal<Edificio | null>(null);
  departamentos = signal<Departamento[]>([]);

  ngOnInit(): void {
    const id = Number(this.ruta.snapshot.paramMap.get('id'));

    this.servicio.obtener(id).subscribe({
      next: e => this.edificio.set(e),
      error: () => {
        this.alerta.error('No se encontró el edificio.');
        this.router.navigate(['/edificios']);
      }
    });

    this.servicioDepartamentos.listarPorEdificio(id).subscribe(lista => this.departamentos.set(lista));
  }
}