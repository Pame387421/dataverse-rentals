export interface Contrato {
  id?: number;
  departamentoId: number;
  usuarioId: number;
  fechaInicioContrato: string;   // formato yyyy-MM-dd
  fechaFinContrato: string;      // formato yyyy-MM-dd
  montoAlquiler: number;
  urlVoucher?: string;
  departamentoNombre?: string;
  usuarioNombre?: string;
}

export interface OpcionCombo {
  valor: number;
  texto: string;
}