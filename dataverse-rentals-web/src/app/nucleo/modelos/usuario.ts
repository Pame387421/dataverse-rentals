export type TipoDocumento = 'DNI' | 'RUC' | 'CE' | 'Pasaporte';

export const TIPOS_DOCUMENTO: { valor: TipoDocumento; texto: string }[] = [
  { valor: 'DNI', texto: 'DNI' },
  { valor: 'RUC', texto: 'RUC' },
  { valor: 'CE', texto: 'Carné de Extranjería' },
  { valor: 'Pasaporte', texto: 'Pasaporte' }
];

export interface Usuario {
  id?: number;
  nombre: string;
  tipoDocumento: TipoDocumento;
  numeroDocumento: string;
  correo: string;
  urlVoucher: string;
  contratoPdf: string;          // ruta del PDF; vacío o "pendiente.pdf" = sin contrato
  tipoUsuarioId: number;
  nombreRol?: string;
  tieneCuenta?: boolean;
  mfaActivo?: boolean;
  pendientePrimerIngreso?: boolean;
}

/** Lo que envía el formulario. */
export interface DatosUsuario {
  nombre: string;
  tipoDocumento: TipoDocumento;
  numeroDocumento: string;
  correo: string;
  urlVoucher: string;
  tipoUsuarioId: number;
  archivoPdf?: File | null;
}

/** Misma regla de tu index.jsp: ruta no vacía y distinta de "pendiente.pdf". */
export function tieneContratoPdf(u: Pick<Usuario, 'contratoPdf'>): boolean {
  const ruta = (u.contratoPdf ?? '').trim();
  return ruta !== '' && ruta.toLowerCase() !== 'pendiente.pdf';
}