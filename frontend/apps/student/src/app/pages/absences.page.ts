import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService, errorMessage, shortTime } from '@edu/core';
import { proofProblem } from './document-requests.page';

interface AbsenceLine {
  recordId: string;
  date: string;
  startTime: string;
  subjectName: string;
  status: 'ABSENT' | 'LATE';
  lateMinutes: number | null;
  justified: boolean;
}

interface Statistic {
  teachingAssignmentId: string;
  subjectName: string;
  sessions: number;
  unjustifiedAbsences: number;
  absencePercent: number;
  level: 'OK' | 'AT_RISK' | 'CRITICAL';
}

interface AbsenceRequest {
  id: string;
  fromDateTime: string;
  toDateTime: string;
  reason: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
}

/** Badge of the absence rate: at risk from 10 %, critical from 25 % (the establishment's policy). */
export const ABSENCE_LEVEL: Record<Statistic['level'], { label: string; badge: string }> = {
  OK: { label: 'Normal', badge: 'badge-success' },
  AT_RISK: { label: 'À risque', badge: 'badge-warning' },
  CRITICAL: { label: 'Critique', badge: 'badge-danger' },
};

const REQUEST_STATUS: Record<AbsenceRequest['status'], string> = {
  PENDING: 'En attente',
  APPROVED: 'Acceptée',
  REJECTED: 'Refusée',
};

/** Mes absences: rate per subject, list of absences and late arrivals, and justification requests. */
@Component({
  selector: 'edu-absences-page',
  imports: [DatePipe, FormsModule],
  template: `
    <h1>Mes absences</h1>
    @if (error()) {
      <p class="edu-error">{{ error() }}</p>
    }
    <h2>Taux d'absence par matière</h2>
    <table class="data">
      <thead><tr><th>Matière</th><th>Séances</th><th>Absences non justifiées</th><th>Taux</th><th></th></tr></thead>
      <tbody>
        @for (s of statistics(); track s.teachingAssignmentId) {
          <tr>
            <td>{{ s.subjectName }}</td>
            <td>{{ s.sessions }}</td>
            <td>{{ s.unjustifiedAbsences }}</td>
            <td>{{ s.absencePercent }} %</td>
            <td><span [class]="'badge ' + levels[s.level].badge">{{ levels[s.level].label }}</span></td>
          </tr>
        }
      </tbody>
    </table>

    <h2>Absences et retards</h2>
    @if (absences().length === 0) {
      <p class="edu-muted">Aucune absence.</p>
    }
    <table class="data">
      <tbody>
        @for (a of absences(); track a.recordId) {
          <tr>
            <td>{{ a.date | date: 'dd/MM/yyyy' }} {{ time(a.startTime) }}</td>
            <td>{{ a.subjectName }}</td>
            <td>{{ a.status === 'LATE' ? 'Retard (' + (a.lateMinutes ?? 0) + ' min)' : 'Absence' }}</td>
            <td>
              <span [class]="'badge ' + (a.justified ? 'badge-success' : 'badge-warning')">
                {{ a.justified ? 'Justifiée' : 'Non justifiée' }}
              </span>
            </td>
          </tr>
        }
      </tbody>
    </table>

    <form class="card" (ngSubmit)="submit()">
      <h2>Justifier une absence</h2>
      <div class="form">
        <div class="field">
          <label for="from">Du</label>
          <input id="from" name="from" type="datetime-local" [(ngModel)]="from" required />
        </div>
        <div class="field">
          <label for="to">Au</label>
          <input id="to" name="to" type="datetime-local" [(ngModel)]="to" required />
        </div>
        <div class="field">
          <label for="reason">Motif</label>
          <input id="reason" name="reason" [(ngModel)]="reason" required />
        </div>
        <div class="field">
          <label for="proof">Justificatif (PDF/JPG/PNG, 5 Mo)</label>
          <input id="proof" type="file" accept=".pdf,.jpg,.jpeg,.png" (change)="pickProof($event)" />
        </div>
        <button class="btn" type="submit" [disabled]="!from || !to || !reason || sending()">Envoyer</button>
      </div>
      @if (formError()) {
        <p class="edu-error">{{ formError() }}</p>
      }
    </form>

    <h2>Mes demandes</h2>
    <table class="data">
      <tbody>
        @for (r of requests(); track r.id) {
          <tr>
            <td>{{ r.fromDateTime | date: 'dd/MM/yyyy HH:mm' }} → {{ r.toDateTime | date: 'dd/MM/yyyy HH:mm' }}</td>
            <td>{{ r.reason }}</td>
            <td>{{ requestStatus[r.status] }}</td>
          </tr>
        }
      </tbody>
    </table>
  `,
})
export class AbsencesPage {
  private readonly api = inject(ApiService);

  protected readonly levels = ABSENCE_LEVEL;
  protected readonly requestStatus = REQUEST_STATUS;
  protected readonly time = shortTime;
  protected readonly statistics = signal<Statistic[]>([]);
  protected readonly absences = signal<AbsenceLine[]>([]);
  protected readonly requests = signal<AbsenceRequest[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly formError = signal<string | null>(null);
  protected readonly sending = signal(false);

  protected from = '';
  protected to = '';
  protected reason = '';
  private proof: File | null = null;

  constructor() {
    this.load();
  }

  protected pickProof(event: Event): void {
    this.proof = (event.target as HTMLInputElement).files?.[0] ?? null;
    this.formError.set(proofProblem(this.proof));
  }

  protected submit(): void {
    const problem = proofProblem(this.proof);
    if (problem) {
      this.formError.set(problem);
      return;
    }
    const form = new FormData();
    form.append('fromDateTime', this.from);
    form.append('toDateTime', this.to);
    form.append('reason', this.reason);
    if (this.proof) {
      form.append('proof', this.proof);
    }
    this.sending.set(true);
    this.api.postForm('/student/absence-requests', form).subscribe({
      next: () => {
        this.sending.set(false);
        this.from = '';
        this.to = '';
        this.reason = '';
        this.formError.set(null);
        this.load();
      },
      error: (e) => {
        this.sending.set(false);
        this.formError.set(errorMessage(e));
      },
    });
  }

  private load(): void {
    this.api.get<{ absences: AbsenceLine[]; statistics: Statistic[] }>('/student/absences').subscribe({
      next: (data) => {
        this.absences.set(data.absences);
        this.statistics.set(data.statistics);
      },
      error: (e) => this.error.set(errorMessage(e)),
    });
    this.api.get<AbsenceRequest[]>('/student/absence-requests').subscribe({
      next: (requests) => this.requests.set(requests),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
