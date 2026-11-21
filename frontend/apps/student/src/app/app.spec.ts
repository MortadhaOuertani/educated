import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { computed, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { EDUCATED_CONFIG, SessionService } from '@edu/core';
import { App } from './app';
import { DocumentRequestsPage, proofProblem } from './pages/document-requests.page';

function fakeSession(personType: string | null) {
  return {
    status: signal('ready'),
    error: signal(null),
    me: signal({ personType }),
    establishments: signal([]),
    selectedEstablishmentId: signal(null),
    displayName: computed(() => 'Demo Student'),
    logout: vi.fn(),
  };
}

describe('student portal', () => {
  function setUp(personType: string | null) {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: SessionService, useValue: fakeSession(personType) },
        { provide: EDUCATED_CONFIG, useValue: { issuer: 'x', clientId: 'x', apiUrl: '/api', portal: 'STUDENT' } },
      ],
    });
  }

  it('is only open to students', () => {
    setUp('STAFF');
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Espace réservé aux étudiants');
  });

  it('lists my document requests with French status labels', async () => {
    setUp('STUDENT');
    const fixture = TestBed.createComponent(DocumentRequestsPage);
    const backend = TestBed.inject(HttpTestingController);
    backend.expectOne('/api/scolarite/document-types').flush([
      { id: 't1', code: 'RELEVE_NOTES', label: { fr: 'Relevé des notes' }, language: 'FR' },
    ]);
    backend.expectOne('/api/student/document-requests').flush([
      { id: 'r1', documentType: { id: 't1', label: { fr: 'Relevé des notes' }, language: 'FR' }, status: 'IN_PROGRESS', requestedAt: '2026-11-20T10:00:00Z' },
    ]);
    fixture.detectChanges();
    await fixture.whenStable();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Relevé des notes');
    expect(text).toContain('En traitement');
    backend.verify();
  });

  it('refuses proofs that are too large or of the wrong type', () => {
    expect(proofProblem(null)).toBeNull();
    expect(proofProblem(new File(['x'], 'a.pdf', { type: 'application/pdf' }))).toBeNull();
    expect(proofProblem(new File(['x'], 'a.exe', { type: 'application/x-msdownload' }))).toContain('PDF');
    const big = new File([new Uint8Array(5 * 1024 * 1024 + 1)], 'big.pdf', { type: 'application/pdf' });
    expect(proofProblem(big)).toContain('5 Mo');
  });
});
