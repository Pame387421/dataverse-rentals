export interface TipoUsuario {
  id?: number;
  nombre: string;
  descripcion: string;
  cantidadUsuarios?: number;
}

export type DatosTipoUsuario = Omit<TipoUsuario, 'id' | 'cantidadUsuarios'>;