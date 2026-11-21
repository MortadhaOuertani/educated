import { Component, inject, signal } from '@angular/core';
import { ApiService, PermissionPolicy, RoleDetail, RoleSummary, errorMessage } from '@edu/core';

const POLICY_LABELS: Record<PermissionPolicy, string> = {
  DENY_ALL_BY_DEFAULT: 'Tout refuser par défaut',
  READ_ONLY_ALL_BY_DEFAULT: 'Lecture seule par défaut',
  ALLOW_ALL_BY_DEFAULT: 'Tout autoriser par défaut',
};

/** The roles and their permission layers (read-only view; editing comes with the full role editor screen). */
@Component({
  selector: 'edu-roles-page',
  template: `
    <h1>Rôles</h1>
    @if (error()) {
      <p class="edu-error">{{ error() }}</p>
    }
    <table class="data">
      <thead>
        <tr><th>Nom</th><th>Politique</th><th>Administratif</th><th></th></tr>
      </thead>
      <tbody>
        @for (role of roles(); track role.id) {
          <tr>
            <td>{{ role.name }}<div class="edu-muted">{{ role.description }}</div></td>
            <td>{{ policies[role.permissionPolicy] }}</td>
            <td>{{ role.administrative ? 'Oui' : 'Non' }}</td>
            <td><button type="button" class="btn btn-secondary" (click)="open(role)">Détail</button></td>
          </tr>
        }
      </tbody>
    </table>

    @if (detail(); as d) {
      <div class="card">
        <h2>{{ d.role.name }}</h2>
        <p>Modules visibles : {{ d.visibleModules.join(', ') || 'aucun' }}</p>
        @if (d.typePermissions.length > 0) {
          <table class="data">
            <thead>
              <tr><th>Objet</th><th>Lire</th><th>Modifier</th><th>Créer</th><th>Supprimer</th></tr>
            </thead>
            <tbody>
              @for (p of d.typePermissions; track p.targetType) {
                <tr><td>{{ p.targetType }}</td><td>{{ p.read }}</td><td>{{ p.write }}</td><td>{{ p.create }}</td><td>{{ p.delete }}</td></tr>
              }
            </tbody>
          </table>
        }
        <p>Actions interdites : {{ d.deniedActions.join(', ') || 'aucune' }}</p>
      </div>
    }
  `,
})
export class RolesPage {
  private readonly api = inject(ApiService);
  protected readonly policies = POLICY_LABELS;
  protected readonly roles = signal<RoleSummary[]>([]);
  protected readonly detail = signal<RoleDetail | null>(null);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.api.get<RoleSummary[]>('/administration/roles').subscribe({
      next: (roles) => this.roles.set(roles),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected open(role: RoleSummary): void {
    this.api.get<RoleDetail>(`/administration/roles/${role.id}`).subscribe({
      next: (detail) => this.detail.set(detail),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
