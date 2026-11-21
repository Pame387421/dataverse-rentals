import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LecturaMedidor } from '../../nucleo/modelos/lectura-medidor';
import { OpcionCombo } from '../../nucleo/modelos/contrato';
import { Medidores } from '../medidores/medidores';

@Injectable({ providedIn: 'root' })
export class LecturasMedidor {
  private http = inject(HttpClient);
  private medidores = inject(Medidores);
  private url = `${environment.apiUrl}/lecturas`;

  /** Mismo texto que listarMedidoresCombo de tu DAO: "Luz - serie (Piso 3 - Dpto 302-A - Torre Central)". */
  listarMedidoresCombo(): Observable<OpcionCombo[]> {
    const nombres: Record<string, string> = { LUZ: 'Luz', AGUA: 'Agua', GAS: 'Gas' };
    return this.medidores.listar().pipe(
      map(lista => lista.map(m => ({
        valor: m.id!,
        texto: `${nombres[m.tipo]} - ${m.numeroSerie} (Piso ${m.piso} - Dpto ${m.numeroDepartamento} - ${m.edificioNombre})`
      })))
    );
  }

  /** El servidor las devuelve de la más reciente a la más antigua. */
  listar(): Observable<LecturaMedidor[]> {
    return this.http.get<LecturaMedidor[]>(this.url);
  }

  obtener(id: number): Observable<LecturaMedidor> {
    return this.http.get<LecturaMedidor>(`${this.url}/${id}`);
  }

  crear(l: LecturaMedidor): Observable<{ mensaje: string }> {
    return this.http.post<{ mensaje: string }>(this.url, l);
  }

  actualizar(id: number, l: LecturaMedidor): Observable<{ mensaje: string }> {
    return this.http.put<{ mensaje: string }>(`${this.url}/${id}`, l);
  }

  eliminar(id: number): Observable<{ mensaje: string }> {
    return this.http.delete<{ mensaje: string }>(`${this.url}/${id}`);
  }
}