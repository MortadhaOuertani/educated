import { Component, computed, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { SessionService } from '@edu/core';
import { NavItem, ShellComponent } from './shell.component';

@Component({
  imports: [ShellComponent],
  template: `<edu-shell title="Espace étudiant" [navItems]="items"><p id="content">Bonjour</p></edu-shell>`,
})
class HostComponent {
  items: NavItem[] = [
    { label: 'Accueil', link: '/' },
    { label: 'Demandes de documents', link: '/demandes' },
  ];
}

function fakeSession(status: 'loading' | 'ready' | 'error') {
  return {
    status: signal(status),
    error: signal('Connexion impossible au serveur d’authentification (Keycloak).'),
    establishments: signal([{ id: 'e1', code: 'TEKUP', name: { fr: 'TEK-UP University', en: null, ar: null } }]),
    selectedEstablishmentId: signal('e1'),
    displayName: computed(() => 'Demo Student'),
    logout: vi.fn(),
    switchEstablishment: vi.fn(),
  };
}

describe('ShellComponent', () => {
  function render(status: 'loading' | 'ready' | 'error') {
    const session = fakeSession(status);
    TestBed.configureTestingModule({
      imports: [HostComponent],
      providers: [provideRouter([]), { provide: SessionService, useValue: session }],
    });
    const fixture = TestBed.createComponent(HostComponent);
    fixture.detectChanges();
    return { element: fixture.nativeElement as HTMLElement, session };
  }

  it('waits for the sign-in before showing the page', () => {
    const { element } = render('loading');
    expect(element.textContent).toContain('Connexion…');
    expect(element.querySelector('#content')).toBeNull();
  });

  it('explains when Keycloak cannot be reached', () => {
    const { element } = render('error');
    expect(element.textContent).toContain('Connexion impossible');
    expect(element.textContent).toContain('Keycloak');
  });

  it('shows the user, the menu and the page once signed in', () => {
    const { element, session } = render('ready');
    expect(element.textContent).toContain('Demo Student');
    expect(element.textContent).toContain('TEK-UP University');
    expect(element.querySelectorAll('nav a').length).toBe(2);
    expect(element.querySelector('#content')?.textContent).toBe('Bonjour');

    (Array.from(element.querySelectorAll('button')).find((b) => b.textContent?.includes('Se déconnecter')) as HTMLButtonElement).click();
    expect(session.logout).toHaveBeenCalled();
  });
});
