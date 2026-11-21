import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService, SessionService, addDays, errorMessage, isoDate, mondayOf, shortTime } from '@edu/core';
import { CurrentYear } from '../current-year';

interface Timetable {
  id: string;
  name: string;
  status: string;
  versionNumber: number;
  slots: { id: string; dayOfWeek: string; startTime: string; endTime: string; subjectName: string; teacherName: string; roomCode: string }[];
}

interface Session {
  id: string;
  date: string;
  startTime: string;
  endTime: string;
  subjectName: string;
  teacherName: string;
  status: string;
  makeUp: boolean;
}

const DAYS: Record<string, string> = {
  MONDAY: 'Lundi', TUESDAY: 'Mardi', WEDNESDAY: 'Mercredi', THURSDAY: 'Jeudi', FRIDAY: 'Vendredi', SATURDAY: 'Samedi',
  SUNDAY: 'Dimanche',
};

/** Emplois du temps: the timetables of a class and its sessions of the week (cancel a session). */
@Component({
  selector: 'edu-timetables-page',
  imports: [DatePipe, FormsModule],
  template: `
    <h1>Emplois du temps</h1>
    @if (error()) {
      <p class="edu-error">{{ error() }}</p>
    }
    <div class="actions">
      <label for="class">Classe</label>
      <select id="class" name="class" [(ngModel)]="classId" (ngModelChange)="load()">
        <option value="">Choisir une classe…</option>
        @for (c of year.classes(); track c.id) {
          <option [value]="c.id">{{ c.code }}</option>
        }
      </select>
    </div>
    @for (t of timetables(); track t.id) {
      <div class="card">
        <strong>{{ t.name }}</strong> · version {{ t.versionNumber }} · {{ t.status }}
        <table class="data">
          <tbody>
            @for (s of t.slots; track s.id) {
              <tr>
                <td>{{ days[s.dayOfWeek] }}</td>
                <td>{{ time(s.startTime) }} – {{ time(s.endTime) }}</td>
                <td>{{ s.subjectName }}</td>
                <td>{{ s.teacherName }}</td>
                <td>{{ s.roomCode }}</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    }
    @if (classId) {
      <h2>Séances de la semaine du {{ monday | date: 'dd/MM/yyyy' }}</h2>
      <table class="data">
        <tbody>
          @for (s of sessions(); track s.id) {
            <tr>
              <td>{{ s.date | date: 'EEEE dd/MM' }} {{ time(s.startTime) }}</td>
              <td>{{ s.subjectName }} @if (s.makeUp) { <span class="badge badge-info">Rattrapage</span> }</td>
              <td>{{ s.teacherName }}</td>
              <td>
                @if (s.status === 'CANCELLED') {
                  <span class="badge badge-danger">Annulée</span>
                } @else if (canCancel) {
                  <button type="button" class="btn btn-danger" (click)="cancel(s)">Annuler</button>
                }
              </td>
            </tr>
          } @empty {
            <tr><td class="edu-muted">Aucune séance.</td></tr>
          }
        </tbody>
      </table>
    }
  `,
})
export class TimetablesPage {
  private readonly api = inject(ApiService);

  protected readonly year = inject(CurrentYear);
  protected readonly canCancel = inject(SessionService).can('SessionOccurrence', 'WRITE');
  protected readonly days = DAYS;
  protected readonly time = shortTime;
  protected readonly monday = mondayOf(new Date());
  protected readonly timetables = signal<Timetable[]>([]);
  protected readonly sessions = signal<Session[]>([]);
  protected readonly error = signal<string | null>(null);
  protected classId = '';

  constructor() {
    this.year.load();
  }

  protected load(): void {
    if (!this.classId) {
      return;
    }
    this.api.get<Timetable[]>('/emploidutemps/timetables', { schoolClassId: this.classId }).subscribe({
      next: (list) => this.timetables.set(list),
      error: (e) => this.error.set(errorMessage(e)),
    });
    this.loadSessions();
  }

  protected cancel(session: Session): void {
    this.api.post(`/emploidutemps/sessions/${session.id}/cancel`, { reason: 'Annulée par la scolarité' }).subscribe({
      next: () => this.loadSessions(),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  private loadSessions(): void {
    const query = { schoolClassId: this.classId, from: isoDate(this.monday), to: isoDate(addDays(this.monday, 6)) };
    this.api.get<Session[]>('/emploidutemps/sessions', query).subscribe({
      next: (list) => this.sessions.set(list),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
