import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService, errorMessage, shortTime } from '@edu/core';
import { Assignment } from './classes.page';

interface Period {
  id: string;
  code: string;
  name: string;
}

interface Row {
  studentId: string;
  matricule: string | null;
  fullName: string | null;
  score: number | null;
  reason: string | null;
}

interface Grid {
  id: string;
  subjectName: string;
  classCode: string;
  assessmentType: string;
  maxScore: number;
  status: 'OPEN' | 'IN_PROGRESS' | 'CONFIRMED' | 'PUBLISHED';
  rows: Row[];
}

interface Invigilation {
  examId: string;
  subjectName: string;
  examDate: string;
  startTime: string;
  endTime: string;
  roomCode: string | null;
  role: string;
}

/** Returns why a grade line is wrong, or null (same rules as the backend). */
export function gradeProblem(score: number | null, reason: string | null, maxScore: number): string | null {
  if (score !== null && reason) {
    return 'Donnez une note ou un motif, pas les deux.';
  }
  if (score !== null && (score < 0 || score > maxScore)) {
    return `La note doit être entre 0 et ${maxScore}.`;
  }
  return null;
}

const ASSESSMENT_TYPES = ['EXAM', 'TEST', 'SUPERVISED_TEST', 'PRACTICAL', 'PROJECT', 'ORAL'];

/** Saisie des notes: open the grid of one assessment, enter a score or a reason for each student, confirm. */
@Component({
  selector: 'edu-grades-page',
  imports: [DatePipe, FormsModule],
  template: `
    <h1>Notes</h1>
    @if (error()) {
      <p class="edu-error">{{ error() }}</p>
    }
    <form class="card" (ngSubmit)="open()">
      <div class="form">
        <div class="field">
          <label for="assignment">Matière</label>
          <select id="assignment" name="assignment" [(ngModel)]="assignmentId" required>
            <option value="">Choisir…</option>
            @for (a of assignments(); track a.id) {
              <option [value]="a.id">{{ a.subjectName.fr }} ({{ a.sessionType }})</option>
            }
          </select>
        </div>
        <div class="field">
          <label for="period">Semestre</label>
          <select id="period" name="period" [(ngModel)]="periodId" required>
            @for (p of periods(); track p.id) {
              <option [value]="p.id">{{ p.name }}</option>
            }
          </select>
        </div>
        <div class="field">
          <label for="type">Épreuve</label>
          <select id="type" name="type" [(ngModel)]="assessmentType">
            @for (t of assessmentTypes; track t) {
              <option [value]="t">{{ t }}</option>
            }
          </select>
        </div>
        <button class="btn" type="submit" [disabled]="!assignmentId || !periodId">Ouvrir la grille</button>
      </div>
    </form>

    @if (grid(); as g) {
      <h2>{{ g.subjectName }} · {{ g.classCode }} · {{ g.assessmentType }} (sur {{ g.maxScore }})</h2>
      <p><span class="badge badge-info">{{ g.status }}</span></p>
      <table class="data">
        <thead><tr><th>Apprenant</th><th>Note</th><th>Ou motif</th></tr></thead>
        <tbody>
          @for (r of rows(); track r.studentId) {
            <tr>
              <td>{{ r.fullName }} <span class="edu-muted">{{ r.matricule }}</span></td>
              <td>
                <input type="number" step="0.25" min="0" [max]="g.maxScore" [(ngModel)]="r.score"
                  [name]="'score-' + r.studentId" [disabled]="locked(g)" [attr.aria-label]="'Note de ' + r.fullName" />
              </td>
              <td>
                <select [(ngModel)]="r.reason" [name]="'reason-' + r.studentId" [disabled]="locked(g)" aria-label="Motif">
                  <option [ngValue]="null">—</option>
                  <option value="JUSTIFIED_ABSENCE">Absence justifiée</option>
                  <option value="UNJUSTIFIED_ABSENCE">Absence non justifiée</option>
                  <option value="EXEMPTED">Dispensé(e)</option>
                  <option value="FRAUD">Fraude</option>
                </select>
              </td>
            </tr>
          }
        </tbody>
      </table>
      @if (!locked(g)) {
        <div class="actions">
          <button type="button" class="btn btn-secondary" (click)="save(g)">Enregistrer</button>
          <button type="button" class="btn" (click)="confirm(g)">Confirmer la grille</button>
        </div>
      }
      @if (message()) {
        <p class="edu-success">{{ message() }}</p>
      }
    }

    <h2>Mes surveillances</h2>
    <table class="data">
      <tbody>
        @for (i of invigilations(); track i.examId) {
          <tr>
            <td>{{ i.examDate | date: 'dd/MM/yyyy' }} {{ time(i.startTime) }} – {{ time(i.endTime) }}</td>
            <td>{{ i.subjectName }}</td>
            <td>{{ i.roomCode }}</td>
            <td>{{ i.role === 'HEAD_INVIGILATOR' ? 'Responsable de salle' : 'Surveillant' }}</td>
          </tr>
        } @empty {
          <tr><td class="edu-muted">Aucune surveillance publiée.</td></tr>
        }
      </tbody>
    </table>
  `,
})
export class GradesPage {
  private readonly api = inject(ApiService);

