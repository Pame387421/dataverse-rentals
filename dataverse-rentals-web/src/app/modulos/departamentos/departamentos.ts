import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Departamento } from '../../nucleo/modelos/departamento';

@Injectable({ providedIn: 'root' })
export class Departamentos {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/departamentos`;

  /** Cada departamento trae el nombre de su edificio (edificioNombre). */
  listar(): Observable<Departamento[]> {
    return this.http.get<Departamento[]>(this.url);
  }

  listarPorEdificio(edificioId: number): Observable<Departamento[]> {
    return this.http.get<Departamento[]>(this.url, { params: new HttpParams().set('edificioId', edificioId) });
  }

  obtener(id: number): Observable<Departamento> {
    return this.http.get<Departamento>(`${this.url}/${id}`);
  }

  crear(d: Departamento): Observable<{ mensaje: string }> {
    return this.http.post<{ mensaje: string }>(this.url, d);
  }

  actualizar(id: number, d: Departamento): Observable<{ mensaje: string }> {
    return this.http.put<{ mensaje: string }>(`${this.url}/${id}`, d);
  }

  /** El servidor rechaza (409) si tiene contratos, medidores, tickets, mantenimientos o pagos. */
  eliminar(id: number): Observable<{ mensaje: string }> {
    return this.http.delete<{ mensaje: string }>(`${this.url}/${id}`);
  }
}