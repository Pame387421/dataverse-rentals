import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Medidor } from '../../nucleo/modelos/medidor';
import { OpcionCombo } from '../../nucleo/modelos/contrato';
import { Departamentos } from '../departamentos/departamentos';

@Injectable({ providedIn: 'root' })
export class Medidores {
  private http = inject(HttpClient);
  private departamentos = inject(Departamentos);
  private url = `${environment.apiUrl}/medidores`;

  /** Mismo texto que listarDepartamentosFiltroCombo de tu DAO: "302-A - Piso 3 - Torre Central". */
  listarDepartamentosCombo(): Observable<OpcionCombo[]> {
    return this.departamentos.listar().pipe(
      map(lista => lista.map(d => ({
        valor: d.id!,
        texto: `${d.numeroDepartamento} - Piso ${d.piso} - ${d.edificioNombre}`
      })))
    );
  }

  listar(): Observable<Medidor[]> {
    return this.http.get<Medidor[]>(this.url);
  }

  obtener(id: number): Observable<Medidor> {
    return this.http.get<Medidor>(`${this.url}/${id}`);
  }

  crear(m: Medidor): Observable<{ mensaje: string }> {
    return this.http.post<{ mensaje: string }>(this.url, m);
  }

  actualizar(id: number, m: Medidor): Observable<{ mensaje: string }> {
    return this.http.put<{ mensaje: string }>(`${this.url}/${id}`, m);
  }

  /** El servidor rechaza (409) si el medidor tiene lecturas. */
  eliminar(id: number): Observable<{ mensaje: string }> {
    return this.http.delete<{ mensaje: string }>(`${this.url}/${id}`);
  }
}