  protected readonly assessmentTypes = ASSESSMENT_TYPES;
  protected readonly time = shortTime;
  protected readonly assignments = signal<Assignment[]>([]);
  protected readonly periods = signal<Period[]>([]);
  protected readonly invigilations = signal<Invigilation[]>([]);
  protected readonly grid = signal<Grid | null>(null);
  protected readonly rows = signal<Row[]>([]);
  protected readonly message = signal<string | null>(null);
  protected readonly error = signal<string | null>(null);

  protected assignmentId = '';
  protected periodId = '';
  protected assessmentType = 'EXAM';

  constructor() {
    const fail = (e: unknown) => this.error.set(errorMessage(e));
    this.api.get<Assignment[]>('/teacher/assignments').subscribe({ next: (a) => this.assignments.set(a), error: fail });
    this.api.get<Period[]>('/teacher/periods').subscribe({
      next: (periods) => {
        this.periods.set(periods);
        this.periodId = periods[0]?.id ?? '';
      },
      error: fail,
    });
    this.api.get<Invigilation[]>('/teacher/invigilations').subscribe({ next: (i) => this.invigilations.set(i), error: fail });
  }

  protected locked(grid: Grid): boolean {
    return grid.status === 'CONFIRMED' || grid.status === 'PUBLISHED';
  }

  protected open(): void {
    const body = { teachingAssignmentId: this.assignmentId, academicPeriodId: this.periodId, assessmentType: this.assessmentType };
    this.api.post<Grid>('/teacher/grade-grids', body).subscribe({
      next: (grid) => this.show(grid, null),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected save(grid: Grid): void {
    const problem = this.firstProblem(grid);
    if (problem) {
      this.error.set(problem);
      return;
    }
    this.api.put<Grid>(`/teacher/grade-grids/${grid.id}/grades`, { grades: this.payload() }).subscribe({
      next: (saved) => this.show(saved, 'Notes enregistrées.'),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected confirm(grid: Grid): void {
    const problem = this.firstProblem(grid);
    if (problem) {
      this.error.set(problem);
      return;
    }
    this.api.put<Grid>(`/teacher/grade-grids/${grid.id}/grades`, { grades: this.payload() }).subscribe({
      next: () =>
        this.api.post<Grid>(`/teacher/grade-grids/${grid.id}/confirm`, {}).subscribe({
          next: (confirmed) => this.show(confirmed, 'Grille confirmée. La scolarité la publiera aux étudiants.'),
          error: (e) => this.error.set(errorMessage(e)),
        }),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  private payload() {
    return this.rows().map((r) => ({ studentId: r.studentId, score: r.score ?? null, reason: r.reason || null }));
  }

  private firstProblem(grid: Grid): string | null {
    for (const row of this.rows()) {
      const problem = gradeProblem(row.score ?? null, row.reason || null, grid.maxScore);
      if (problem) {
        return `${row.fullName} : ${problem}`;
      }
    }
    return null;
  }

  private show(grid: Grid, message: string | null): void {
    this.error.set(null);
    this.grid.set(grid);
    this.rows.set(grid.rows.map((r) => ({ ...r })));
    this.message.set(message);
  }
}
