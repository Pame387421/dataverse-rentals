import { Component, inject } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { Autenticacion } from '../../nucleo/autenticacion/autenticacion';
import { VerContrato } from '../ver-contrato';

@Component({
  selector: 'app-plantilla',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, VerContrato],
  templateUrl: './plantilla.html',
  styles: [`
    .sidebar-scroll { overflow-y: auto; }
    .nav-link { border-radius: 6px; padding: 8px 12px; font-size: 14px; display: flex; align-items: center; }
    .nav-link:hover { background-color: #e9ecef; }
    .nav-link i { font-size: 16px; }
  `]
})
export class Plantilla {
  auth = inject(Autenticacion);
  anio = new Date().getFullYear();
}