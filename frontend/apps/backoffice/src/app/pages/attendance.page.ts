import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ApiService, SessionService, errorMessage } from '@edu/core';

interface AbsenceRequest {
  id: string;
  matricule: string | null;
  fullName: string | null;
  fromDateTime: string;
  toDateTime: string;
  reason: string;
  hasProof: boolean;
  status: string;
  createdAt: string;
}

/** Assiduité: justification requests of the students; approving one justifies the absences of the period. */
@Component({
  selector: 'edu-attendance-page',
  imports: [DatePipe],
  template: `
    <h1>Assiduité</h1>
    <h2>Demandes de justification en attente</h2>
    @if (error()) {
      <p class="edu-error">{{ error() }}</p>
    }
    <table class="data">
      <thead><tr><th>Apprenant</th><th>Période</th><th>Motif</th><th>Justificatif</th><th></th></tr></thead>
      <tbody>
        @for (r of requests(); track r.id) {
          <tr>
            <td>{{ r.fullName }} <span class="edu-muted">{{ r.matricule }}</span></td>
            <td>{{ r.fromDateTime | date: 'dd/MM/yyyy HH:mm' }} → {{ r.toDateTime | date: 'dd/MM/yyyy HH:mm' }}</td>
            <td>{{ r.reason }}</td>
            <td>{{ r.hasProof ? 'Oui' : 'Non' }}</td>
            <td>
              @if (canReview) {
                <button type="button" class="btn" (click)="review(r, 'APPROVED')">Accepter</button>
                <button type="button" class="btn btn-danger" (click)="review(r, 'REJECTED')">Refuser</button>
              }
            </td>
          </tr>
        } @empty {
          <tr><td class="edu-muted">Aucune demande en attente.</td></tr>
        }
      </tbody>
    </table>
  `,
})
export class AttendancePage {
  private readonly api = inject(ApiService);

  protected readonly canReview = inject(SessionService).can('StudentAbsenceRequest', 'WRITE');
  protected readonly requests = signal<AbsenceRequest[]>([]);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.load();
  }

  protected review(request: AbsenceRequest, status: 'APPROVED' | 'REJECTED'): void {
    this.api.patch(`/assiduite/absence-requests/${request.id}`, { status }).subscribe({
      next: () => this.load(),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  private load(): void {
    this.api.get<AbsenceRequest[]>('/assiduite/absence-requests', { status: 'PENDING' }).subscribe({
      next: (list) => this.requests.set(list),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
