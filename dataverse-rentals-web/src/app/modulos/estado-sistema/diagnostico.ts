import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';

/** Equivale a tu EstadoSistemaViewModel. */
export interface EstadoSistemaDatos {
  baseDatosConectada: boolean;
  totalContratos: number;
  totalPagos: number;
  totalSoportes: number;
  estadoGeneral: string;
  ultimaActualizacion: Date;
}

@Injectable({ providedIn: 'root' })
export class Diagnostico {
  private http = inject(HttpClient);

  /** El servidor abre una conexión real a la base de datos y cuenta los registros de cada tabla. */
  chequear(): Observable<EstadoSistemaDatos> {
    return this.http.get<Omit<EstadoSistemaDatos, 'ultimaActualizacion'> & { ultimaActualizacion: string }>(
      `${environment.apiUrl}/estado`
    ).pipe(map(e => ({ ...e, ultimaActualizacion: new Date(e.ultimaActualizacion) })));
  }
}