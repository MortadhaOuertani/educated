import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService, LocalizedText, errorMessage } from '@edu/core';

interface Internship {
  id: string;
  referenceNumber: string;
  title: string;
  startDate: string;
  endDate: string;
  status: string;
  nature: LocalizedText;
  companyName: string;
  academicSupervisorName: string | null;
  refusalReason: string | null;
  documents: { id: string; documentType: string }[];
  history: { toStatus: string; at: string | null }[];
  defense: { date: string; grade: number | null; mention: string | null } | null;
}

interface Options {
  natures: { id: string; code: string; label: LocalizedText; requiresDefense: boolean }[];
  companies: { id: string; name: string }[];
  defenseSessions: { id: string; name: string; requestsOpenFrom: string | null; requestsOpenTo: string | null }[];
}

/** The steps of an internship in French. */
export const INTERNSHIP_STATUS: Record<string, string> = {
  PENDING_APPROVAL: 'En attente de validation',
  APPROVED: 'Validé',
  REFUSED: 'Refusé',
  DEFENSE_REQUESTED: 'Soutenance demandée',
  AUTHORIZED_BY_SUPERVISOR: "Autorisé par l'encadrant",
  AUTHORIZED_TO_DEFEND: 'Autorisé à soutenir',
  DEFENDED: 'Soutenu',
  VALIDATED: 'Stage validé',
};

/** Mon stage: declare an internship, upload the report and request the defense. */
@Component({
  selector: 'edu-internship-page',
  imports: [DatePipe, FormsModule],
  template: `
    <h1>Mon stage</h1>
    @if (error()) {
      <p class="edu-error">{{ error() }}</p>
    }
    @for (i of internships(); track i.id) {
      <div class="card">
        <h2>{{ i.title }}</h2>
        <p>
          {{ i.nature.fr }} · {{ i.companyName }} · du {{ i.startDate | date: 'dd/MM/yyyy' }} au
          {{ i.endDate | date: 'dd/MM/yyyy' }} · <span class="edu-muted">{{ i.referenceNumber }}</span>
        </p>
        <p><span class="badge badge-info">{{ statuses[i.status] ?? i.status }}</span></p>
        @if (i.refusalReason) {
          <p class="edu-error">{{ i.refusalReason }}</p>
        }
        @if (i.academicSupervisorName) {
          <p>Encadrant académique : {{ i.academicSupervisorName }}</p>
        }
        @if (i.defense) {
          <p>
            Soutenance le {{ i.defense.date | date: 'dd/MM/yyyy' }}
            @if (i.defense.grade !== null) {
              · Note : <strong>{{ i.defense.grade }}</strong> / 20
            }
          </p>
        }
        <p class="edu-muted">Documents : {{ documentList(i) }}</p>
        <div class="actions">
          <label for="report-{{ i.id }}">Déposer le rapport (PDF)</label>
          <input id="report-{{ i.id }}" type="file" accept=".pdf" (change)="upload(i, $event)" />
          @if (i.status === 'APPROVED') {
            <select [(ngModel)]="sessionId" [name]="'session-' + i.id" aria-label="Session de soutenance">
              <option value="">Session de soutenance…</option>
              @for (s of options()?.defenseSessions ?? []; track s.id) {
                <option [value]="s.id">{{ s.name }}</option>
              }
            </select>
            <button type="button" class="btn" [disabled]="!sessionId" (click)="requestDefense(i)">
              Demander la soutenance
            </button>
          }
        </div>
      </div>
    }

    <form class="card" (ngSubmit)="declare()">
      <h2>Déclarer un stage</h2>
      <div class="form">
        <div class="field">
          <label for="nature">Type de stage</label>
          <select id="nature" name="nature" [(ngModel)]="natureId" required>
            <option value="">Choisir…</option>
            @for (n of options()?.natures ?? []; track n.id) {
              <option [value]="n.id">{{ n.label.fr }}</option>
            }
          </select>
        </div>
        <div class="field">
          <label for="company">Entreprise</label>
          <input id="company" name="company" [(ngModel)]="companyName" required />
        </div>
        <div class="field">
          <label for="title">Sujet</label>
          <input id="title" name="title" [(ngModel)]="title" required />
        </div>
        <div class="field">
          <label for="start">Début</label>
          <input id="start" name="start" type="date" [(ngModel)]="startDate" required />
        </div>
        <div class="field">
          <label for="end">Fin</label>
          <input id="end" name="end" type="date" [(ngModel)]="endDate" required />
        </div>
        <button class="btn" type="submit" [disabled]="!natureId || !companyName || !title || !startDate || !endDate">
          Déclarer
        </button>
      </div>
      @if (formError()) {
        <p class="edu-error">{{ formError() }}</p>
      }
    </form>
  `,
})
export class InternshipPage {
  private readonly api = inject(ApiService);

  protected readonly statuses = INTERNSHIP_STATUS;
  protected readonly internships = signal<Internship[]>([]);
  protected readonly options = signal<Options | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly formError = signal<string | null>(null);

  protected natureId = '';
  protected companyName = '';
  protected title = '';
  protected startDate = '';
  protected endDate = '';
  protected sessionId = '';

  constructor() {
    this.api.get<Options>('/student/internship-options').subscribe({
      next: (options) => this.options.set(options),
      error: (e) => this.error.set(errorMessage(e)),
    });
    this.load();
  }

  protected documentList(internship: Internship): string {
    return internship.documents.map((d) => d.documentType).join(', ') || 'aucun';
  }

  protected declare(): void {
    const existing = this.options()?.companies.find((c) => c.name.toLowerCase() === this.companyName.trim().toLowerCase());
    const body = {
      natureId: this.natureId,
      companyId: existing?.id ?? null,
      newCompany: existing ? null : { name: this.companyName.trim() },
      title: this.title,
      startDate: this.startDate,
      endDate: this.endDate,
    };
    this.api.post<Internship>('/student/internships', body).subscribe({
      next: () => {
        this.formError.set(null);
        this.natureId = '';
        this.companyName = '';
        this.title = '';
        this.load();
      },
      error: (e) => this.formError.set(errorMessage(e)),
    });
  }

  protected upload(internship: Internship, event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) {
      return;
    }
    const form = new FormData();
    form.append('documentType', 'REPORT');
    form.append('file', file);
    this.api.postForm(`/student/internships/${internship.id}/documents`, form).subscribe({
      next: () => this.load(),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected requestDefense(internship: Internship): void {
    this.api.post(`/student/internships/${internship.id}/defense-request`, { defenseSessionId: this.sessionId }).subscribe({
      next: () => this.load(),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  private load(): void {
    this.api.get<Internship[]>('/student/internships').subscribe({
      next: (list) => this.internships.set(list),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
