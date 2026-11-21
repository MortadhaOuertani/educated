import { computed, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router, UrlTree, provideRouter } from '@angular/router';
import { Operation, SessionService } from '@edu/core';
import { buildNav, requirePermission } from './access';
import { App } from './app';
import { nextStatuses } from './pages/document-requests.page';
import { remainingAmount } from './pages/finances.page';

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
    expect(labels).toEqual(['Tableau de bord', 'Apprenants', 'Demandes de documents', 'Notifications']);
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

  it('shows a module screen only to roles allowed to read it', () => {
    const financeOnly = fakeSession(false, { Installment: ['READ'] });
    const labels = buildNav(financeOnly.can).map((item) => item.label);
    expect(labels).toContain('Finances');
    expect(labels).not.toContain('Examens et notes');
    expect(labels).not.toContain('Apprenants');
  });

  it('computes what is left to pay on an installment in millimes', () => {
    expect(remainingAmount({ amount: 630, paidAmount: 370.5 })).toBe(259.5);
    expect(remainingAmount({ amount: 0.3, paidAmount: 0.1 })).toBe(0.2);
  });
});
