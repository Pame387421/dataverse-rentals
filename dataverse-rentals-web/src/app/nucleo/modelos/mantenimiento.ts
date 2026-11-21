import { OpcionCombo } from './contrato';

export type EstadoMantenimiento = 'PENDIENTE' | 'EN_PROCESO' | 'FINALIZADO';
export type CategoriaMantenimiento = 'Correctivo' | 'Preventivo' | 'Mejora';

export interface Mantenimiento {
  id?: number;
  descripcion: string;
  categoria: CategoriaMantenimiento;
  costoMateriales: number;
  costoManoObra: number;
  costoTotal: number;
  fechaRegistro: string;          // yyyy-MM-dd
  fechaEjecucion?: string;
  fechaFinalizacion?: string;
  estado: EstadoMantenimiento;
  observaciones?: string;
  departamentoId: number;
  edificioId?: number | null;     // se deduce del departamento
  boletaId?: number | null;       // vínculo opcional con una boleta (el formulario no lo edita)
  proveedorId: number;
  ticketSoporteId: number | null;
  // Se completan al leer
  nombreDepartamento?: string;
  nombreProveedor?: string;
  textoTicket?: string;
}

/** Opción del desplegable de tickets: incluye el departamento para filtrar. */
export interface OpcionTicket extends OpcionCombo {
  departamentoId: number;
}

/** Lo que envía el formulario: el servicio calcula el resto. */
export type DatosMantenimiento = Omit<
  Mantenimiento,
  'id' | 'costoTotal' | 'fechaRegistro' | 'edificioId' | 'boletaId'
  | 'nombreDepartamento' | 'nombreProveedor' | 'textoTicket'
>;