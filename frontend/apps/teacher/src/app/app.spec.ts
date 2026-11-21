import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { computed, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { EDUCATED_CONFIG, SessionService } from '@edu/core';
import { App } from './app';
import { AttendancePage, countByStatus } from './pages/attendance.page';
import { gradeProblem } from './pages/grades.page';

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

  it('counts present, absent and late students', () => {
    const counts = countByStatus([
      { studentId: 'a', matricule: null, fullName: 'A', status: 'PRESENT', lateMinutes: null },
      { studentId: 'b', matricule: null, fullName: 'B', status: 'ABSENT', lateMinutes: null },
      { studentId: 'c', matricule: null, fullName: 'C', status: 'LATE', lateMinutes: 10 },
      { studentId: 'd', matricule: null, fullName: 'D', status: 'ABSENT', lateMinutes: null },
    ]);
    expect(counts).toEqual({ PRESENT: 1, ABSENT: 2, LATE: 1 });
  });

  it('checks grades like the backend', () => {
    expect(gradeProblem(14, null, 20)).toBeNull();
    expect(gradeProblem(null, 'EXEMPTED', 20)).toBeNull();
    expect(gradeProblem(25, null, 20)).toContain('entre 0 et 20');
    expect(gradeProblem(12, 'FRAUD', 20)).toContain('pas les deux');
  });

  it('loads the call sheet of the session and shows who is absent', async () => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: EDUCATED_CONFIG, useValue: { issuer: 'x', clientId: 'x', apiUrl: '/api', portal: 'TEACHER' } },
      ],
    });
    const fixture = TestBed.createComponent(AttendancePage);
    fixture.componentRef.setInput('id', 's1');
    fixture.detectChanges();
    const backend = TestBed.inject(HttpTestingController);
    backend.expectOne('/api/teacher/sessions/s1/attendance').flush({
      sessionId: 's1', date: '2026-11-16', startTime: '08:30:00', endTime: '10:00:00',
      subjectName: 'Architecture logicielle', status: 'DRAFT', editableUntil: null, editable: true,
      records: [
        { studentId: 'st1', matricule: 'DEMO-0001', fullName: 'STUDENT Demo', status: 'ABSENT', lateMinutes: null },
      ],
    });
    fixture.detectChanges();
    await fixture.whenStable();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Appel · Architecture logicielle');
    expect(text).toContain('Absents : 1');
    expect(text).toContain("Confirmer l'appel");
    backend.verify();
  });
});
