import { HttpResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService, PageResponse, RoleSummary, SessionService, UserAccount, errorMessage } from '@edu/core';

interface UserDetail {
  user: UserAccount;
  roles: RoleSummary[];
}

@Component({
  selector: 'edu-users-page',
  imports: [FormsModule],
  template: `
    <h1>Utilisateurs</h1>
    <div class="actions">
      <input name="search" [(ngModel)]="search" (keyup.enter)="load()" placeholder="Identifiant ou e-mail" />
      <button type="button" class="btn btn-secondary" (click)="load()">Rechercher</button>
    </div>
    @if (message()) {
      <p [class]="messageIsError() ? 'edu-error' : 'edu-success'">{{ message() }}</p>
    }
    <table class="data">
      <thead>
        <tr><th>Identifiant</th><th>E-mail</th><th>Type</th><th>Actif</th><th></th></tr>
      </thead>
      <tbody>
        @for (u of users(); track u.id) {
          <tr>
            <td>{{ u.username }}</td>
            <td>{{ u.email }}</td>
            <td>{{ u.personType ?? '—' }}</td>
            <td>{{ u.active ? 'Oui' : 'Non' }}</td>
            <td><button type="button" class="btn btn-secondary" (click)="open(u)">Rôles</button></td>
          </tr>
        }
      </tbody>
    </table>

    @if (selected(); as s) {
      <div class="card">
        <h2>{{ s.user.username }}</h2>
        <p>Rôles : {{ roleNames(s) }}</p>
        @if (canWrite) {
          <div class="actions">
            <select name="role" [(ngModel)]="roleId">
              <option value="">Choisir un rôle…</option>
              @for (r of roles(); track r.id) {
                <option [value]="r.id">{{ r.name }}</option>
              }
            </select>
            <button type="button" class="btn" [disabled]="!roleId" (click)="assign(s.user)">Attribuer</button>
          </div>
          <p class="edu-muted">Un rôle administratif demande l'accord d'un second Super Admin.</p>
        }
      </div>
    }
  `,
})
export class UsersPage {
  private readonly api = inject(ApiService);
  protected readonly canWrite = inject(SessionService).can('UserAccount', 'WRITE');

  protected readonly users = signal<UserAccount[]>([]);
  protected readonly roles = signal<RoleSummary[]>([]);
  protected readonly selected = signal<UserDetail | null>(null);
  protected readonly message = signal<string | null>(null);
  protected readonly messageIsError = signal(false);
  protected search = '';
  protected roleId = '';

  constructor() {
    this.load();
    this.api.get<RoleSummary[]>('/administration/roles').subscribe({ next: (roles) => this.roles.set(roles) });
  }

  protected load(): void {
    this.api.get<PageResponse<UserAccount>>('/administration/users', { search: this.search, size: 50 }).subscribe({
      next: (page) => this.users.set(page.content),
      error: (e) => this.show(errorMessage(e), true),
    });
  }

  protected open(user: UserAccount): void {
    this.api.get<UserDetail>(`/administration/users/${user.id}`).subscribe({
      next: (detail) => this.selected.set(detail),
      error: (e) => this.show(errorMessage(e), true),
    });
  }

  protected roleNames(detail: UserDetail): string {
    return detail.roles.map((r) => r.name).join(', ') || 'aucun';
  }

  protected assign(user: UserAccount): void {
    this.api.postWithResponse(`/administration/users/${user.id}/roles`, { roleId: this.roleId }).subscribe({
      next: (response: HttpResponse<unknown>) => {
        this.show(
          response.status === 202 ? 'Demande créée : un autre Super Admin doit l’approuver.' : 'Rôle attribué.',
          false,
        );
        this.roleId = '';
        this.open(user);
      },
      error: (e) => this.show(errorMessage(e), true),
    });
  }

  private show(text: string, isError: boolean): void {
    this.message.set(text);
    this.messageIsError.set(isError);
  }
}
