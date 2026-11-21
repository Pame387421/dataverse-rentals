import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DatosSoporte, Soporte } from '../../nucleo/modelos/soporte';
import { OpcionCombo } from '../../nucleo/modelos/contrato';
import { Departamentos } from '../departamentos/departamentos';
import { Mantenimientos } from '../mantenimientos/mantenimientos';
import { Usuarios } from '../usuarios/usuarios';

function recortar(texto: string, limite: number, corte: number): string {
  return texto.length > limite ? texto.substring(0, corte) + '...' : texto;
}

@Injectable({ providedIn: 'root' })
export class Soportes {
  private http = inject(HttpClient);
  private departamentos = inject(Departamentos);
  private usuarios = inject(Usuarios);
  private mantenimientos = inject(Mantenimientos);
  private url = `${environment.apiUrl}/soportes`;

  /** De la más reciente a la más antigua. */
  listar(): Observable<Soporte[]> {
    return this.http.get<Soporte[]>(this.url);
  }

  /** Los tickets del inquilino que inició sesión (el servidor los filtra por su cuenta). */
  listarDeUsuario(_usuarioId?: number): Observable<Soporte[]> {
    return this.http.get<Soporte[]>(`${this.url}/mios`);
  }

  obtener(id: number): Observable<Soporte> {
    return this.http.get<Soporte>(`${this.url}/${id}`);
  }

  /** Siempre nace Pendiente y sin orden vinculada. Un inquilino queda fijo como solicitante. */
  crear(d: DatosSoporte): Observable<{ mensaje: string }> {
    return this.http.post<{ mensaje: string }>(this.url, d);
  }

  /** El servidor maneja la fecha de cierre (al resolver y al reabrir) y la orden vinculada. */
  actualizar(id: number, d: DatosSoporte): Observable<{ mensaje: string }> {
    return this.http.put<{ mensaje: string }>(`${this.url}/${id}`, d);
  }

  eliminar(id: number): Observable<{ mensaje: string }> {
    return this.http.delete<{ mensaje: string }>(`${this.url}/${id}`);
  }

  /** Administrador: todos los departamentos. Inquilino: el servidor devuelve solo los de sus contratos. */
  listarDepartamentosCombo(): Observable<OpcionCombo[]> {
    return this.departamentos.listar().pipe(
      map(lista => lista.map(d => ({
        valor: d.id!,
        texto: `${d.edificioNombre} - Piso ${d.piso} - Dpto ${d.numeroDepartamento}`
      })))
    );
  }

  listarDepartamentosComboDeUsuario(_usuarioId?: number): Observable<OpcionCombo[]> {
    return this.listarDepartamentosCombo();
  }

  listarUsuariosCombo(): Observable<OpcionCombo[]> {
    return this.usuarios.listar().pipe(
      map(lista => lista.map(u => ({ valor: u.id!, texto: u.nombre })))
    );
  }

  /** Equivale a listarMantenimientosCombo de tu DAO: todas las órdenes, de la última a la primera. */
  listarMantenimientosCombo(): Observable<OpcionCombo[]> {
    return this.mantenimientos.listar().pipe(
      map(lista => [...lista]
        .sort((a, b) => (b.id ?? 0) - (a.id ?? 0))
        .map(m => ({ valor: m.id!, texto: `Orden #${m.id} - ${recortar(m.descripcion, 40, 37)}` })))
    );
  }
}