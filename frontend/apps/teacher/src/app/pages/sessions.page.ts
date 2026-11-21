import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService, addDays, errorMessage, groupByDate, isoDate, mondayOf, shortTime } from '@edu/core';

export interface TeacherSession {
  id: string;
  date: string;
  startTime: string;
  endTime: string;
  status: string;
  makeUp: boolean;
  teachingAssignmentId: string;
  subjectName: string | null;
  classCode: string | null;
  roomCode: string | null;
}

interface MakeUpRequest {
  id: string;
  subjectName: { fr: string };
  requestedDate: string;
  startTime: string;
  endTime: string;
  status: string;
}

const MAKE_UP_STATUS: Record<string, string> = {
  REQUESTED: 'Demandée',
  CONFIRMED: 'Confirmée',
  REJECTED: 'Refusée',
  DONE: 'Faite',
};

/** Mes séances: the week's sessions, the call of each session and make-up requests. */
@Component({
  selector: 'edu-sessions-page',
  imports: [DatePipe, FormsModule, RouterLink],
  template: `
    <h1>Mes séances</h1>
    <div class="actions">
      <button type="button" class="btn btn-secondary" (click)="move(-7)">‹ Semaine précédente</button>
      <span>Semaine du {{ monday() | date: 'dd/MM/yyyy' }}</span>
      <button type="button" class="btn btn-secondary" (click)="move(7)">Semaine suivante ›</button>
    </div>
    @if (error()) {
      <p class="edu-error">{{ error() }}</p>
    }
    @for (day of days(); track day.date) {
      <section class="week-day">
        <h3>{{ day.date | date: 'EEEE dd/MM' }}</h3>
        <table class="data">
          <tbody>
            @for (s of day.items; track s.id) {
              <tr>
                <td>{{ time(s.startTime) }} – {{ time(s.endTime) }}</td>
                <td>
                  <strong>{{ s.subjectName }}</strong> · {{ s.classCode }} · {{ s.roomCode }}
                  @if (s.makeUp) {
                    <span class="badge badge-info">Rattrapage</span>
                  }
                  @if (s.status === 'CANCELLED') {
                    <span class="badge badge-danger">Annulée</span>
                  }
                </td>
                <td>
                  @if (s.status !== 'CANCELLED') {
                    <a class="btn" [routerLink]="['/seances', s.id, 'appel']">Appel</a>
                  }
                  <button type="button" class="btn btn-secondary" (click)="startMakeUp(s)">Rattrapage</button>
                </td>
              </tr>
            }
          </tbody>
        </table>
      </section>
    } @empty {
      <p class="edu-muted">Aucune séance cette semaine.</p>
    }

    @if (makeUpFor(); as session) {
      <form class="card" (ngSubmit)="requestMakeUp(session)">
        <h2>Rattrapage de {{ session.subjectName }} du {{ session.date | date: 'dd/MM/yyyy' }}</h2>
        <div class="form">
          <div class="field">
            <label for="date">Date</label>
            <input id="date" name="date" type="date" [(ngModel)]="makeUpDate" required />
          </div>
          <div class="field">
            <label for="start">Début</label>
            <input id="start" name="start" type="time" [(ngModel)]="makeUpStart" required />
          </div>
          <div class="field">
            <label for="end">Fin</label>
            <input id="end" name="end" type="time" [(ngModel)]="makeUpEnd" required />
          </div>
          <div class="field">
            <label for="reason">Motif</label>
            <input id="reason" name="reason" [(ngModel)]="makeUpReason" />
          </div>
          <button class="btn" type="submit" [disabled]="!makeUpDate || !makeUpStart || !makeUpEnd">Demander</button>
          <button class="btn btn-secondary" type="button" (click)="makeUpFor.set(null)">Annuler</button>
        </div>
      </form>
    }

    <h2>Mes demandes de rattrapage</h2>
    <table class="data">
      <tbody>
        @for (r of makeUps(); track r.id) {
          <tr>
            <td>{{ r.subjectName.fr }}</td>
            <td>{{ r.requestedDate | date: 'dd/MM/yyyy' }} {{ time(r.startTime) }} – {{ time(r.endTime) }}</td>
            <td>{{ makeUpStatus[r.status] ?? r.status }}</td>
          </tr>
        } @empty {
          <tr><td class="edu-muted">Aucune demande.</td></tr>
        }
      </tbody>
    </table>
  `,
})
export class SessionsPage {
  private readonly api = inject(ApiService);

  protected readonly time = shortTime;
  protected readonly makeUpStatus = MAKE_UP_STATUS;
  protected readonly monday = signal(mondayOf(new Date()));
  protected readonly days = signal<{ date: string; items: TeacherSession[] }[]>([]);
  protected readonly makeUps = signal<MakeUpRequest[]>([]);
  protected readonly makeUpFor = signal<TeacherSession | null>(null);
  protected readonly error = signal<string | null>(null);

  protected makeUpDate = '';
  protected makeUpStart = '';
  protected makeUpEnd = '';
  protected makeUpReason = '';

  constructor() {
    this.load();
    this.loadMakeUps();
  }

  protected move(days: number): void {
    this.monday.set(addDays(this.monday(), days));
    this.load();
  }

  protected startMakeUp(session: TeacherSession): void {
    this.makeUpFor.set(session);
    this.makeUpDate = '';
    this.makeUpStart = session.startTime.substring(0, 5);
    this.makeUpEnd = session.endTime.substring(0, 5);
    this.makeUpReason = '';
  }

  protected requestMakeUp(session: TeacherSession): void {
    const body = {
      teachingAssignmentId: session.teachingAssignmentId,
      originalOccurrenceId: session.id,
      requestedDate: this.makeUpDate,
      startTime: this.makeUpStart,
      endTime: this.makeUpEnd,
      reason: this.makeUpReason,
    };
    this.api.post('/teacher/make-up-requests', body).subscribe({
      next: () => {
        this.makeUpFor.set(null);
        this.loadMakeUps();
      },
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  private load(): void {
    const from = isoDate(this.monday());
    const to = isoDate(addDays(this.monday(), 6));
    this.api.get<TeacherSession[]>('/teacher/sessions', { from, to }).subscribe({
      next: (sessions) => this.days.set(groupByDate(sessions)),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  private loadMakeUps(): void {
    this.api.get<MakeUpRequest[]>('/teacher/make-up-requests').subscribe({
      next: (list) => this.makeUps.set(list),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
