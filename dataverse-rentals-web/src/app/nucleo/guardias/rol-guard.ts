import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Autenticacion } from '../autenticacion/autenticacion';
import { Rol } from '../modelos/usuario-sesion';

const RUTA_CAMBIO_CLAVE = '/cambiar-clave';

/** Deja pasar solo a los roles indicados en `data: { roles: [...] }` de la ruta. */
export const rolGuard: CanActivateFn = (route, state) => {
  const auth = inject(Autenticacion);
  const router = inject(Router);

  const sesion = auth.sesion();
  if (!sesion) return router.createUrlTree(['/inicio-sesion']);

  // Con contraseña temporal solo se puede estar en la pantalla de cambio.
  // Si ya está ahí, se deja pasar: así nunca puede redirigirse a sí misma.
  if (sesion.debeCambiarClave) {
    return state.url.startsWith(RUTA_CAMBIO_CLAVE) ? true : router.createUrlTree([RUTA_CAMBIO_CLAVE]);
  }

  const roles = route.data['roles'] as Rol[] | undefined;
  return !roles || roles.includes(sesion.rol) ? true : router.createUrlTree([auth.rutaInicio()]);
};

/** La raíz "/" lleva a la pantalla de inicio de cada rol. */
export const inicioGuard: CanActivateFn = () => {
  const auth = inject(Autenticacion);
  return inject(Router).createUrlTree([auth.rutaInicio()]);
};