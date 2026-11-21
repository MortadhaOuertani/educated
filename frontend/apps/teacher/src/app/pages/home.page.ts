import { Component, inject } from '@angular/core';
import { SessionService } from '@edu/core';

/** Teacher home. Calls, grades, materials and internships arrive with the pedagogie and assiduite modules. */
@Component({
  selector: 'edu-home-page',
  template: `
    <h1>Bonjour {{ session.displayName() }}</h1>
    <div class="card">
      <p>Votre espace enseignant est prêt.</p>
      <p class="edu-muted">
        Les appels, la saisie des notes, les supports de cours et les stages arrivent avec les prochains modules.
      </p>
    </div>
  `,
})
export class HomePage {
  protected readonly session = inject(SessionService);
}
