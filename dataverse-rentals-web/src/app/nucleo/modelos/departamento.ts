export interface Departamento {
  id?: number;
  piso: number;
  numeroDepartamento: string;
  areaM2: number;
  estadoAcabados: string;
  edificioId: number;
  edificioNombre?: string;
}

// Misma lista fija que tu DepartamentoController.obtenerEstadosAcabadosFijos()
export const ESTADOS_ACABADOS = [
  'Nuevo',
  'Remodelado',
  'En buen estado',
  'Necesita reparaciones',
  'Obsoleto'
];