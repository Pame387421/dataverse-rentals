import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, from, map, of, switchMap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DatosUsuario, TipoDocumento, Usuario } from '../../nucleo/modelos/usuario';
import { Autenticacion } from '../../nucleo/autenticacion/autenticacion';

@Injectable({ providedIn: 'root' })
export class Usuarios {
  private http = inject(HttpClient);
  private auth = inject(Autenticacion);
  private url = `${environment.apiUrl}/usuarios`;

  /**
   * Administrador: todos los usuarios. Inquilino: solo su propio perfil (lo usan los módulos que aún
   * están en memoria para mostrar su nombre, sin llamar a rutas de administración).
   */
  listar(): Observable<Usuario[]> {
    if (this.auth.esInquilino()) return this.obtenerPropio().pipe(map(u => [u]));
    return this.http.get<Usuario[]>(this.url);
  }

  obtener(id: number): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.url}/${id}`);
  }

  /** El perfil del usuario que inició sesión (sirve para Administrador e Inquilino). */
  obtenerPropio(): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.url}/me`);
  }

  crear(d: DatosUsuario): Observable<{ mensaje: string }> {
    const { archivoPdf, ...campos } = d;
    return this.http.post<{ mensaje: string; id: number }>(this.url, campos).pipe(
      switchMap(r => this.cargarContrato(r.id, archivoPdf, r.mensaje, 'El usuario se creó, pero no se pudo cargar el contrato'))
    );
  }

  /** Si no se elige un PDF nuevo, el servidor conserva el actual. */
  actualizar(id: number, d: DatosUsuario): Observable<{ mensaje: string }> {
    const { archivoPdf, ...campos } = d;
    return this.http.put<{ mensaje: string }>(`${this.url}/${id}`, campos).pipe(
      switchMap(r => this.cargarContrato(id, archivoPdf, r.mensaje, 'Los datos se guardaron, pero no se pudo cargar el contrato'))
    );
  }

  /** Solo nombre y documento (no el correo, el rol ni el contrato). */
  actualizarPerfil(d: { nombre: string; tipoDocumento: TipoDocumento; numeroDocumento: string }): Observable<{ mensaje: string }> {
    return this.http.put<{ mensaje: string }>(`${this.url}/me`, d);
  }

  eliminar(id: number): Observable<{ mensaje: string }> {
    return this.http.delete<{ mensaje: string }>(`${this.url}/${id}`);
  }

    /** Crea la cuenta o reinicia la clave. La contraseña temporal solo llega en esta respuesta. */
  generarAcceso(id: number, reiniciarMfa = false): Observable<{ correo: string; claveTemporal: string; cuentaNueva: boolean }> {
    return this.http.post<{ correo: string; claveTemporal: string; cuentaNueva: boolean }>(
      `${this.url}/${id}/cuenta`, {}, { params: { reiniciarMfa } }
    );
  }

  /** Descarga el PDF (la ruta está protegida) y devuelve una dirección temporal para abrirlo. */
  verContrato(id: number): Observable<string> {
    return this.http.get(`${this.url}/${id}/contrato`, { responseType: 'blob' }).pipe(
      map(blob => URL.createObjectURL(blob)),
      catchError((err: HttpErrorResponse) => this.errorDeBlob(err))
    );
  }

  private cargarContrato(id: number, archivo: File | null | undefined, mensaje: string, aviso: string): Observable<{ mensaje: string }> {
    if (!archivo) return of({ mensaje });

    const datos = new FormData();
    datos.append('archivo', archivo);
    return this.http.post(`${this.url}/${id}/contrato`, datos).pipe(
      map(() => ({ mensaje })),
      catchError(err => throwError(() => ({
        error: { error: `${aviso}: ${err.error?.error ?? 'error desconocido'}` }
      })))
    );
  }

  /** Con responseType "blob", el cuerpo del error también llega como archivo: hay que leerlo como texto. */
  private errorDeBlob(err: HttpErrorResponse): Observable<never> {
    if (err.error instanceof Blob) {
      return from(err.error.text()).pipe(
        map(texto => { try { return JSON.parse(texto); } catch { return {}; } }),
        switchMap(cuerpo => throwError(() => ({ status: err.status, error: cuerpo })))
      );
    }
    return throwError(() => err);
  }
}