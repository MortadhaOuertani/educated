import { computed, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router, UrlTree, provideRouter } from '@angular/router';
import { Operation, SessionService } from '@edu/core';
import { buildNav, requirePermission } from './access';
import { App } from './app';
import { nextStatuses } from './pages/document-requests.page';

/** What the "Scolarité" system role gets from the backend. */
const scolariteRights: Record<string, Operation[]> = {
  Student: ['READ', 'WRITE', 'CREATE', 'NAVIGATE'],
  DocumentRequest: ['READ', 'WRITE', 'CREATE', 'NAVIGATE'],
};

function fakeSession(administrative: boolean, permissions: Record<string, Operation[]>) {
  const me = signal({ administrative, permissions });
  return {
    status: signal('ready'),
    error: signal(null),
    me,
    establishments: signal([]),
    selectedEstablishmentId: signal(null),
    displayName: computed(() => 'Agent'),
    logout: vi.fn(),
    can: (type: string, op: Operation) => administrative || (permissions[type] ?? []).includes(op),
  };
}

describe('back-office', () => {
  it('shows only the screens the role allows', () => {
    const session = fakeSession(false, scolariteRights);
    const labels = buildNav(session.can).map((item) => item.label);
    expect(labels).toEqual(['Tableau de bord', 'Apprenants', 'Demandes de documents']);
  });

  it('shows the administration screens to a Super Admin', () => {
    const labels = buildNav(fakeSession(true, {}).can).map((item) => item.label);
    expect(labels).toContain('Rôles');
    expect(labels).toContain('Utilisateurs');
  });

  it('sends users without the right back to the dashboard', () => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: SessionService, useValue: fakeSession(false, scolariteRights) }],
    });
    const allowed = TestBed.runInInjectionContext(() => requirePermission('Student', 'READ')({} as never, {} as never));
    const refused = TestBed.runInInjectionContext(() => requirePermission('Role', 'READ')({} as never, {} as never));
    expect(allowed).toBe(true);
    expect(refused instanceof UrlTree && TestBed.inject(Router).serializeUrl(refused)).toBe('/');
  });

  it('offers only the allowed next statuses of a document request', () => {
    expect(nextStatuses('SUBMITTED')).toEqual(['IN_PROGRESS', 'REJECTED']);
    expect(nextStatuses('IN_PROGRESS')).toEqual(['DONE', 'REJECTED']);
    expect(nextStatuses('DONE')).toEqual([]);
  });

  it('tells accounts without any back-office right that they have no access', () => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: SessionService, useValue: fakeSession(false, {}) }],
    });
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain("Aucun accès à l'espace de gestion");
  });
});
