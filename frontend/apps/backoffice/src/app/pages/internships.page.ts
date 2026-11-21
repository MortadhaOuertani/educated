import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService, PersonInfo, SessionService, errorMessage } from '@edu/core';

interface Internship {
  id: string;
  referenceNumber: string;
  title: string;
  studentName: string | null;
  matricule: string | null;
  companyName: string;
  startDate: string;
  endDate: string;
  status: string;
  academicSupervisorName: string | null;
}

interface Teacher {
  id: string;
  person: PersonInfo;
}

/** Stages: approve or refuse declared internships, name the supervisor, give the final defense authorization. */
@Component({
  selector: 'edu-internships-page',
  imports: [DatePipe, FormsModule],
  template: `
    <h1>Stages</h1>
    <div class="actions">
      <label for="status">Statut</label>
      <select id="status" name="status" [(ngModel)]="status" (ngModelChange)="load()">
        <option value="">Tous</option>
        <option value="PENDING_APPROVAL">En attente de validation</option>
        <option value="AUTHORIZED_BY_SUPERVISOR">Autorisés par l'encadrant</option>
        <option value="VALIDATED">Validés</option>
      </select>
    </div>
    @if (error()) {
      <p class="edu-error">{{ error() }}</p>
    }
    <table class="data">
      <thead><tr><th>Apprenant</th><th>Sujet</th><th>Période</th><th>Statut</th><th></th></tr></thead>
      <tbody>
        @for (i of internships(); track i.id) {
          <tr>
            <td>{{ i.studentName }} <span class="edu-muted">{{ i.matricule }}</span></td>
            <td>{{ i.title }}<div class="edu-muted">{{ i.companyName }} · {{ i.referenceNumber }}</div></td>
            <td>{{ i.startDate | date: 'dd/MM/yyyy' }} → {{ i.endDate | date: 'dd/MM/yyyy' }}</td>
            <td>{{ i.status }}<div class="edu-muted">{{ i.academicSupervisorName }}</div></td>
            <td>
              @if (canWrite && i.status === 'PENDING_APPROVAL') {
                <select [(ngModel)]="supervisorId" [name]="'supervisor-' + i.id" aria-label="Encadrant académique">
                  <option value="">Encadrant…</option>
                  @for (t of teachers(); track t.id) {
                    <option [value]="t.id">{{ t.person.lastName }} {{ t.person.firstName }}</option>
                  }
                </select>
                <button type="button" class="btn" [disabled]="!supervisorId" (click)="approve(i)">Valider</button>
                <button type="button" class="btn btn-danger" (click)="refuse(i)">Refuser</button>
              }
              @if (canWrite && i.status === 'AUTHORIZED_BY_SUPERVISOR') {
                <button type="button" class="btn" (click)="authorize(i)">Autoriser à soutenir</button>
              }
            </td>
          </tr>
        } @empty {
          <tr><td class="edu-muted">Aucun stage.</td></tr>
        }
      </tbody>
    </table>
    @if (canWrite) {
      <div class="field">
        <label for="refusal">Motif de refus</label>
        <input id="refusal" name="refusal" [(ngModel)]="refusalReason" placeholder="Utilisé par le bouton Refuser" />
      </div>
    }
  `,
})
export class InternshipsPage {
  private readonly api = inject(ApiService);

  protected readonly canWrite = inject(SessionService).can('Internship', 'WRITE');
  protected readonly internships = signal<Internship[]>([]);
  protected readonly teachers = signal<Teacher[]>([]);
  protected readonly error = signal<string | null>(null);
  protected status = 'PENDING_APPROVAL';
  protected supervisorId = '';
  protected refusalReason = '';

  constructor() {
    this.load();
    if (this.canWrite) {
      this.api.get<Teacher[]>('/pedagogie/teachers').subscribe({
        next: (list) => this.teachers.set(list),
        error: (e) => this.error.set(errorMessage(e)),
      });
    }
  }

  protected load(): void {
    this.api.get<Internship[]>('/stages/internships', { status: this.status }).subscribe({
      next: (list) => this.internships.set(list),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected approve(internship: Internship): void {
    this.act(`/stages/internships/${internship.id}/approve`, { academicSupervisorId: this.supervisorId });
  }

  protected refuse(internship: Internship): void {
    if (!this.refusalReason.trim()) {
      this.error.set('Écrivez le motif de refus avant de refuser.');
      return;
    }
    this.act(`/stages/internships/${internship.id}/refuse`, { reason: this.refusalReason.trim() });
  }

  protected authorize(internship: Internship): void {
    this.act(`/stages/internships/${internship.id}/authorize-defense`, {});
  }

  private act(path: string, body: unknown): void {
    this.api.post(path, body).subscribe({
      next: () => {
        this.error.set(null);
        this.load();
      },
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
