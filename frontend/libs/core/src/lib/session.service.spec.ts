import { HttpErrorResponse, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { OAuthService } from 'angular-oauth2-oidc';
import { EDUCATED_CONFIG } from './config';
import { EstablishmentStore } from './establishment-store';
import { ESTABLISHMENT_HEADER, establishmentInterceptor } from './establishment.interceptor';
import { Me } from './models';
import { SessionService } from './session.service';

const agent: Me = {
  userId: 'u1',
  username: 'scolarite.agent',
  email: null,
  firstName: 'Agent',
  lastName: 'Scolarité',
  personType: 'STAFF',
  personId: null,
  administrative: false,
  theme: 'SYSTEM',
  displayDensity: 'NORMAL',
  preferredLocale: 'fr',
  establishmentId: 'est-1',
  visibleModules: ['SCOLARITE'],
  permissions: { Student: ['READ', 'WRITE', 'CREATE'] },
  deniedActions: [],
};

describe('SessionService', () => {
  let session: SessionService;
  let backend: HttpTestingController;
  const oauth = {
    configure: vi.fn(),
    loadDiscoveryDocumentAndLogin: vi.fn().mockResolvedValue(true),
    hasValidAccessToken: vi.fn().mockReturnValue(true),
    setupAutomaticSilentRefresh: vi.fn(),
    logOut: vi.fn(),
  };

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        { provide: EDUCATED_CONFIG, useValue: { issuer: 'x', clientId: 'x', apiUrl: '/api', portal: 'BACK_OFFICE' } },
        { provide: OAuthService, useValue: oauth },
        provideHttpClient(withInterceptors([establishmentInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    session = TestBed.inject(SessionService);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  async function flushProfile(me: Me) {
    await Promise.resolve();
    backend.expectOne('/api/me').flush(me);
    await Promise.resolve();
    backend.expectOne('/api/parametrage/establishments').flush([{ id: 'est-1', code: 'TEKUP', name: { fr: 'TEK-UP' } }]);
  }

  it('signs in, loads the profile and chooses the default establishment', async () => {
    const started = session.start();
    await flushProfile(agent);
    await started;

    expect(session.status()).toBe('ready');
    expect(session.displayName()).toBe('Agent Scolarité');
    expect(session.selectedEstablishmentId()).toBe('est-1');
    expect(session.establishments().length).toBe(1);
  });

  it('follows the permissions returned by the backend', async () => {
    const started = session.start();
    await flushProfile(agent);
    await started;

    expect(session.can('Student', 'READ')).toBe(true);
    expect(session.can('Student', 'DELETE')).toBe(false);
    expect(session.can('Role', 'READ')).toBe(false);
    expect(session.isModuleVisible('SCOLARITE')).toBe(true);
    expect(session.isModuleVisible('FINANCES')).toBe(false);
  });

  it('lets an administrative user do everything', async () => {
    const started = session.start();
    await flushProfile({ ...agent, administrative: true, permissions: {}, visibleModules: [] });
    await started;

    expect(session.can('Role', 'DELETE')).toBe(true);
    expect(session.isModuleVisible('FINANCES')).toBe(true);
  });

  it('forgets a remembered establishment the user can no longer access', async () => {
    TestBed.inject(EstablishmentStore).select('old-establishment');

    const loaded = session.loadProfile();
    await Promise.resolve();
    const first = backend.expectOne('/api/me');
    expect(first.request.headers.get(ESTABLISHMENT_HEADER)).toBe('old-establishment');
    first.flush({ detail: 'No access' }, { status: 403, statusText: 'Forbidden' });
    await new Promise((resolve) => setTimeout(resolve));
    const retry = backend.expectOne('/api/me');
    expect(retry.request.headers.has(ESTABLISHMENT_HEADER)).toBe(false);
    retry.flush(agent);
    await Promise.resolve();
    backend.expectOne('/api/parametrage/establishments').flush([]);
    await loaded;

    expect(session.selectedEstablishmentId()).toBe('est-1');
  });

  it('blames Keycloak, not the backend, when the sign-in server cannot be reached', async () => {
    oauth.loadDiscoveryDocumentAndLogin.mockRejectedValueOnce(new HttpErrorResponse({ status: 0 }));
    await session.start();
    expect(session.status()).toBe('error');
    expect(session.error()).toContain('Keycloak');
  });

  it('reports a backend that cannot be reached after signing in', async () => {
    const started = session.start();
    await Promise.resolve();
    backend.expectOne('/api/me').error(new ProgressEvent('error'), { status: 0 });
    await started;
    expect(session.status()).toBe('error');
    expect(session.error()).toContain('backend');
  });
});
