import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { computed, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { EDUCATED_CONFIG, SessionService } from '@edu/core';
import { App } from './app';
import { AbsencesPage } from './pages/absences.page';
import { DocumentRequestsPage, proofProblem } from './pages/document-requests.page';
import { PaymentsPage } from './pages/payments.page';
import { TimetablePage } from './pages/timetable.page';

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

  it('shows the week of sessions grouped by day', async () => {
    setUp('STUDENT');
    const fixture = TestBed.createComponent(TimetablePage);
    const backend = TestBed.inject(HttpTestingController);
    backend.expectOne((r) => r.url === '/api/student/timetable').flush([
      { id: 's1', date: '2026-11-16', startTime: '08:30:00', endTime: '10:00:00', sessionType: 'LECTURE',
        status: 'SCHEDULED', makeUp: false, subjectName: 'Architecture logicielle', roomCode: 'B-213' },
      { id: 's2', date: '2026-11-18', startTime: '12:00:00', endTime: '13:30:00', sessionType: 'LAB',
        status: 'CANCELLED', makeUp: false, cancellationReason: 'Grève', subjectName: 'Jakarta EE', roomCode: 'B-213' },
    ]);
    fixture.detectChanges();
    await fixture.whenStable();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelectorAll('section.week-day').length).toBe(2);
    expect(element.textContent).toContain('08:30 – 10:00');
    expect(element.textContent).toContain('Annulée');
    backend.verify();
  });

  it('marks a subject with a high absence rate as critical', async () => {
    setUp('STUDENT');
    const fixture = TestBed.createComponent(AbsencesPage);
    const backend = TestBed.inject(HttpTestingController);
    backend.expectOne('/api/student/absences').flush({
      absences: [{ recordId: 'a1', date: '2026-11-16', startTime: '08:30:00', subjectName: 'Architecture logicielle',
        status: 'ABSENT', lateMinutes: null, justified: false }],
      statistics: [{ teachingAssignmentId: 't1', subjectName: 'Architecture logicielle', sessions: 2,
        unjustifiedAbsences: 1, absencePercent: 50, level: 'CRITICAL' }],
    });
    backend.expectOne('/api/student/absence-requests').flush([]);
    fixture.detectChanges();
    await fixture.whenStable();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Critique');
    expect(text).toContain('Non justifiée');
    backend.verify();
  });

  it('only loads payments when the section is switched on', async () => {
    setUp('STUDENT');
    const fixture = TestBed.createComponent(PaymentsPage);
    const backend = TestBed.inject(HttpTestingController);
    backend.expectOne((r) => r.url === '/api/parametrage/feature-toggles')
      .flush([{ featureKey: 'student.payments', enabled: true }]);
    backend.expectOne('/api/student/payments').flush({
      plans: [{ id: 'p1', totalAmount: 6300, paidAmount: 630, status: 'ACTIVE', installments: [
        { id: 'i1', number: 1, dueDate: '2026-09-15', amount: 630, paidAmount: 630, status: 'PAID' },
      ] }],
      payments: [],
    });
    fixture.detectChanges();
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Payée');
    backend.verify();
  });
});
