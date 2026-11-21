import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ApiService, errorMessage, shortTime } from '@edu/core';

interface Exam {
  id: string;
  subjectName: string;
  assessmentType: string;
  examDate: string;
  startTime: string;
  endTime: string;
  roomCodes: string[];
}

interface Grade {
  subjectCode: string;
  subjectName: string;
  assessmentType: string;
  score: number | null;
  maxScore: number;
  reason: string | null;
}

interface ReportCard {
  id: string;
  average: number | null;
  rankInClass: number | null;
  creditsEarned: number;
  appreciation: string | null;
  subjects: { subjectCode: string; subjectName: string; coefficient: number; average: number; validated: boolean }[];
}

/** Why there is no score, as shown to the student. */
export const GRADE_REASON: Record<string, string> = {
  JUSTIFIED_ABSENCE: 'Absence justifiée',
  UNJUSTIFIED_ABSENCE: 'Absence non justifiée',
  EXEMPTED: 'Dispensé(e)',
  FRAUD: 'Fraude',
};

/** Examens et notes: published exam calendar, published grades and report cards. */
@Component({
  selector: 'edu-grades-page',
  imports: [DatePipe, DecimalPipe],
  template: `
    <h1>Examens et notes</h1>
    @if (error()) {
      <p class="edu-error">{{ error() }}</p>
    }
    <h2>Calendrier des examens</h2>
    @if (exams().length === 0) {
      <p class="edu-muted">Aucun examen publié.</p>
    }
    <table class="data">
      <tbody>
        @for (e of exams(); track e.id) {
          <tr>
            <td>{{ e.examDate | date: 'EEEE dd/MM/yyyy' }}</td>
            <td>{{ time(e.startTime) }} – {{ time(e.endTime) }}</td>
            <td>{{ e.subjectName }} · {{ e.assessmentType }}</td>
            <td>{{ e.roomCodes.join(', ') }}</td>
          </tr>
        }
      </tbody>
    </table>

    <h2>Notes publiées</h2>
    @if (grades().length === 0) {
      <p class="edu-muted">Aucune note publiée.</p>
    }
    <table class="data">
      <tbody>
        @for (g of grades(); track $index) {
          <tr>
            <td>{{ g.subjectName }}</td>
            <td>{{ g.assessmentType }}</td>
            <td>{{ g.score !== null ? (g.score | number: '1.2-2') + ' / ' + g.maxScore : reasons[g.reason ?? ''] }}</td>
          </tr>
        }
      </tbody>
    </table>

    <h2>Bulletins</h2>
    @for (card of reportCards(); track card.id) {
      <div class="card">
        <p>
          Moyenne générale : <strong>{{ card.average | number: '1.2-2' }}</strong> · Rang : {{ card.rankInClass }} ·
          Crédits : {{ card.creditsEarned }} · {{ card.appreciation }}
        </p>
        <table class="data">
          <thead><tr><th>Matière</th><th>Coefficient</th><th>Moyenne</th><th></th></tr></thead>
          <tbody>
            @for (s of card.subjects; track s.subjectCode) {
              <tr>
                <td>{{ s.subjectName }}</td>
                <td>{{ s.coefficient }}</td>
                <td>{{ s.average | number: '1.2-2' }}</td>
                <td>
                  <span [class]="'badge ' + (s.validated ? 'badge-success' : 'badge-danger')">
                    {{ s.validated ? 'Validée' : 'Non validée' }}
                  </span>
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    } @empty {
      <p class="edu-muted">Aucun bulletin publié.</p>
    }
  `,
})
export class GradesPage {
  private readonly api = inject(ApiService);

  protected readonly reasons = GRADE_REASON;
  protected readonly time = shortTime;
  protected readonly exams = signal<Exam[]>([]);
  protected readonly grades = signal<Grade[]>([]);
  protected readonly reportCards = signal<ReportCard[]>([]);
  protected readonly error = signal<string | null>(null);

  constructor() {
    const fail = (e: unknown) => this.error.set(errorMessage(e));
    this.api.get<Exam[]>('/student/exams').subscribe({ next: (e) => this.exams.set(e), error: fail });
    this.api.get<Grade[]>('/student/grades').subscribe({ next: (g) => this.grades.set(g), error: fail });
    this.api.get<ReportCard[]>('/student/report-cards').subscribe({ next: (r) => this.reportCards.set(r), error: fail });
  }
}
