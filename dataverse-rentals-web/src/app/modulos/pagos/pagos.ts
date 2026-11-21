import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, from, map, switchMap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { BoletaPendiente, NuevoPago, Pago } from '../../nucleo/modelos/pago';
import { Boleta } from '../../nucleo/modelos/boleta';
import { Autenticacion } from '../../nucleo/autenticacion/autenticacion';
import { Boletas } from '../boletas/boletas';

function aPendiente(b: Boleta): BoletaPendiente {
  return {
    id: b.id!,
    inquilino: b.inquilino ?? 'N/A',
    nombreDepartamento: b.departamentoNombre ?? 'N/A',
    alquiler: b.montoAlquiler,
    consumoLuz: b.consumoLuz,
    consumoAgua: b.consumoAgua,
    consumoGas: b.consumoGas,
    arbitrios: b.arbitrios,
    montoTotal: b.montoTotal,
    saldoPendiente: b.saldoPendiente ?? b.montoTotal,
    estado: b.estado
  };
}

@Injectable({ providedIn: 'root' })
export class Pagos {
  private http = inject(HttpClient);
  private auth = inject(Autenticacion);
  private boletas = inject(Boletas);
  private url = `${environment.apiUrl}/pagos`;

  /** Administrador: todos, del más reciente al más antiguo. Inquilino: solo los suyos. */
  listar(): Observable<Pago[]> {
    return this.http.get<Pago[]>(this.auth.esInquilino() ? `${this.url}/mios` : this.url);
  }

  /** Los pagos del inquilino que inició sesión (el servidor los filtra por su cuenta). */
  listarDeUsuario(_usuarioId?: number): Observable<Pago[]> {
    return this.http.get<Pago[]>(`${this.url}/mios`);
  }

  obtener(id: number): Observable<Pago> {
    return this.http.get<Pago>(`${this.url}/${id}`);
  }

  /** Equivale a pagoDAO.listarBoletasPendientesCombo(): boletas pendientes con su saldo restante. */
  listarBoletasPendientes(): Observable<BoletaPendiente[]> {
    return this.boletas.listar().pipe(
      map(lista => lista.filter(b => b.estado === 'PENDIENTE').map(aPendiente))
    );
  }

  /** Las boletas pendientes del inquilino que inició sesión. */
  listarBoletasPendientesDeUsuario(_usuarioId?: number): Observable<BoletaPendiente[]> {
    return this.boletas.listarDeUsuario().pipe(
      map(lista => lista.filter(b => b.estado === 'PENDIENTE').map(aPendiente))
    );
  }

  /** Equivale a pagoDAO.obtenerDatosPreFacturacionPago(boletaId). */
  obtenerBoleta(id: number): Observable<BoletaPendiente> {
    return this.boletas.obtener(id).pipe(map(aPendiente));
  }

  /**
   * Va como formulario multipart. Igual que tu controller: si hay PDF se usa el PDF, y si no, la URL.
   * El servidor comprueba que el abono no supere el saldo y que la boleta sea del inquilino.
   */
  crear(n: NuevoPago): Observable<{ mensaje: string }> {
    const datos = new FormData();
    datos.append('boletaId', String(n.boletaId));
    datos.append('montoAbonado', String(n.montoAbonado));
    if (n.archivoPdf) {
      datos.append('archivo', n.archivoPdf);
    } else if (n.urlVoucher?.trim()) {
      datos.append('urlVoucher', n.urlVoucher.trim());
    }
    return this.http.post<{ mensaje: string }>(this.url, datos);
  }

  /** Anula el pago: el servidor recalcula los saldos y reabre la boleta. */
  eliminar(id: number): Observable<{ mensaje: string }> {
    return this.http.delete<{ mensaje: string }>(`${this.url}/${id}`);
  }

  /** Descarga el comprobante (la ruta está protegida) y devuelve una dirección temporal para abrirlo. */
  verVoucher(id: number): Observable<string> {
    return this.http.get(`${this.url}/${id}/voucher`, { responseType: 'blob' }).pipe(
      map(blob => URL.createObjectURL(blob)),
      catchError((err: HttpErrorResponse) => this.errorDeBlob(err))
    );
  }

  /** Con responseType "blob", el cuerpo del error también llega como archivo: hay que leerlo como texto. */
  private errorDeBlob(err: HttpErrorResponse): Observable<never> {
    if (err.error instanceof Blob) {
      return from(err.error.text()).pipe(
        map(texto => { try { return JSON.parse(texto); } catch { return {}; } }),
        switchMap(cuerpo => throwError(() => ({ status: err.status, error: cuerpo })))
      );
    }
    return throwError(() => err);
  }
}