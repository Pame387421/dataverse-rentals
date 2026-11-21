import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Edificio } from '../../nucleo/modelos/edificio';

@Injectable({ providedIn: 'root' })
export class Edificios {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/edificios`;

  /** Incluye la cantidad de departamentos de cada edificio (cantidadDepartamentos). */
  listar(): Observable<Edificio[]> {
    return this.http.get<Edificio[]>(this.url);
  }

  obtener(id: number): Observable<Edificio> {
    return this.http.get<Edificio>(`${this.url}/${id}`);
  }

  crear(edificio: Edificio): Observable<{ mensaje: string }> {
    return this.http.post<{ mensaje: string }>(this.url, edificio);
  }

  actualizar(id: number, edificio: Edificio): Observable<{ mensaje: string }> {
    return this.http.put<{ mensaje: string }>(`${this.url}/${id}`, edificio);
  }

  /** El servidor rechaza (409) si el edificio tiene departamentos asociados. */
  eliminar(id: number): Observable<{ mensaje: string }> {
    return this.http.delete<{ mensaje: string }>(`${this.url}/${id}`);
  }
}