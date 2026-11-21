import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Departamentos } from '../departamentos';
import { Departamento } from '../../../nucleo/modelos/departamento';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-departamentos-detalle',
  imports: [RouterLink, DecimalPipe],
  templateUrl: './departamentos-detalle.html'
})
export class DepartamentosDetalle implements OnInit {
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private servicio = inject(Departamentos);
  private alerta = inject(Alerta);

  departamento = signal<Departamento | null>(null);

  ngOnInit(): void {
    const id = Number(this.ruta.snapshot.paramMap.get('id'));
    this.servicio.obtener(id).subscribe({
      next: d => this.departamento.set(d),
      error: () => {
        this.alerta.error('No se encontró el departamento.');
        this.router.navigate(['/departamentos']);
      }
    });
  }
}