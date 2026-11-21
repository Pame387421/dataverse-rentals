import { Injectable } from '@angular/core';
import Swal from 'sweetalert2';

function escapar(texto: string): string {
  return texto.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

@Injectable({ providedIn: 'root' })
export class Alerta {

  exito(mensaje: string) {
    return Swal.fire({ icon: 'success', title: 'Listo', text: mensaje, timer: 2000, showConfirmButton: false });
  }

  error(mensaje: string) {
    return Swal.fire({ icon: 'error', title: 'Error', text: mensaje });
  }

  async confirmarEliminacion(mensaje: string): Promise<boolean> {
    const r = await Swal.fire({
      icon: 'warning',
      title: '¿Está seguro?',
      text: mensaje,
      showCancelButton: true,
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar'
    });
    return r.isConfirmed;
  }

    async confirmar(titulo: string, texto: string, textoBoton = 'Confirmar', textoCancelar = 'Cancelar'): Promise<boolean> {
    const r = await Swal.fire({
      icon: 'question', title: titulo, text: texto, showCancelButton: true,
      confirmButtonText: textoBoton, cancelButtonText: textoCancelar, confirmButtonColor: '#198754'
    });
    return r.isConfirmed;
  }

  /** La contraseña temporal se muestra una sola vez; el botón la copia al portapapeles. */
  claveTemporal(correo: string, clave: string): void {
    Swal.fire({
      icon: 'success',
      title: 'Acceso generado',
      html: `<p class="mb-2">Entrega esta contraseña temporal a <strong>${escapar(correo)}</strong>:</p>
             <p class="mb-2"><code class="fs-4 user-select-all">${escapar(clave)}</code></p>
             <p class="small text-muted mb-0">Se muestra una sola vez. Al ingresar, el sistema le pedirá elegir una contraseña propia.</p>`,
      confirmButtonText: 'Copiar y cerrar',
      confirmButtonColor: '#198754',
      preConfirm: () => { navigator.clipboard?.writeText(clave).catch(() => undefined); }
    });
  }
}