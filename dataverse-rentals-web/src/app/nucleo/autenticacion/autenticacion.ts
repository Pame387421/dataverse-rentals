import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UsuarioSesion } from '../modelos/usuario-sesion';
import { Alerta } from '../../compartido/alerta';

const CLAVE_SESION = 'sesion';
const CLAVE_TOKEN = 'token';
const CLAVE_MFA = 'tokenTemporalMfa';

/** Lo que devuelve /api/auth/login y /api/auth/verificar-2fa. */
export interface RespuestaAuth {
  requiereMfa: boolean;
  tokenTemporal: string | null;
  token: string | null;
  usuario: UsuarioSesion | null;
}

@Injectable({ providedIn: 'root' })
export class Autenticacion {
  private http = inject(HttpClient);
  private router = inject(Router);
  private alerta = inject(Alerta);
  private url = `${environment.apiUrl}/auth`;

  sesion = signal<UsuarioSesion | null>(this.leerSesion());
  esAdministrador = computed(() => this.sesion()?.rol === 'Administrador');
  esInquilino = computed(() => this.sesion()?.rol === 'Inquilino');

  // ---------------------------------------------------------------- Sesión guardada

  private leerSesion(): UsuarioSesion | null {
    const token = localStorage.getItem(CLAVE_TOKEN);
    const texto = localStorage.getItem(CLAVE_SESION);
    if (!token || !texto || !this.tokenVigente(token)) {
      this.limpiar();
      return null;
    }
    try {
      return JSON.parse(texto) as UsuarioSesion;
    } catch {
      this.limpiar();
      return null;
    }
  }

  /** Revisa la fecha de vencimiento (exp) del JWT, sin consultar al servidor. */
  private tokenVigente(token: string): boolean {
    try {
      const carga = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
      return typeof carga.exp === 'number' && carga.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }

  private limpiar(): void {
    localStorage.removeItem(CLAVE_SESION);
    localStorage.removeItem(CLAVE_TOKEN);
  }

  private guardarSesion(r: RespuestaAuth): void {
    if (!r.token || !r.usuario) return;
    localStorage.setItem(CLAVE_TOKEN, r.token);
    localStorage.setItem(CLAVE_SESION, JSON.stringify(r.usuario));
    sessionStorage.removeItem(CLAVE_MFA);
    this.sesion.set(r.usuario);
  }

  // ---------------------------------------------------------------- Login en dos pasos

  /** Paso 1: correo y clave. Si la cuenta tiene verificación en dos pasos, queda a la espera del código. */
  iniciarSesion(email: string, clave: string): Observable<RespuestaAuth> {
    return this.http.post<RespuestaAuth>(`${this.url}/login`, { email: email.trim(), clave }).pipe(
      tap(r => {
        if (r.requiereMfa && r.tokenTemporal) {
          sessionStorage.setItem(CLAVE_MFA, r.tokenTemporal);
        } else {
          this.guardarSesion(r);
        }
      })
    );
  }

  tienePreautenticacion(): boolean {
    return sessionStorage.getItem(CLAVE_MFA) !== null;
  }

  /** Paso 2: código de 6 dígitos de la aplicación autenticadora. */
  verificarMfa(codigo: string): Observable<RespuestaAuth> {
    const tokenTemporal = sessionStorage.getItem(CLAVE_MFA);
    if (!tokenTemporal) {
      return throwError(() => ({ error: { error: 'La verificación expiró. Inicie sesión nuevamente.' } }));
    }
    return this.http
      .post<RespuestaAuth>(`${this.url}/verificar-2fa`, { tokenTemporal, codigo: codigo.replace(/\s/g, '') })
      .pipe(tap(r => this.guardarSesion(r)));
  }
  
    /** El servidor devuelve una sesión nueva, ya sin la marca de contraseña temporal. */
  cambiarClave(claveActual: string, claveNueva: string): Observable<RespuestaAuth> {
    return this.http
      .post<RespuestaAuth>(`${this.url}/cambiar-clave`, { claveActual, claveNueva })
      .pipe(tap(r => this.guardarSesion(r)));
  }

  // ---------------------------------------------------------------- Estado de la sesión

  estaAutenticado(): boolean {
    return this.sesion() !== null;
  }

  /** Para que el nombre de la barra superior se actualice al editar el perfil. */
  actualizarNombre(nombre: string): void {
    const actual = this.sesion();
    if (!actual) return;
    const nueva = { ...actual, nombre };
    localStorage.setItem(CLAVE_SESION, JSON.stringify(nueva));
    this.sesion.set(nueva);
  }

  /** Igual que tu HomeController: el Administrador va al panel y el Inquilino a sus boletas. */
    /** Con contraseña temporal solo se puede cambiar la clave; luego, el Administrador va al panel y el Inquilino a sus boletas. */
  rutaInicio(): string {
    const s = this.sesion();
    if (s?.debeCambiarClave) return '/cambiar-clave';
    return s?.rol === 'Inquilino' ? '/mis-boletas' : '/panel';
  }

  /** La llama el interceptor cuando el servidor responde 401. */
  sesionExpirada(): void {
    if (!this.estaAutenticado()) return;   // si varias peticiones fallan a la vez, solo avisa una vez
    this.cerrarSesion();
    this.alerta.error('Tu sesión expiró. Inicia sesión nuevamente.');
  }

  cerrarSesion(): void {
    this.limpiar();
    sessionStorage.removeItem(CLAVE_MFA);
    this.sesion.set(null);
    this.router.navigate(['/inicio-sesion']);
  }
}