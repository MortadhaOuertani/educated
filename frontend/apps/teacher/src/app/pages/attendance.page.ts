import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService, errorMessage, shortTime } from '@edu/core';

type AttendanceStatus = 'PRESENT' | 'ABSENT' | 'LATE';

export interface CallRecord {
  studentId: string;
  matricule: string | null;
  fullName: string | null;
  status: AttendanceStatus;
  lateMinutes: number | null;
}

interface Sheet {
  sessionId: string;
  date: string;
  startTime: string;
  endTime: string;
  subjectName: string;
  status: 'DRAFT' | 'CONFIRMED' | 'LOCKED';
  editableUntil: string | null;
  editable: boolean;
  records: CallRecord[];
}

/** How many students are present, absent and late (shown above the list). */
export function countByStatus(records: CallRecord[]): Record<AttendanceStatus, number> {
  const counts: Record<AttendanceStatus, number> = { PRESENT: 0, ABSENT: 0, LATE: 0 };
  for (const record of records) {
    counts[record.status]++;
  }
  return counts;
}

/** Appel: mark each student, save, then confirm. Changes stay possible during the edit window. */
@Component({
  selector: 'edu-attendance-page',
  imports: [DatePipe, FormsModule, RouterLink],
  template: `
    <a routerLink="/seances">‹ Mes séances</a>
    @if (error()) {
      <p class="edu-error">{{ error() }}</p>
    }
    @if (sheet(); as s) {
      <h1>Appel · {{ s.subjectName }}</h1>
      <p>{{ s.date | date: 'EEEE dd/MM/yyyy' }}, {{ time(s.startTime) }} – {{ time(s.endTime) }}</p>
      <p>
        @if (s.status === 'DRAFT') {
          <span class="badge badge-warning">Non confirmé</span>
        } @else {
          <span class="badge badge-success">Confirmé</span>
          @if (s.editable) {
            <span class="edu-muted"> · modifiable jusqu'au {{ s.editableUntil | date: 'dd/MM/yyyy HH:mm' }}</span>
          }
        }
      </p>
      <p>Présents : {{ counts().PRESENT }} · Absents : {{ counts().ABSENT }} · Retards : {{ counts().LATE }}</p>
      <table class="data">
        <thead><tr><th>Apprenant</th><th>Présence</th><th>Retard (min)</th></tr></thead>
        <tbody>
          @for (r of records(); track r.studentId) {
            <tr>
              <td>{{ r.fullName }} <span class="edu-muted">{{ r.matricule }}</span></td>
              <td>
                <select [(ngModel)]="r.status" [name]="'status-' + r.studentId" [disabled]="!s.editable"
                  (ngModelChange)="refreshCounts()" [attr.aria-label]="'Présence de ' + r.fullName">
                  <option value="PRESENT">Présent</option>
                  <option value="ABSENT">Absent</option>
                  <option value="LATE">En retard</option>
                </select>
              </td>
              <td>
                @if (r.status === 'LATE') {
                  <input type="number" min="1" [(ngModel)]="r.lateMinutes" [name]="'late-' + r.studentId"
                    [disabled]="!s.editable" aria-label="Minutes de retard" />
                }
              </td>
            </tr>
          }
        </tbody>
      </table>
      <div class="actions">
        <button type="button" class="btn btn-secondary" [disabled]="!s.editable || saving()" (click)="save()">Enregistrer</button>
        @if (s.status === 'DRAFT') {
          <button type="button" class="btn" [disabled]="saving()" (click)="confirm()">Confirmer l'appel</button>
        }
      </div>
      @if (saved()) {
        <p class="edu-success">{{ saved() }}</p>
      }
    }
  `,
})
export class AttendancePage implements OnInit {
  private readonly api = inject(ApiService);

  /** Session id from the route (/seances/:id/appel). */
  readonly id = input.required<string>();

  protected readonly time = shortTime;
  protected readonly sheet = signal<Sheet | null>(null);
  protected readonly records = signal<CallRecord[]>([]);
  protected readonly counts = signal(countByStatus([]));
  protected readonly saving = signal(false);
  protected readonly saved = signal<string | null>(null);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.api.get<Sheet>(`/teacher/sessions/${this.id()}/attendance`).subscribe({
      next: (sheet) => this.show(sheet),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected refreshCounts(): void {
    this.counts.set(countByStatus(this.records()));
  }

  protected save(): void {
    const records = this.records().map((r) => ({ studentId: r.studentId, status: r.status, lateMinutes: r.lateMinutes }));
    this.saving.set(true);
    this.api.put<Sheet>(`/teacher/sessions/${this.id()}/attendance`, { records }).subscribe({
      next: (sheet) => {
        this.show(sheet);
        this.saved.set('Appel enregistré.');
      },
      error: (e) => this.fail(e),
    });
  }

  /** Saves the current marks, then confirms. */
  protected confirm(): void {
    const records = this.records().map((r) => ({ studentId: r.studentId, status: r.status, lateMinutes: r.lateMinutes }));
    this.saving.set(true);
    this.api.put<Sheet>(`/teacher/sessions/${this.id()}/attendance`, { records }).subscribe({
      next: () =>
        this.api.post<Sheet>(`/teacher/sessions/${this.id()}/attendance/confirm`, {}).subscribe({
          next: (sheet) => {
            this.show(sheet);
            this.saved.set('Appel confirmé.');
          },
          error: (e) => this.fail(e),
        }),
      error: (e) => this.fail(e),
    });
  }

  private show(sheet: Sheet): void {
    this.saving.set(false);
    this.sheet.set(sheet);
    this.records.set(sheet.records.map((r) => ({ ...r })));
    this.refreshCounts();
  }

  private fail(error: unknown): void {
    this.saving.set(false);
    this.error.set(errorMessage(error));
  }
}
