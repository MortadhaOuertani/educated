import { computed, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { SessionService } from '@edu/core';
import { App } from './app';

function fakeSession(personType: string) {
  return {
    status: signal('ready'),
    error: signal(null),
    me: signal({ personType }),
    establishments: signal([]),
    selectedEstablishmentId: signal(null),
    displayName: computed(() => 'Demo Teacher'),
    logout: vi.fn(),
  };
}

describe('teacher portal', () => {
  function render(personType: string): string {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: SessionService, useValue: fakeSession(personType) }],
    });
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    return fixture.nativeElement.textContent as string;
  }

  it('opens for teachers', () => {
    expect(render('TEACHER')).not.toContain('Espace réservé aux enseignants');
  });

  it('is closed to other accounts', () => {
    expect(render('STUDENT')).toContain('Espace réservé aux enseignants');
  });
});
