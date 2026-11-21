import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { EDUCATED_CONFIG } from './config';
import { EstablishmentStore } from './establishment-store';
import { ESTABLISHMENT_HEADER, establishmentInterceptor } from './establishment.interceptor';

describe('establishmentInterceptor', () => {
  let http: HttpClient;
  let backend: HttpTestingController;
  let store: EstablishmentStore;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        { provide: EDUCATED_CONFIG, useValue: { issuer: 'x', clientId: 'x', apiUrl: '/api', portal: 'STUDENT' } },
        provideHttpClient(withInterceptors([establishmentInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
    store = TestBed.inject(EstablishmentStore);
  });

  afterEach(() => backend.verify());

  it('adds the chosen establishment to backend calls only', () => {
    store.select('est-1');

    http.get('/api/me').subscribe();
    http.get('https://keycloak.example/realms/x').subscribe();

    expect(backend.expectOne('/api/me').request.headers.get(ESTABLISHMENT_HEADER)).toBe('est-1');
    expect(backend.expectOne('https://keycloak.example/realms/x').request.headers.has(ESTABLISHMENT_HEADER)).toBe(false);
  });

  it('sends no header when no establishment is chosen', () => {
    http.get('/api/me').subscribe();
    expect(backend.expectOne('/api/me').request.headers.has(ESTABLISHMENT_HEADER)).toBe(false);
  });
});
