import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DatosProveedor, Proveedor } from '../../nucleo/modelos/proveedor';

@Injectable({ providedIn: 'root' })
export class Proveedores {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/proveedores`;

  /** Del más reciente al más antiguo. */
  listar(): Observable<Proveedor[]> {
    return this.http.get<Proveedor[]>(this.url);
  }

  obtener(id: number): Observable<Proveedor> {
    return this.http.get<Proveedor>(`${this.url}/${id}`);
  }

  crear(d: DatosProveedor): Observable<{ mensaje: string }> {
    return this.http.post<{ mensaje: string }>(this.url, d);
  }

  actualizar(id: number, d: DatosProveedor): Observable<{ mensaje: string }> {
    return this.http.put<{ mensaje: string }>(`${this.url}/${id}`, d);
  }

  /** El servidor rechaza (409) si el proveedor tiene órdenes técnicas asociadas. */
  eliminar(id: number): Observable<{ mensaje: string }> {
    return this.http.delete<{ mensaje: string }>(`${this.url}/${id}`);
  }
}