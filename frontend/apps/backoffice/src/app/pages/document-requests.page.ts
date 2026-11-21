import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  ApiService,
  DOCUMENT_LANGUAGE,
  DOCUMENT_STATUS,
  DocumentRequest,
  DocumentRequestStatus,
  PageResponse,
  SessionService,
  errorMessage,
} from '@edu/core';

/** The next statuses a request may take (same rule as the backend). */
export function nextStatuses(status: DocumentRequestStatus): DocumentRequestStatus[] {
  switch (status) {
    case 'SUBMITTED':
      return ['IN_PROGRESS', 'REJECTED'];
    case 'IN_PROGRESS':
      return ['DONE', 'REJECTED'];
    default:
      return [];
  }
}

const ACTION_LABELS: Record<DocumentRequestStatus, string> = {
  SUBMITTED: '',
  IN_PROGRESS: 'Prendre en charge',
  DONE: 'Marquer traitée',
  REJECTED: 'Refuser',
};

@Component({
  selector: 'edu-document-requests-page',
  imports: [FormsModule, DatePipe],
  template: `
    <h1>Demandes de documents</h1>
    <div class="actions">
      <label for="status">Statut</label>
      <select id="status" name="status" [(ngModel)]="status" (change)="load()">
        <option value="">Toutes</option>
        <option value="SUBMITTED">Déposées</option>
        <option value="IN_PROGRESS">En traitement</option>
        <option value="DONE">Traitées</option>
        <option value="REJECTED">Refusées</option>
      </select>
    </div>
    @if (error()) {
      <p class="edu-error">{{ error() }}</p>
    }
    <table class="data">
      <thead>
        <tr><th>Apprenant</th><th>Document</th><th>Motif</th><th>Date</th><th>Statut</th><th></th></tr>
      </thead>
      <tbody>
        @for (r of requests(); track r.id) {
          <tr>
            <td>{{ r.studentName }}<div class="edu-muted">{{ r.matricule }}</div></td>
            <td>{{ r.documentType.label.fr }}<div class="edu-muted">{{ languages[r.documentType.language] }}</div></td>
            <td>{{ r.reason }}</td>
            <td>{{ r.requestedAt | date: 'dd/MM/yyyy HH:mm' }}</td>
            <td><span [class]="'badge ' + statuses[r.status].badge">{{ statuses[r.status].label }}</span></td>
            <td>
              @if (canProcess) {
                <div class="actions">
                  @for (next of next(r.status); track next) {
                    <button
                      type="button"
                      [class]="next === 'REJECTED' ? 'btn btn-danger' : 'btn'"
                      (click)="change(r, next)"
                    >
                      {{ actionLabels[next] }}
                    </button>
                  }
                </div>
              }
            </td>
          </tr>
        } @empty {
          <tr><td colspan="6" class="edu-muted">Aucune demande.</td></tr>
        }
      </tbody>
    </table>
  `,
})
export class DocumentRequestsPage {
  private readonly api = inject(ApiService);
  protected readonly canProcess = inject(SessionService).can('DocumentRequest', 'WRITE');

  protected readonly statuses = DOCUMENT_STATUS;
  protected readonly languages = DOCUMENT_LANGUAGE;
  protected readonly actionLabels = ACTION_LABELS;
  protected readonly next = nextStatuses;
  protected readonly requests = signal<DocumentRequest[]>([]);
  protected readonly error = signal<string | null>(null);
  protected status = 'SUBMITTED';

  constructor() {
    this.load();
  }

  protected load(): void {
    this.api
      .get<PageResponse<DocumentRequest>>('/scolarite/document-requests', { status: this.status, size: 100 })
      .subscribe({
        next: (page) => {
          this.requests.set(page.content);
          this.error.set(null);
        },
        error: (e) => this.error.set(errorMessage(e)),
      });
  }

  protected change(request: DocumentRequest, status: DocumentRequestStatus): void {
    let rejectionReason: string | null = null;
    if (status === 'REJECTED') {
      rejectionReason = window.prompt('Motif du refus :');
      if (!rejectionReason) {
        return;
      }
    }
    this.api.patch(`/scolarite/document-requests/${request.id}/status`, { status, rejectionReason }).subscribe({
      next: () => this.load(),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
