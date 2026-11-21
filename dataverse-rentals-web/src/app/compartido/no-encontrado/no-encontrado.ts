/*import { Component } from '@angular/core';

@Component({
  imports: [],
  selector: 'app-no-encontrado',
  styleUrl: './no-encontrado.scss',
  templateUrl: './no-encontrado.html',
})
export class NoEncontrado {}*/

import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Autenticacion } from '../../nucleo/autenticacion/autenticacion';

@Component({
  selector: 'app-no-encontrado',
  imports: [RouterLink],
  templateUrl: './no-encontrado.html'
})
export class NoEncontrado {
  auth = inject(Autenticacion);
}
