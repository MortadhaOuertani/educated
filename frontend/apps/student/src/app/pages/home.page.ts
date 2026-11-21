import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService, DocumentRequest, SessionService, StudentProfile, errorMessage } from '@edu/core';

@Component({
  selector: 'edu-home-page',
  imports: [RouterLink],
  template: `
    <h1>Bonjour {{ session.displayName() }}</h1>
    @if (error()) {
      <p class="edu-error">{{ error() }}</p>
    }
    <div class="grid">
      <div class="card">
        <div class="edu-muted">Ma classe</div>
        <div class="stat">{{ profile()?.currentClass?.code ?? '—' }}</div>
        <div class="edu-muted">Matricule {{ profile()?.student?.matricule }}</div>
      </div>
      <div class="card">
        <div class="edu-muted">Demandes en cours</div>
        <div class="stat">{{ openRequests() }}</div>
        <a routerLink="/demandes">Voir mes demandes</a>
      </div>
    </div>
  `,
})
export class HomePage {
  protected readonly session = inject(SessionService);
  private readonly api = inject(ApiService);

  protected readonly profile = signal<StudentProfile | null>(null);
  protected readonly requests = signal<DocumentRequest[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly openRequests = computed(
    () => this.requests().filter((r) => r.status === 'SUBMITTED' || r.status === 'IN_PROGRESS').length,
  );

  constructor() {
    this.api.get<StudentProfile>('/student/profile').subscribe({
      next: (profile) => this.profile.set(profile),
      error: (e) => this.error.set(errorMessage(e)),
    });
    this.api.get<DocumentRequest[]>('/student/document-requests').subscribe({
      next: (requests) => this.requests.set(requests),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
