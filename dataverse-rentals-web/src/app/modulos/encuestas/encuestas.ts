import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DatosEncuesta, Encuesta } from '../../nucleo/modelos/encuesta';
import { OpcionCombo } from '../../nucleo/modelos/contrato';
import { Usuarios } from '../usuarios/usuarios';

@Injectable({ providedIn: 'root' })
export class Encuestas {
  private http = inject(HttpClient);
  private usuarios = inject(Usuarios);
  private url = `${environment.apiUrl}/encuestas`;

  /** Todas, de la más reciente a la más antigua (Administrador). */
  listar(): Observable<Encuesta[]> {
    return this.http.get<Encuesta[]>(this.url);
  }

  /** Solo las del inquilino que inició sesión. */
  listarMias(): Observable<Encuesta[]> {
    return this.http.get<Encuesta[]>(`${this.url}/mias`);
  }

  /** Desplegable de usuarios (solo lo usa el Administrador). */
  listarUsuariosCombo(): Observable<OpcionCombo[]> {
    return this.usuarios.listar().pipe(
      map(lista => lista.map(u => ({ valor: u.id!, texto: u.nombre })))
    );
  }

  /** Un inquilino siempre registra a su nombre; el servidor ignora el usuarioId que envíe. */
  crear(d: DatosEncuesta): Observable<{ mensaje: string }> {
    return this.http.post<{ mensaje: string }>(this.url, d);
  }
}