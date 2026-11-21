export type EstadoBoleta = 'PENDIENTE' | 'PAGADA' | 'VENCIDA' | 'ANULADA';


export interface DetalleBoleta {
  concepto: string;
  monto: number;
}

export interface Boleta {
  id?: number;
  contratoId: number;
  fechaEmision: string;        // yyyy-MM-dd
  fechaVencimiento: string;    // yyyy-MM-dd
  montoAlquiler: number;
  consumoLuz: number;
  consumoAgua: number;
  consumoGas: number;
  arbitrios: number;
  montoTotal: number;
  estado: EstadoBoleta;
  detalles: DetalleBoleta[];
  // Datos que se completan al leer (contrato, inquilino y unidad)
  inquilino?: string;
  departamentoNombre?: string;
  edificioNombre?: string;
  piso?: number;
  numeroDepartamento?: string;
  saldoPendiente?: number; 
}

/** Equivale a tu BoletaVM: la boleta calculada antes de emitirla. */
export interface BoletaPrevia {
  contratoId: number;
  inquilino: string;
  departamento: string;
  fechaEmision: string;
  fechaVencimiento: string;
  montoAlquiler: number;
  montoArbitrios: number;
  consumoAgua: number;
  consumoLuz: number;
  consumoGas: number;
  montoTotal: number;
}

// Tarifas de tu BoletaController
export const TARIFA_LUZ = 0.85;
export const TARIFA_AGUA = 4.5;
export const TARIFA_GAS = 2.5;

export function redondear(n: number): number {
  return Math.round(n * 100) / 100;
}

const MESES = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio', 'julio', 'agosto',
  'setiembre', 'octubre', 'noviembre', 'diciembre'];

/** Etiqueta del periodo, como el "MMMM yyyy" de tu controller. Ej: "octubre 2026". */
export function periodo(fecha: string): string {
  const [anio, mes] = fecha.split('-').map(Number);
  return `${MESES[mes - 1]} ${anio}`;
}

export function aFechaIso(f: Date): string {
  const p = (n: number) => String(n).padStart(2, '0');
  return `${f.getFullYear()}-${p(f.getMonth() + 1)}-${p(f.getDate())}`;
}

export function hoyIso(): string {
  return aFechaIso(new Date());
}

export function sumarDias(fecha: string, dias: number): string {
  const [a, m, d] = fecha.split('-').map(Number);
  return aFechaIso(new Date(a, m - 1, d + dias));
}

/** Igual que CalculoArbitrios.calcularMensual de tu Java. */
export function calcularArbitriosMensual(uso: string, areaM2: number, frontisML = 0): number {
  const residencial = uso.toLowerCase() === 'residencial';
  const limpieza = areaM2 * (residencial ? 0.35 : 1.2);
  const barrido = frontisML * (residencial ? 0.4 : 0.8);
  const parques = areaM2 * (residencial ? 0.15 : 0.2);
  const serenazgo = areaM2 * (residencial ? 0.5 : 1.5);
  return redondear(limpieza + barrido + parques + serenazgo);
}

/** Desglose que arma tu doPost: los rubros en cero no se incluyen. */
export function armarDetalles(etiqueta: string, alquiler: number, arbitrios: number,
                              agua: number, luz: number, gas: number): DetalleBoleta[] {
  const detalles: DetalleBoleta[] = [{ concepto: `Alquiler Base - ${etiqueta}`, monto: alquiler }];
  if (arbitrios > 0) detalles.push({ concepto: `Arbitrios Municipales - ${etiqueta}`, monto: arbitrios });
  if (agua > 0) detalles.push({ concepto: `Consumo Agua - ${etiqueta}`, monto: agua });
  if (luz > 0) detalles.push({ concepto: `Consumo Luz - ${etiqueta}`, monto: luz });
  if (gas > 0) detalles.push({ concepto: `Consumo Gas - ${etiqueta}`, monto: gas });
  return detalles;
}