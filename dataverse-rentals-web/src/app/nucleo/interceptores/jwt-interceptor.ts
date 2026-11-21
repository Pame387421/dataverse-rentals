import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { Injector, inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Autenticacion } from '../autenticacion/autenticacion';

const MENSAJE_SIN_CONEXION = 'No se pudo conectar con el servidor. Verifica que el backend esté en ejecución.';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const injector = inject(Injector);
  const esApi = req.url.startsWith(environment.apiUrl);
  const token = localStorage.getItem('token');

  if (esApi && token) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      if (!esApi) return throwError(() => err);

      // Servidor apagado o inalcanzable: se deja el mensaje en el mismo formato { error: { error } }
      if (err.status === 0) {
        return throwError(() => new HttpErrorResponse({
          status: 0,
          url: err.url ?? undefined,
          error: { error: MENSAJE_SIN_CONEXION }
        }));
      }

      // 401 fuera del login: la sesión venció o el token ya no es válido
      const esLogin = req.url.includes('/auth/login') || req.url.includes('/auth/verificar-2fa');
      if (err.status === 401 && !esLogin) {
        injector.get(Autenticacion).sesionExpirada();
      }
      return throwError(() => err);
    })
  );
};