import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-tipos-usuario-formulario',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container mt-4">
      <div class="alert alert-info">
        <h5 class="fw-bold">Módulo de Administración</h5>
        <p>Los tipos de usuario son administrados internamente por el sistema. El backend expone la lista únicamente para los selectores de usuarios.</p>
        <button class="btn btn-primary btn-sm" (click)="volver()">Volver al Panel</button>
      </div>
    </div>
  `
})
export class TiposUsuarioFormulario implements OnInit {
  private router = inject(Router);

  ngOnInit(): void {
    // Redirección segura para evitar quedarse atrapado en una vista vacía
  }

  volver(): void {
    this.router.navigate(['/panel']);
  }
}
