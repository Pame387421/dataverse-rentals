export interface Proveedor {
  id?: number;
  nombre: string;
  ruc: string;
  correo: string;
  telefono: string;
}

/** Lo que envía el formulario. */
export type DatosProveedor = Omit<Proveedor, 'id'>;