import { DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService, SessionService, errorMessage } from '@edu/core';
import { CurrentYear } from '../current-year';

interface Grid {
  id: string;
  subjectName: string;
  assessmentType: string;
  status: 'OPEN' | 'IN_PROGRESS' | 'CONFIRMED' | 'PUBLISHED';
  rows: unknown[];
}

interface ReportCard {
  id: string;
  matricule: string | null;
  fullName: string | null;
  average: number | null;
  rankInClass: number | null;
  creditsEarned: number;
  appreciation: string | null;
  published: boolean;
}

export const GRID_STATUS: Record<Grid['status'], { label: string; badge: string }> = {
  OPEN: { label: 'Ouverte', badge: 'badge-info' },
  IN_PROGRESS: { label: 'En saisie', badge: 'badge-warning' },
  CONFIRMED: { label: 'Confirmée', badge: 'badge-success' },
  PUBLISHED: { label: 'Publiée', badge: 'badge-success' },
};

/** Examens et notes: publish the confirmed grids of a class, then generate and publish the report cards. */
@Component({
  selector: 'edu-exams-page',
  imports: [DecimalPipe, FormsModule],
  template: `
    <h1>Examens et notes</h1>
    @if (error()) {
      <p class="edu-error">{{ error() }}</p>
    }
    <div class="actions">
      <label for="class">Classe</label>
      <select id="class" name="class" [(ngModel)]="classId" (ngModelChange)="load()">
        <option value="">Choisir…</option>
        @for (c of year.classes(); track c.id) {
          <option [value]="c.id">{{ c.code }}</option>
        }
      </select>
      <label for="period">Semestre</label>
      <select id="period" name="period" [(ngModel)]="periodId" (ngModelChange)="load()">
        @for (p of year.year()?.periods ?? []; track p.id) {
          <option [value]="p.id">{{ p.name.fr }}</option>
        }
      </select>
    </div>

    @if (classId && periodId) {
      <h2>Grilles de notes</h2>
      <table class="data">
        <tbody>
          @for (g of grids(); track g.id) {
            <tr>
              <td>{{ g.subjectName }}</td>
              <td>{{ g.assessmentType }}</td>
              <td><span [class]="'badge ' + statuses[g.status].badge">{{ statuses[g.status].label }}</span></td>
              <td>
                @if (canPublish && g.status === 'CONFIRMED') {
                  <button type="button" class="btn" (click)="publishGrid(g)">Publier</button>
                }
              </td>
            </tr>
          } @empty {
            <tr><td class="edu-muted">Aucune grille pour cette classe.</td></tr>
          }
        </tbody>
      </table>

      <h2>Bulletins</h2>
      <div class="actions">
        <button type="button" class="btn btn-secondary" (click)="generate()">Calculer les bulletins</button>
        <button type="button" class="btn" [disabled]="cards().length === 0" (click)="publishCards()">Publier les bulletins</button>
      </div>
      <table class="data">
        <thead><tr><th>Rang</th><th>Apprenant</th><th>Moyenne</th><th>Crédits</th><th>Appréciation</th><th></th></tr></thead>
        <tbody>
          @for (c of cards(); track c.id) {
            <tr>
              <td>{{ c.rankInClass }}</td>
              <td>{{ c.fullName }} <span class="edu-muted">{{ c.matricule }}</span></td>
              <td>{{ c.average | number: '1.2-2' }}</td>
              <td>{{ c.creditsEarned }}</td>
              <td>{{ c.appreciation }}</td>
              <td>{{ c.published ? 'Publié' : '' }}</td>
            </tr>
          }
        </tbody>
      </table>
    }
  `,
})
export class ExamsPage {
  private readonly api = inject(ApiService);
  private readonly session = inject(SessionService);

  protected readonly year = inject(CurrentYear);
  protected readonly canPublish = this.session.can('GradeGrid', 'WRITE');
  protected readonly statuses = GRID_STATUS;
  protected readonly grids = signal<Grid[]>([]);
  protected readonly cards = signal<ReportCard[]>([]);
  protected readonly error = signal<string | null>(null);
  protected classId = '';
  protected periodId = '';

  constructor() {
    this.year.load();
  }

  protected load(): void {
    if (!this.periodId) {
      this.periodId = this.year.year()?.periods?.[0]?.id ?? '';
    }
    if (!this.classId || !this.periodId) {
      return;
    }
    const query = { schoolClassId: this.classId, academicPeriodId: this.periodId };
    this.api.get<Grid[]>('/examens/grade-grids', query).subscribe({
      next: (list) => this.grids.set(list),
      error: (e) => this.error.set(errorMessage(e)),
    });
    this.api.get<ReportCard[]>('/examens/report-cards', query).subscribe({
      next: (list) => this.cards.set(list),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected publishGrid(grid: Grid): void {
    this.api.post(`/examens/grade-grids/${grid.id}/publish`, {}).subscribe({
      next: () => this.load(),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected generate(): void {
    this.post('/examens/report-cards/generate');
  }

  protected publishCards(): void {
    this.post('/examens/report-cards/publish');
  }

  private post(path: string): void {
    this.api.post<ReportCard[]>(path, { schoolClassId: this.classId, academicPeriodId: this.periodId }).subscribe({
      next: (cards) => {
        this.error.set(null);
        this.cards.set(cards);
      },
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
