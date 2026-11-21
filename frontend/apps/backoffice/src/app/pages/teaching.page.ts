import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ApiService, LocalizedText, PersonInfo, SessionService, errorMessage, shortTime } from '@edu/core';

interface Teacher {
  id: string;
  employeeNumber: string | null;
  person: PersonInfo;
  grade: LocalizedText;
  employmentType: string;
  statutoryHours: number | null;
  active: boolean;
}

interface MakeUpRequest {
  id: string;
  subjectName: LocalizedText;
  teacherName: string | null;
  requestedDate: string;
  startTime: string;
  endTime: string;
  reason: string | null;
  status: string;
}

/** Pédagogie: teachers and the make-up sessions waiting for a decision. */
@Component({
  selector: 'edu-teaching-page',
  imports: [DatePipe],
  template: `
    <h1>Pédagogie</h1>
    @if (error()) {
      <p class="edu-error">{{ error() }}</p>
    }
    <h2>Rattrapages à valider</h2>
    <table class="data">
      <tbody>
        @for (r of makeUps(); track r.id) {
          <tr>
            <td>{{ r.teacherName }}</td>
            <td>{{ r.subjectName.fr }}</td>
            <td>{{ r.requestedDate | date: 'dd/MM/yyyy' }} {{ time(r.startTime) }} – {{ time(r.endTime) }}</td>
            <td>{{ r.reason }}</td>
            <td>
              @if (canDecide) {
                <button type="button" class="btn" (click)="decide(r, 'CONFIRMED')">Confirmer</button>
                <button type="button" class="btn btn-danger" (click)="decide(r, 'REJECTED')">Refuser</button>
              }
            </td>
          </tr>
        } @empty {
          <tr><td class="edu-muted">Aucune demande en attente.</td></tr>
        }
      </tbody>
    </table>

    <h2>Enseignants</h2>
    <table class="data">
      <thead><tr><th>Nom</th><th>Matricule</th><th>Grade</th><th>Contrat</th><th>Heures statutaires</th></tr></thead>
      <tbody>
        @for (t of teachers(); track t.id) {
          <tr>
            <td>{{ t.person.lastName }} {{ t.person.firstName }}</td>
            <td>{{ t.employeeNumber }}</td>
            <td>{{ t.grade.fr }}</td>
            <td>{{ t.employmentType }}</td>
            <td>{{ t.statutoryHours }}</td>
          </tr>
        }
      </tbody>
    </table>
  `,
})
export class TeachingPage {
  private readonly api = inject(ApiService);

  protected readonly canDecide = inject(SessionService).can('MakeUpRequest', 'WRITE');
  protected readonly time = shortTime;
  protected readonly teachers = signal<Teacher[]>([]);
  protected readonly makeUps = signal<MakeUpRequest[]>([]);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.api.get<Teacher[]>('/pedagogie/teachers').subscribe({
      next: (list) => this.teachers.set(list),
      error: (e) => this.error.set(errorMessage(e)),
    });
    this.loadMakeUps();
  }

  protected decide(request: MakeUpRequest, status: 'CONFIRMED' | 'REJECTED'): void {
    this.api.patch(`/pedagogie/make-up-requests/${request.id}`, { status }).subscribe({
      next: () => this.loadMakeUps(),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  private loadMakeUps(): void {
    this.api.get<MakeUpRequest[]>('/pedagogie/make-up-requests', { status: 'REQUESTED' }).subscribe({
      next: (list) => this.makeUps.set(list),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
