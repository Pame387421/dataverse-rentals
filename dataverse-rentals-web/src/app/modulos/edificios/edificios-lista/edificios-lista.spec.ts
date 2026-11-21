import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EdificiosLista } from './edificios-lista';

describe('EdificiosLista', () => {
  let component: EdificiosLista;
  let fixture: ComponentFixture<EdificiosLista>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EdificiosLista],
    }).compileComponents();

    fixture = TestBed.createComponent(EdificiosLista);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
