import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SoportesDetalle } from './soportes-detalle';

describe('SoportesDetalle', () => {
  let component: SoportesDetalle;
  let fixture: ComponentFixture<SoportesDetalle>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SoportesDetalle],
    }).compileComponents();

    fixture = TestBed.createComponent(SoportesDetalle);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
