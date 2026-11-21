import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { Autenticacion } from '../autenticacion/autenticacion';
import { Boletas } from '../../modulos/boletas/boletas';

/** Un inquilino solo puede abrir (desglose o imprimir) sus propias boletas. */
export const boletaPropiaGuard: CanActivateFn = route => {
  const auth = inject(Autenticacion);
  const router = inject(Router);
  const boletas = inject(Boletas);

  const id = Number(route.paramMap.get('id'));
  const usuarioId = auth.sesion()?.usuarioId;
  if (!usuarioId) return router.createUrlTree(['/inicio-sesion']);

  return boletas.listarDeUsuario(usuarioId).pipe(
    map(lista => lista.some(b => b.id === id) ? true : router.createUrlTree(['/mis-boletas']))
  );
};