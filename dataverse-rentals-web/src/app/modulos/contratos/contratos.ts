import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Contrato, OpcionCombo } from '../../nucleo/modelos/contrato';
import { Autenticacion } from '../../nucleo/autenticacion/autenticacion';
import { Departamentos } from '../departamentos/departamentos';
import { Usuarios } from '../usuarios/usuarios';

@Injectable({ providedIn: 'root' })
export class Contratos {
  private http = inject(HttpClient);
  private auth = inject(Autenticacion);
  private departamentos = inject(Departamentos);
  private usuarios = inject(Usuarios);
  private url = `${environment.apiUrl}/contratos`;

  /** Opciones del desplegable de departamentos (equivale a listarDepartamentosCombo). */
  listarDepartamentosCombo(): Observable<OpcionCombo[]> {
    return this.departamentos.listar().pipe(
      map(lista => lista.map(d => ({
        valor: d.id!,
        texto: `${d.edificioNombre} - Piso ${d.piso} - Dpto ${d.numeroDepartamento}`
      })))
    );
  }

  /** Opciones del desplegable de usuarios (equivale a listarUsuariosCombo). */
  listarUsuariosCombo(): Observable<OpcionCombo[]> {
    return this.usuarios.listar().pipe(
      map(lista => lista.map(u => ({ valor: u.id!, texto: u.nombre })))
    );
  }

  /** Administrador: todos los contratos. Inquilino: solo los suyos. */
  listar(): Observable<Contrato[]> {
    return this.http.get<Contrato[]>(this.auth.esInquilino() ? `${this.url}/mios` : this.url);
  }

  obtener(id: number): Observable<Contrato> {
    return this.http.get<Contrato>(`${this.url}/${id}`);
  }

  crear(c: Contrato): Observable<{ mensaje: string }> {
    return this.http.post<{ mensaje: string }>(this.url, c);
  }

  actualizar(id: number, c: Contrato): Observable<{ mensaje: string }> {
    return this.http.put<{ mensaje: string }>(`${this.url}/${id}`, c);
  }

  /** El servidor rechaza (409) si el contrato tiene boletas o pagos. */
  eliminar(id: number): Observable<{ mensaje: string }> {
    return this.http.delete<{ mensaje: string }>(`${this.url}/${id}`);
  }
}