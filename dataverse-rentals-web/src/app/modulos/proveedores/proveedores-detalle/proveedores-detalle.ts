import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Proveedores } from '../proveedores';
import { Proveedor } from '../../../nucleo/modelos/proveedor';
import { Alerta } from '../../../compartido/alerta';

@Component({
  selector: 'app-proveedores-detalle',
  imports: [RouterLink],
  templateUrl: './proveedores-detalle.html'
})
export class ProveedoresDetalle implements OnInit {
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private servicio = inject(Proveedores);
  private alerta = inject(Alerta);

  proveedor = signal<Proveedor | null>(null);

  ngOnInit(): void {
    const id = Number(this.ruta.snapshot.paramMap.get('id'));
    this.servicio.obtener(id).subscribe({
      next: p => this.proveedor.set(p),
      error: () => {
        this.alerta.error('No se encontró el proveedor.');
        this.router.navigate(['/proveedores']);
      }
    });
  }
}