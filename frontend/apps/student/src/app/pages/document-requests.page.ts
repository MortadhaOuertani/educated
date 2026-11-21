import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  ApiService,
  DOCUMENT_LANGUAGE,
  DOCUMENT_STATUS,
  DocumentRequest,
  DocumentType,
  errorMessage,
} from '@edu/core';

const MAX_PROOF_BYTES = 5 * 1024 * 1024;
const PROOF_TYPES = ['application/pdf', 'image/jpeg', 'image/png'];

/** Returns why the proof file is refused, or null when it is fine (same rules as the backend). */
export function proofProblem(file: File | null): string | null {
  if (!file) {
    return null;
  }
  if (!PROOF_TYPES.includes(file.type)) {
    return 'Le justificatif doit être un PDF, un JPG ou un PNG.';
  }
  if (file.size > MAX_PROOF_BYTES) {
    return 'Le justificatif ne doit pas dépasser 5 Mo.';
  }
  return null;
}

@Component({
  selector: 'edu-document-requests-page',
  imports: [FormsModule, DatePipe],
  template: `
    <h1>Demandes de documents</h1>

    <form class="card" (ngSubmit)="submit()">
      <h2>Nouvelle demande</h2>
      <div class="form">
        <div class="field">
          <label for="type">Document</label>
          <select id="type" name="type" [(ngModel)]="documentTypeId" required>
            <option value="">Choisir un document…</option>
            @for (type of types(); track type.id) {
              <option [value]="type.id">{{ type.label.fr }}</option>
            }
          </select>
        </div>
        <div class="field">
          <label for="reason">Motif</label>
          <input id="reason" name="reason" [(ngModel)]="reason" placeholder="Ex. : dossier de bourse" />
        </div>
        <div class="field">
          <label for="proof">Justificatif (facultatif, PDF/JPG/PNG, 5 Mo)</label>
          <input id="proof" type="file" accept=".pdf,.jpg,.jpeg,.png" (change)="pickProof($event)" />
        </div>
        <button class="btn" type="submit" [disabled]="!documentTypeId || sending()">Envoyer la demande</button>
      </div>
      @if (formError()) {
        <p class="edu-error">{{ formError() }}</p>
      }
      @if (sent()) {
        <p class="edu-success">Votre demande a été déposée.</p>
      }
    </form>

    <h2>Mes demandes</h2>
    @if (requests().length === 0) {
      <p class="edu-muted">Aucune demande pour le moment.</p>
    } @else {
      <table class="data">
        <thead>
          <tr><th>Document</th><th>Langue</th><th>Demandée le</th><th>Statut</th></tr>
        </thead>
        <tbody>
          @for (request of requests(); track request.id) {
            <tr>
              <td>{{ request.documentType.label.fr }}</td>
              <td>{{ languages[request.documentType.language] }}</td>
              <td>{{ request.requestedAt | date: 'dd/MM/yyyy' }}</td>
              <td>
                <span [class]="'badge ' + statuses[request.status].badge">{{ statuses[request.status].label }}</span>
                @if (request.rejectionReason) {
                  <div class="edu-muted">{{ request.rejectionReason }}</div>
                }
              </td>
            </tr>
          }
        </tbody>
      </table>
      <p class="edu-muted">Une demande « Traitée » est prête : retirez votre document à la scolarité.</p>
    }
  `,
})
export class DocumentRequestsPage {
  private readonly api = inject(ApiService);

  protected readonly statuses = DOCUMENT_STATUS;
  protected readonly languages = DOCUMENT_LANGUAGE;
  protected readonly types = signal<DocumentType[]>([]);
  protected readonly requests = signal<DocumentRequest[]>([]);
  protected readonly sending = signal(false);
  protected readonly sent = signal(false);
  protected readonly formError = signal<string | null>(null);

  protected documentTypeId = '';
  protected reason = '';
  private proof: File | null = null;

  constructor() {
    this.api.get<DocumentType[]>('/scolarite/document-types').subscribe({
      next: (types) => this.types.set(types),
      error: (e) => this.formError.set(errorMessage(e)),
    });
    this.loadRequests();
  }

  protected pickProof(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0] ?? null;
    this.proof = file;
    this.formError.set(proofProblem(file));
  }

  protected submit(): void {
    const problem = proofProblem(this.proof);
    if (problem || !this.documentTypeId) {
      this.formError.set(problem ?? 'Choisissez un document.');
      return;
    }
    const form = new FormData();
    form.append('documentTypeId', this.documentTypeId);
    if (this.reason.trim()) {
      form.append('reason', this.reason.trim());
    }
    if (this.proof) {
      form.append('proof', this.proof);
    }
    this.sending.set(true);
    this.sent.set(false);
    this.formError.set(null);
    this.api.postForm<DocumentRequest>('/student/document-requests', form).subscribe({
      next: () => {
        this.sending.set(false);
        this.sent.set(true);
        this.documentTypeId = '';
        this.reason = '';
        this.loadRequests();
      },
      error: (e) => {
        this.sending.set(false);
        this.formError.set(errorMessage(e));
      },
    });
  }

  private loadRequests(): void {
    this.api.get<DocumentRequest[]>('/student/document-requests').subscribe({
      next: (requests) => this.requests.set(requests),
      error: (e) => this.formError.set(errorMessage(e)),
    });
  }
}
