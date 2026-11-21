import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

const MESES_CORTOS = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'set', 'oct', 'nov', 'dic'];

/** "2026-10" -> "oct 2026". */
export function etiquetaMes(clave: string): string {
  const [anio, mes] = clave.split('-').map(Number);
  return `${MESES_CORTOS[mes - 1]} ${anio}`;
}

export interface IngresosTresMeses { etiquetas: string[]; valores: number[]; }
export interface ConsumosGlobales { luz: number; agua: number; gas: number; }
export interface MetricasContratos { vigentes: number; porVencer: number; }

export interface ResumenReportes {
  ingresos: IngresosTresMeses;
  consumos: ConsumosGlobales;
  contratos: MetricasContratos;
}

/** Equivale a tu ConsumosPorUnidadVM. */
export interface ConsumoUnidad {
  departamento: string;
  luz: number;
  agua: number;
  gas: number;
  arbitrios: number;
  total: number;
}

export interface IngresoMes {
  clave: string;
  etiqueta: string;
  pagos: number;
  abonado: number;
  saldo: number;
}

/** Equivale a tu ContratosPorVencerVM. */
export interface ContratoPorVencer {
  contratoId: number;
  inquilino: string;
  departamento: string;
  fechaFin: string;
  diasRestantes: number;
}

@Injectable({ providedIn: 'root' })
export class Reportes {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/reportes`;

  /** Lo que muestra el panel de reportes: los tres minigráficos. */
  resumen(): Observable<ResumenReportes> {
    return this.http.get<ResumenReportes>(`${this.url}/resumen`);
  }

  consumosPorUnidad(): Observable<ConsumoUnidad[]> {
    return this.http.get<ConsumoUnidad[]>(`${this.url}/consumos`);
  }

  /** Meses con pagos, del más reciente al más antiguo. */
  ingresosPorMes(): Observable<IngresoMes[]> {
    return this.http.get<IngresoMes[]>(`${this.url}/ingresos`);
  }

  contratosPorVencer(): Observable<ContratoPorVencer[]> {
    return this.http.get<ContratoPorVencer[]>(`${this.url}/alertas`);
  }
}