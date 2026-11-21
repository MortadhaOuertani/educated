import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService, DocumentRequest, PageResponse, SessionService, Student } from '@edu/core';

@Component({
  selector: 'edu-dashboard-page',
  imports: [RouterLink],
  template: `
    <h1>Bonjour {{ session.displayName() }}</h1>
    <div class="grid">
      @if (students() !== null) {
        <div class="card">
          <div class="edu-muted">Apprenants</div>
          <div class="stat">{{ students() }}</div>
          <a routerLink="/apprenants">Voir les apprenants</a>
        </div>
      }
      @if (pendingRequests() !== null) {
        <div class="card">
          <div class="edu-muted">Demandes de documents à traiter</div>
          <div class="stat">{{ pendingRequests() }}</div>
          <a routerLink="/demandes">Traiter les demandes</a>
        </div>
      }
    </div>
  `,
})
export class DashboardPage {
  protected readonly session = inject(SessionService);
  private readonly api = inject(ApiService);
  protected readonly students = signal<number | null>(null);
  protected readonly pendingRequests = signal<number | null>(null);

  constructor() {
    if (this.session.can('Student', 'READ')) {
      this.api.get<PageResponse<Student>>('/scolarite/students', { size: 1 }).subscribe({
        next: (page) => this.students.set(page.totalElements),
      });
    }
    if (this.session.can('DocumentRequest', 'READ')) {
      this.api
        .get<PageResponse<DocumentRequest>>('/scolarite/document-requests', { status: 'SUBMITTED', size: 1 })
        .subscribe({ next: (page) => this.pendingRequests.set(page.totalElements) });
    }
  }
}
