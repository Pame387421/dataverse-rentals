export interface Encuesta {
  id?: number;
  usuarioId: number;
  calificacion: number;        // 1 = muy malo, 5 = excelente
  comentario: string;
  fechaRegistro: string;       // yyyy-MM-ddTHH:mm
  nombreUsuario?: string;
}

/** Lo que envía el formulario: el servicio pone la fecha. */
export type DatosEncuesta = Pick<Encuesta, 'usuarioId' | 'calificacion' | 'comentario'>;