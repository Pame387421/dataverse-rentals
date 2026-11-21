import { TipoMedidor } from './medidor';

export interface LecturaMedidor {
  id?: number;
  medidorId: number;
  fechaLectura: string;   // formato yyyy-MM-dd
  valor: number;
  tipo?: TipoMedidor;
  numeroSerie?: string;
  edificioNombre?: string;
  piso?: number;
  numeroDepartamento?: string;
}

/** Misma regla de tu index.jsp: luz en kWh, el resto en m³. */
export function unidadMedida(tipo?: TipoMedidor): string {
  return tipo === 'LUZ' ? 'kWh' : 'm³';
}