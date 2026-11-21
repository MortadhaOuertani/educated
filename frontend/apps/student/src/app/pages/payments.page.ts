import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ApiService, FeatureToggle, errorMessage } from '@edu/core';

interface Installment {
  id: string;
  number: number;
  dueDate: string;
  amount: number;
  paidAmount: number;
  status: 'DUE' | 'PARTIALLY_PAID' | 'PAID' | 'OVERDUE';
}

interface Account {
  plans: { id: string; totalAmount: number; paidAmount: number; status: string; installments: Installment[] }[];
  payments: { id: string; amount: number; paidAt: string; method: string; receiptNumber: string }[];
}

export const INSTALLMENT_STATUS: Record<Installment['status'], { label: string; badge: string }> = {
  DUE: { label: 'À payer', badge: 'badge-info' },
  PARTIALLY_PAID: { label: 'Partiellement payée', badge: 'badge-warning' },
  PAID: { label: 'Payée', badge: 'badge-success' },
  OVERDUE: { label: 'En retard', badge: 'badge-danger' },
};

/** The Paiements section can be switched off by the establishment; when on, it shows fees and receipts. */
@Component({
  selector: 'edu-payments-page',
  imports: [DatePipe, DecimalPipe],
  template: `
    <h1>Paiements</h1>
    @if (error()) {
      <p class="edu-error">{{ error() }}</p>
    } @else if (disabledMessage()) {
      <div class="card">
        <p>{{ disabledMessage() }}</p>
      </div>
    } @else if (account(); as data) {
      @for (plan of data.plans; track plan.id) {
        <p>
          Total : <strong>{{ plan.totalAmount | number: '1.3-3' }} TND</strong> · Payé :
          {{ plan.paidAmount | number: '1.3-3' }} TND
        </p>
        <table class="data">
          <thead><tr><th>Échéance</th><th>Date</th><th>Montant</th><th>Payé</th><th>Statut</th></tr></thead>
          <tbody>
            @for (i of plan.installments; track i.id) {
              <tr>
                <td>{{ i.number }}</td>
                <td>{{ i.dueDate | date: 'dd/MM/yyyy' }}</td>
                <td>{{ i.amount | number: '1.3-3' }}</td>
                <td>{{ i.paidAmount | number: '1.3-3' }}</td>
                <td><span [class]="'badge ' + statuses[i.status].badge">{{ statuses[i.status].label }}</span></td>
              </tr>
            }
          </tbody>
        </table>
      } @empty {
        <p class="edu-muted">Aucun frais pour le moment.</p>
      }
      <h2>Reçus</h2>
      <table class="data">
        <tbody>
          @for (p of data.payments; track p.id) {
            <tr>
              <td>{{ p.receiptNumber }}</td>
              <td>{{ p.paidAt | date: 'dd/MM/yyyy' }}</td>
              <td>{{ p.amount | number: '1.3-3' }} TND</td>
              <td>{{ p.method }}</td>
            </tr>
          }
        </tbody>
      </table>
    }
  `,
})
export class PaymentsPage {
  private readonly api = inject(ApiService);

  protected readonly statuses = INSTALLMENT_STATUS;
  protected readonly disabledMessage = signal<string | null>(null);
  protected readonly account = signal<Account | null>(null);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.api.get<FeatureToggle[]>('/parametrage/feature-toggles', { portal: 'STUDENT' }).subscribe({
      next: (toggles) => {
        const payments = toggles.find((t) => t.featureKey === 'student.payments');
        if (payments && !payments.enabled) {
          this.disabledMessage.set(payments.disabledMessage ?? 'Cette rubrique est désactivée.');
        } else {
          this.loadAccount();
        }
      },
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  private loadAccount(): void {
    this.api.get<Account>('/student/payments').subscribe({
      next: (account) => this.account.set(account),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
