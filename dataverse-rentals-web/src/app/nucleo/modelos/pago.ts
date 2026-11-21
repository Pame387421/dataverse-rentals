import { EstadoBoleta } from './boleta';

export interface BoletaPendiente {
  id: number;
  inquilino: string;
  nombreDepartamento: string;
  alquiler: number;
  consumoLuz: number;
  consumoAgua: number;
  consumoGas: number;
  arbitrios: number;
  montoTotal: number;
  saldoPendiente: number;
  estado: EstadoBoleta;
  
}

export interface Pago {
  id?: number;
  boletaId: number;
  inquilino: string;
  nombreDepartamento: string;
  fechaPago: string;            // formato yyyy-MM-ddTHH:mm
  alquiler: number;
  consumoLuz: number;
  consumoAgua: number;
  consumoGas: number;
  arbitrios: number;
  montoTotal: number;
  montoAbonado: number;
  saldoPendiente: number;
  tieneVoucherPdf?: boolean;
  urlVoucher?: string;
}

export interface NuevoPago {
  boletaId: number;
  montoAbonado: number;
  archivoPdf?: File | null;
  urlVoucher?: string;
}