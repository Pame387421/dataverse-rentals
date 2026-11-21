import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LecturasMedidorFormulario } from './lecturas-medidor-formulario';

describe('LecturasMedidorFormulario', () => {
  let component: LecturasMedidorFormulario;
  let fixture: ComponentFixture<LecturasMedidorFormulario>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LecturasMedidorFormulario],
    }).compileComponents();

    fixture = TestBed.createComponent(LecturasMedidorFormulario);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
