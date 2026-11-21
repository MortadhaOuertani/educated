import { Component, inject, signal } from '@angular/core';
import { ApiService, FeatureToggle, errorMessage } from '@edu/core';

/** The Paiements section can be switched off by the establishment (it is off today). */
@Component({
  selector: 'edu-payments-page',
  template: `
    <h1>Paiements</h1>
    @if (error()) {
      <p class="edu-error">{{ error() }}</p>
    } @else if (disabledMessage()) {
      <div class="card">
        <p>{{ disabledMessage() }}</p>
      </div>
    } @else {
      <p class="edu-muted">Aucun paiement à afficher.</p>
    }
  `,
})
export class PaymentsPage {
  private readonly api = inject(ApiService);
  protected readonly disabledMessage = signal<string | null>(null);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.api.get<FeatureToggle[]>('/parametrage/feature-toggles', { portal: 'STUDENT' }).subscribe({
      next: (toggles) => {
        const payments = toggles.find((t) => t.featureKey === 'student.payments');
        if (payments && !payments.enabled) {
          this.disabledMessage.set(payments.disabledMessage ?? 'Cette rubrique est désactivée.');
        }
      },
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
