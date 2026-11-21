import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Autenticacion } from '../autenticacion/autenticacion';

export const autenticacionGuard: CanActivateFn = (_route, state) => {
  const auth = inject(Autenticacion);
  const router = inject(Router);

  if (!auth.estaAutenticado()) return router.createUrlTree(['/inicio-sesion']);

  // Contraseña temporal: solo se permite la pantalla de cambio (si ya está en ella, pasa)
  if (auth.sesion()?.debeCambiarClave && !state.url.startsWith('/cambiar-clave')) {
    return router.createUrlTree(['/cambiar-clave']);
  }
  return true;
};