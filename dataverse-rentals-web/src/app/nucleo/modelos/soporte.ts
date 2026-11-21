export type EstadoSoporte = 'PENDIENTE' | 'EN_PROCESO' | 'RESUELTO';
export type PrioridadSoporte = 'Alta' | 'Media' | 'Baja';

export interface Soporte {
  id?: number;
  descripcion: string;
  fechaRegistro: string;          // yyyy-MM-ddTHH:mm
  fechaCierre?: string;           // yyyy-MM-ddTHH:mm
  estado: EstadoSoporte;
  prioridad: PrioridadSoporte;
  observaciones?: string;
  departamentoId: number;
  usuarioId: number;
  mantenimientoId: number | null;
  // Se completan al leer
  nombreDepartamento?: string;
  nombreUsuario?: string;
  textoMantenimiento?: string;
}

/** Lo que envía el formulario: el servicio calcula las fechas. */
export type DatosSoporte = Omit<
  Soporte,
  'id' | 'fechaRegistro' | 'fechaCierre'
  | 'nombreDepartamento' | 'nombreUsuario' | 'textoMantenimiento'
>;