import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Boleta, BoletaPrevia } from '../../nucleo/modelos/boleta';
import { Autenticacion } from '../../nucleo/autenticacion/autenticacion';

@Injectable({ providedIn: 'root' })
export class Boletas {
  private http = inject(HttpClient);
  private auth = inject(Autenticacion);
  private url = `${environment.apiUrl}/boletas`;

  /** Administrador: todas, de la más reciente a la más antigua. Inquilino: solo las suyas. */
  listar(): Observable<Boleta[]> {
    return this.http.get<Boleta[]>(this.auth.esInquilino() ? `${this.url}/mias` : this.url);
  }

  /** Las boletas del inquilino que inició sesión (el servidor las filtra por su cuenta). */
  listarDeUsuario(_usuarioId?: number): Observable<Boleta[]> {
    return this.http.get<Boleta[]>(`${this.url}/mias`);
  }

  /** Incluye los detalles y el saldo pendiente. El servidor responde 403 si es de otro inquilino. */
  obtener(id: number): Observable<Boleta> {
    return this.http.get<Boleta>(`${this.url}/${id}`);
  }

  /** Equivale al GET /boletas/create: el servidor calcula la boleta con tarifas y lecturas. */
  previsualizar(contratoId: number): Observable<BoletaPrevia> {
    return this.http.get<BoletaPrevia>(`${this.url}/previa`, { params: { contratoId } });
  }

  /** Solo se envían contrato y fechas: los montos los vuelve a calcular el servidor. */
  crear(v: BoletaPrevia): Observable<{ mensaje: string }> {
    return this.http.post<{ mensaje: string }>(this.url, {
      contratoId: v.contratoId,
      fechaEmision: v.fechaEmision,
      fechaVencimiento: v.fechaVencimiento
    });
  }

  /** Borra la boleta y sus pagos (el servidor rechaza con 409 si tiene mantenimientos). */
  eliminar(id: number): Observable<{ mensaje: string }> {
    return this.http.delete<{ mensaje: string }>(`${this.url}/${id}`);
  }
}