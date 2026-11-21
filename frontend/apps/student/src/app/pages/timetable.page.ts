import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ApiService, addDays, errorMessage, groupByDate, isoDate, mondayOf, shortTime } from '@edu/core';

interface Session {
  id: string;
  date: string;
  startTime: string;
  endTime: string;
  sessionType: string;
  status: string;
  makeUp: boolean;
  cancellationReason: string | null;
  subjectName: string | null;
  teacherName: string | null;
  roomCode: string | null;
}

const SESSION_TYPE: Record<string, string> = { LECTURE: 'Cours', TUTORIAL: 'TD', LAB: 'TP', PROJECT: 'Projet' };

/** Emploi du temps: the sessions of the student's class, one week at a time. */
@Component({
  selector: 'edu-timetable-page',
  imports: [DatePipe],
  template: `
    <h1>Emploi du temps</h1>
    <div class="actions">
      <button type="button" class="btn btn-secondary" (click)="move(-7)">‹ Semaine précédente</button>
      <span>Semaine du {{ monday() | date: 'dd/MM/yyyy' }}</span>
      <button type="button" class="btn btn-secondary" (click)="move(7)">Semaine suivante ›</button>
    </div>
    @if (error()) {
      <p class="edu-error">{{ error() }}</p>
    }
    @if (days().length === 0) {
      <p class="edu-muted">Aucune séance cette semaine.</p>
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
                  <strong>{{ s.subjectName }}</strong> · {{ types[s.sessionType] ?? s.sessionType }}
                  @if (s.makeUp) {
                    <span class="badge badge-info">Rattrapage</span>
                  }
                  <div class="edu-muted">{{ s.teacherName }}</div>
                </td>
                <td>{{ s.roomCode }}</td>
                <td>
                  @if (s.status === 'CANCELLED') {
                    <span class="badge badge-danger">Annulée</span>
                    <div class="edu-muted">{{ s.cancellationReason }}</div>
                  }
                </td>
              </tr>
            }
          </tbody>
        </table>
      </section>
    }
  `,
})
export class TimetablePage {
  private readonly api = inject(ApiService);

  protected readonly types = SESSION_TYPE;
  protected readonly time = shortTime;
  protected readonly monday = signal(mondayOf(new Date()));
  protected readonly days = signal<{ date: string; items: Session[] }[]>([]);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.load();
  }

  protected move(days: number): void {
    this.monday.set(addDays(this.monday(), days));
    this.load();
  }

  private load(): void {
    const from = isoDate(this.monday());
    const to = isoDate(addDays(this.monday(), 6));
    this.api.get<Session[]>('/student/timetable', { from, to }).subscribe({
      next: (sessions) => this.days.set(groupByDate(sessions)),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
