import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { TipoUsuario } from '../../nucleo/modelos/tipo-usuario';

/** Solo alimenta el desplegable de roles del formulario de usuarios. */
@Injectable({ providedIn: 'root' })
export class TiposUsuario {
  private http = inject(HttpClient);

  listar(): Observable<TipoUsuario[]> {
    return this.http.get<TipoUsuario[]>(`${environment.apiUrl}/tipos-usuario`);
  }
}


