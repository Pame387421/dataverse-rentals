import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DatosMantenimiento, Mantenimiento, OpcionTicket } from '../../nucleo/modelos/mantenimiento';
import { Soporte } from '../../nucleo/modelos/soporte';
import { OpcionCombo } from '../../nucleo/modelos/contrato';
import { Departamentos } from '../departamentos/departamentos';
import { Proveedores } from '../proveedores/proveedores';

/** Igual que tu DAO: "Ticket #1 - descripción recortada a 25 caracteres". */
function textoTicket(s: Soporte): string {
  const d = s.descripcion ?? '';
  return `Ticket #${s.id} - ${d.length > 25 ? d.substring(0, 25) + '...' : d}`;
}

@Injectable({ providedIn: 'root' })
export class Mantenimientos {
  private http = inject(HttpClient);
  private departamentos = inject(Departamentos);
  private proveedores = inject(Proveedores);
  private url = `${environment.apiUrl}/mantenimientos`;

  /** De la más reciente a la más antigua; cada orden trae su departamento, proveedor y ticket. */
  listar(): Observable<Mantenimiento[]> {
    return this.http.get<Mantenimiento[]>(this.url);
  }

  obtener(id: number): Observable<Mantenimiento> {
    return this.http.get<Mantenimiento>(`${this.url}/${id}`);
  }

  /** El servidor deduce el edificio, calcula el total y pasa el ticket elegido a EN_PROCESO. */
  crear(d: DatosMantenimiento): Observable<{ mensaje: string }> {
    return this.http.post<{ mensaje: string }>(this.url, d);
  }

  /** Conserva la fecha de registro, la boleta y el ticket. */
  actualizar(id: number, d: DatosMantenimiento): Observable<{ mensaje: string }> {
    return this.http.put<{ mensaje: string }>(`${this.url}/${id}`, d);
  }

  /** Sus tickets quedan sin orden vinculada. */
  eliminar(id: number): Observable<{ mensaje: string }> {
    return this.http.delete<{ mensaje: string }>(`${this.url}/${id}`);
  }

  listarDepartamentosCombo(): Observable<OpcionCombo[]> {
    return this.departamentos.listar().pipe(
      map(lista => lista.map(d => ({
        valor: d.id!,
        texto: `${d.edificioNombre} - Piso ${d.piso} - Dpto ${d.numeroDepartamento}`
      })))
    );
  }

  listarProveedoresCombo(): Observable<OpcionCombo[]> {
    return this.proveedores.listar().pipe(
      map(lista => [...lista].reverse().map(p => ({ valor: p.id!, texto: p.nombre })))
    );
  }

  /** Equivale a listarTicketsFiltradosCombo: tickets sin resolver y que aún no tengan una orden. */
  listarTicketsCombo(): Observable<OpcionTicket[]> {
    return this.http.get<Soporte[]>(`${environment.apiUrl}/soportes`).pipe(
      map(lista => lista
        .filter(s => s.estado !== 'RESUELTO' && !s.mantenimientoId)
        .map(s => ({ valor: s.id!, texto: textoTicket(s), departamentoId: s.departamentoId })))
    );
  }
}