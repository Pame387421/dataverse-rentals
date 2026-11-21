import { Routes } from '@angular/router';
import { Plantilla } from './compartido/plantilla/plantilla';
import { autenticacionGuard } from './nucleo/guardias/autenticacion-guard';
import { inicioGuard, rolGuard } from './nucleo/guardias/rol-guard';
import { boletaPropiaGuard } from './nucleo/guardias/propiedad-guard';

export const routes: Routes = [
  { path: 'inicio-sesion', loadComponent: () => import('./autenticacion/inicio-sesion/inicio-sesion').then(m => m.InicioSesion) },
  { path: 'verificar-2fa', loadComponent: () => import('./autenticacion/verificar-2fa/verificar-2fa').then(m => m.Verificar2fa) },
  {
    path: '',
    component: Plantilla,
    canActivate: [autenticacionGuard],
    children: [
      { path: '', pathMatch: 'full', canActivate: [inicioGuard], children: [] },

      // ---------- COMPARTIDAS (ambos roles; sin guardia de rol para que no haya redirecciones en bucle) ----------
      { path: 'configurar-mfa', loadComponent: () => import('./autenticacion/configurar-mfa/configurar-mfa').then(m => m.ConfigurarMfa) },
      { path: 'cambiar-clave', loadComponent: () => import('./autenticacion/cambiar-clave/cambiar-clave').then(m => m.CambiarClave) },

      // ---------- ADMINISTRADOR ----------
      {
        path: '',
        canActivate: [rolGuard],
        data: { roles: ['Administrador'] },
        children: [
          { path: 'panel', loadComponent: () => import('./modulos/panel-principal/panel-principal').then(m => m.PanelPrincipal) },

          { path: 'edificios', loadComponent: () => import('./modulos/edificios/edificios-lista/edificios-lista').then(m => m.EdificiosLista) },
          { path: 'edificios/nuevo', loadComponent: () => import('./modulos/edificios/edificios-formulario/edificios-formulario').then(m => m.EdificiosFormulario) },
          { path: 'edificios/editar/:id', loadComponent: () => import('./modulos/edificios/edificios-formulario/edificios-formulario').then(m => m.EdificiosFormulario) },
          { path: 'edificios/detalle/:id', loadComponent: () => import('./modulos/edificios/edificios-detalle/edificios-detalle').then(m => m.EdificiosDetalle) },

          { path: 'departamentos', loadComponent: () => import('./modulos/departamentos/departamentos-lista/departamentos-lista').then(m => m.DepartamentosLista) },
          { path: 'departamentos/nuevo', loadComponent: () => import('./modulos/departamentos/departamentos-formulario/departamentos-formulario').then(m => m.DepartamentosFormulario) },
          { path: 'departamentos/editar/:id', loadComponent: () => import('./modulos/departamentos/departamentos-formulario/departamentos-formulario').then(m => m.DepartamentosFormulario) },
          { path: 'departamentos/detalle/:id', loadComponent: () => import('./modulos/departamentos/departamentos-detalle/departamentos-detalle').then(m => m.DepartamentosDetalle) },

          { path: 'contratos', loadComponent: () => import('./modulos/contratos/contratos-lista/contratos-lista').then(m => m.ContratosLista) },
          { path: 'contratos/nuevo', loadComponent: () => import('./modulos/contratos/contratos-formulario/contratos-formulario').then(m => m.ContratosFormulario) },
          { path: 'contratos/editar/:id', loadComponent: () => import('./modulos/contratos/contratos-formulario/contratos-formulario').then(m => m.ContratosFormulario) },
          { path: 'contratos/detalle/:id', loadComponent: () => import('./modulos/contratos/contratos-detalle/contratos-detalle').then(m => m.ContratosDetalle) },

          { path: 'medidores', loadComponent: () => import('./modulos/medidores/medidores-lista/medidores-lista').then(m => m.MedidoresLista) },
          { path: 'medidores/nuevo', loadComponent: () => import('./modulos/medidores/medidores-formulario/medidores-formulario').then(m => m.MedidoresFormulario) },
          { path: 'medidores/editar/:id', loadComponent: () => import('./modulos/medidores/medidores-formulario/medidores-formulario').then(m => m.MedidoresFormulario) },
          { path: 'medidores/detalle/:id', loadComponent: () => import('./modulos/medidores/medidores-detalle/medidores-detalle').then(m => m.MedidoresDetalle) },

          { path: 'lecturas', loadComponent: () => import('./modulos/lecturas-medidor/lecturas-medidor-lista/lecturas-medidor-lista').then(m => m.LecturasMedidorLista) },
          { path: 'lecturas/nuevo', loadComponent: () => import('./modulos/lecturas-medidor/lecturas-medidor-formulario/lecturas-medidor-formulario').then(m => m.LecturasMedidorFormulario) },
          { path: 'lecturas/editar/:id', loadComponent: () => import('./modulos/lecturas-medidor/lecturas-medidor-formulario/lecturas-medidor-formulario').then(m => m.LecturasMedidorFormulario) },
          { path: 'lecturas/detalle/:id', loadComponent: () => import('./modulos/lecturas-medidor/lecturas-medidor-detalle/lecturas-medidor-detalle').then(m => m.LecturasMedidorDetalle) },

          { path: 'boletas', loadComponent: () => import('./modulos/boletas/boletas-lista/boletas-lista').then(m => m.BoletasLista) },
          { path: 'boletas/seleccionar-contrato', loadComponent: () => import('./modulos/boletas/boletas-seleccionar-contrato/boletas-seleccionar-contrato').then(m => m.BoletasSeleccionarContrato) },
          { path: 'boletas/nueva/:contratoId', loadComponent: () => import('./modulos/boletas/boletas-formulario/boletas-formulario').then(m => m.BoletasFormulario) },
          { path: 'boletas/detalle/:id', loadComponent: () => import('./modulos/boletas/boletas-detalle/boletas-detalle').then(m => m.BoletasDetalle) },
          { path: 'boletas/imprimir/:id', loadComponent: () => import('./modulos/boletas/boletas-imprimir/boletas-imprimir').then(m => m.BoletasImprimir) },

          { path: 'pagos', loadComponent: () => import('./modulos/pagos/pagos-lista/pagos-lista').then(m => m.PagosLista) },
          { path: 'pagos/nuevo', loadComponent: () => import('./modulos/pagos/pagos-formulario/pagos-formulario').then(m => m.PagosFormulario) },
          { path: 'pagos/detalle/:id', loadComponent: () => import('./modulos/pagos/pagos-detalle/pagos-detalle').then(m => m.PagosDetalle) },

          { path: 'mantenimientos', loadComponent: () => import('./modulos/mantenimientos/mantenimientos-lista/mantenimientos-lista').then(m => m.MantenimientosLista) },
          { path: 'mantenimientos/nuevo', loadComponent: () => import('./modulos/mantenimientos/mantenimientos-formulario/mantenimientos-formulario').then(m => m.MantenimientosFormulario) },
          { path: 'mantenimientos/editar/:id', loadComponent: () => import('./modulos/mantenimientos/mantenimientos-formulario/mantenimientos-formulario').then(m => m.MantenimientosFormulario) },
          { path: 'mantenimientos/detalle/:id', loadComponent: () => import('./modulos/mantenimientos/mantenimientos-detalle/mantenimientos-detalle').then(m => m.MantenimientosDetalle) },

          { path: 'soporte', loadComponent: () => import('./modulos/soportes/soportes-lista/soportes-lista').then(m => m.SoportesLista) },
          { path: 'soporte/nuevo', loadComponent: () => import('./modulos/soportes/soportes-formulario/soportes-formulario').then(m => m.SoportesFormulario) },
          { path: 'soporte/editar/:id', loadComponent: () => import('./modulos/soportes/soportes-formulario/soportes-formulario').then(m => m.SoportesFormulario) },
          { path: 'soporte/detalle/:id', loadComponent: () => import('./modulos/soportes/soportes-detalle/soportes-detalle').then(m => m.SoportesDetalle) },

          { path: 'proveedores', loadComponent: () => import('./modulos/proveedores/proveedores-lista/proveedores-lista').then(m => m.ProveedoresLista) },
          { path: 'proveedores/nuevo', loadComponent: () => import('./modulos/proveedores/proveedores-formulario/proveedores-formulario').then(m => m.ProveedoresFormulario) },
          { path: 'proveedores/editar/:id', loadComponent: () => import('./modulos/proveedores/proveedores-formulario/proveedores-formulario').then(m => m.ProveedoresFormulario) },
          { path: 'proveedores/detalle/:id', loadComponent: () => import('./modulos/proveedores/proveedores-detalle/proveedores-detalle').then(m => m.ProveedoresDetalle) },

          { path: 'usuarios', loadComponent: () => import('./modulos/usuarios/usuarios-lista/usuarios-lista').then(m => m.UsuariosLista) },
          { path: 'usuarios/nuevo', loadComponent: () => import('./modulos/usuarios/usuarios-formulario/usuarios-formulario').then(m => m.UsuariosFormulario) },
          { path: 'usuarios/editar/:id', loadComponent: () => import('./modulos/usuarios/usuarios-formulario/usuarios-formulario').then(m => m.UsuariosFormulario) },
          { path: 'usuarios/detalle/:id', loadComponent: () => import('./modulos/usuarios/usuarios-detalle/usuarios-detalle').then(m => m.UsuariosDetalle) },

          { path: 'encuestas', loadComponent: () => import('./modulos/encuestas/encuestas-lista/encuestas-lista').then(m => m.EncuestasLista) },
          { path: 'encuestas/nuevo', loadComponent: () => import('./modulos/encuestas/encuestas-formulario/encuestas-formulario').then(m => m.EncuestasFormulario) },

          { path: 'estado-sistema', loadComponent: () => import('./modulos/estado-sistema/estado-sistema').then(m => m.EstadoSistema) },

          { path: 'reportes', loadComponent: () => import('./modulos/reportes/reportes-lista/reportes-lista').then(m => m.ReportesLista) },
          { path: 'reportes/ingresos', loadComponent: () => import('./modulos/reportes/reportes-ingresos/reportes-ingresos').then(m => m.ReportesIngresos) },
          { path: 'reportes/consumos', loadComponent: () => import('./modulos/reportes/reportes-consumos/reportes-consumos').then(m => m.ReportesConsumos) },
          { path: 'reportes/alertas', loadComponent: () => import('./modulos/reportes/reportes-alertas/reportes-alertas').then(m => m.ReportesAlertas) }
        ]
      },

      // ---------- INQUILINO ----------
      {
        path: '',
        canActivate: [rolGuard],
        data: { roles: ['Inquilino'] },
        children: [
          { path: 'mis-boletas', loadComponent: () => import('./modulos/inquilino/mis-boletas/mis-boletas').then(m => m.MisBoletas) },
          { path: 'mis-boletas/detalle/:id', canActivate: [boletaPropiaGuard], loadComponent: () => import('./modulos/boletas/boletas-detalle/boletas-detalle').then(m => m.BoletasDetalle) },
          { path: 'mis-boletas/imprimir/:id', canActivate: [boletaPropiaGuard], loadComponent: () => import('./modulos/boletas/boletas-imprimir/boletas-imprimir').then(m => m.BoletasImprimir) },

          { path: 'mis-pagos', loadComponent: () => import('./modulos/inquilino/mis-pagos/mis-pagos').then(m => m.MisPagos) },
          { path: 'mis-pagos/nuevo', loadComponent: () => import('./modulos/pagos/pagos-formulario/pagos-formulario').then(m => m.PagosFormulario) },

          { path: 'mis-tickets', loadComponent: () => import('./modulos/inquilino/mis-tickets/mis-tickets').then(m => m.MisTickets) },
          { path: 'mis-tickets/nuevo', loadComponent: () => import('./modulos/soportes/soportes-formulario/soportes-formulario').then(m => m.SoportesFormulario) },

          { path: 'mis-encuestas', loadComponent: () => import('./modulos/inquilino/mis-encuestas/mis-encuestas').then(m => m.MisEncuestas) },
          { path: 'mis-encuestas/nueva', loadComponent: () => import('./modulos/encuestas/encuestas-formulario/encuestas-formulario').then(m => m.EncuestasFormulario) },

          { path: 'mi-perfil', loadComponent: () => import('./modulos/inquilino/mi-perfil/mi-perfil').then(m => m.MiPerfil) },
          { path: 'mi-perfil/editar', loadComponent: () => import('./modulos/inquilino/mi-perfil-editar/mi-perfil-editar').then(m => m.MiPerfilEditar) }
        ]
      }
    ]
  },
  { path: '**', loadComponent: () => import('./compartido/no-encontrado/no-encontrado').then(m => m.NoEncontrado) }
];