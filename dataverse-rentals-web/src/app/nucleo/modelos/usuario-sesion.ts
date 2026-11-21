export type Rol = 'Administrador' | 'Inquilino';

export interface UsuarioSesion {
  email: string;
  nombre: string;
  rol: Rol;
  usuarioId: number | null;   // id del usuario de negocio (null si no aplica)
  debeCambiarClave?: boolean;   // true mientras use una contraseña temporal
}