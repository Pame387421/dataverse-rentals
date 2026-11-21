import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

/** Equivale a tu PagoResumenVM. */
export interface PagoResumen {
  fecha: string;
  usuario: string;
  departamento: string;
  monto: number;
}

/** Equivale a tu DashboardVM. */
export interface DatosDashboard {
  mes: string;
  oficina: string;
  ingresosMensuales: number;
  consumosM3: number;
  contratosPorVencer: number;
  usuariosActivos: number;
  unidadesDisponibles: number;
  alertas: number;
  mesesLabels: string[];
  ingresosPorMes: number[];
  totalAgua: number;
  totalLuz: number;
  totalGas: number;
  ultimosPagos: PagoResumen[];
}

export function dashboardVacio(): DatosDashboard {
  return {
    mes: '', oficina: 'Central', ingresosMensuales: 0, consumosM3: 0, contratosPorVencer: 0,
    usuariosActivos: 0, unidadesDisponibles: 0, alertas: 0, mesesLabels: [], ingresosPorMes: [],
    totalAgua: 0, totalLuz: 0, totalGas: 0, ultimosPagos: []
  };
}

@Injectable({ providedIn: 'root' })
export class Dashboard {
  private http = inject(HttpClient);

  /** El servidor calcula todas las métricas y devuelve solo el resultado. */
  cargar(): Observable<DatosDashboard> {
    return this.http.get<DatosDashboard>(`${environment.apiUrl}/panel`);
  }
}