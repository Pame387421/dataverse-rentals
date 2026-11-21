export type TipoMedidor = 'LUZ' | 'AGUA' | 'GAS';

// Misma lista que TipoMedidor.values() de tu enum Java
export const TIPOS_MEDIDOR: TipoMedidor[] = ['LUZ', 'AGUA', 'GAS'];

export interface Medidor {
  id?: number;
  numeroSerie: string;
  tipo: TipoMedidor;
  departamentoId: number;
  edificioNombre?: string;
  piso?: number;
  numeroDepartamento?: string;
}