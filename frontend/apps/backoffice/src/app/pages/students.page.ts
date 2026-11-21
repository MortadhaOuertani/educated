import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService, PageResponse, STUDENT_STATUS, SessionService, Student, errorMessage } from '@edu/core';

@Component({
  selector: 'edu-students-page',
  imports: [FormsModule, RouterLink],
  template: `
    <h1>Apprenants</h1>

    <div class="actions">
      <input name="search" [(ngModel)]="search" (keyup.enter)="load(0)" placeholder="Nom, prénom ou matricule" />
      <button type="button" class="btn btn-secondary" (click)="load(0)">Rechercher</button>
    </div>

    @if (error()) {
      <p class="edu-error">{{ error() }}</p>
    }

    @if (page(); as p) {
      <p class="edu-muted">{{ p.totalElements }} apprenant(s)</p>
      <table class="data">
        <thead>
          <tr><th>Matricule</th><th>Nom</th><th>Prénom</th><th>Statut</th></tr>
        </thead>
        <tbody>
          @for (student of p.content; track student.id) {
            <tr>
              <td><a [routerLink]="['/apprenants', student.id]">{{ student.matricule }}</a></td>
              <td>{{ student.person.lastName }}</td>
              <td>{{ student.person.firstName }}</td>
              <td>{{ statuses[student.status] }}</td>
            </tr>
          }
        </tbody>
      </table>
      <div class="actions">
        <button type="button" class="btn btn-secondary" [disabled]="p.page === 0" (click)="load(p.page - 1)">Précédent</button>
        <span class="edu-muted">Page {{ p.page + 1 }} / {{ p.totalPages || 1 }}</span>
        <button type="button" class="btn btn-secondary" [disabled]="p.page + 1 >= p.totalPages" (click)="load(p.page + 1)">Suivant</button>
      </div>
    }

    @if (session.can('Student', 'CREATE')) {
      <form class="card" (ngSubmit)="create()">
        <h2>Nouvel apprenant</h2>
        <div class="form">
          <div class="field"><label for="matricule">Matricule</label><input id="matricule" name="matricule" [(ngModel)]="newStudent.matricule" required /></div>
          <div class="field"><label for="lastName">Nom</label><input id="lastName" name="lastName" [(ngModel)]="newStudent.lastName" required /></div>
          <div class="field"><label for="firstName">Prénom</label><input id="firstName" name="firstName" [(ngModel)]="newStudent.firstName" required /></div>
          <div class="field"><label for="email">E-mail</label><input id="email" name="email" type="email" [(ngModel)]="newStudent.email" /></div>
          <div class="field"><label for="birthDate">Date de naissance</label><input id="birthDate" name="birthDate" type="date" [(ngModel)]="newStudent.birthDate" /></div>
          <button class="btn" type="submit" [disabled]="!newStudent.matricule || !newStudent.lastName || !newStudent.firstName">Créer</button>
        </div>
        @if (createError()) {
          <p class="edu-error">{{ createError() }}</p>
        }
      </form>
    }
  `,
})
export class StudentsPage {
  protected readonly session = inject(SessionService);
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);

  protected readonly statuses = STUDENT_STATUS;
  protected readonly page = signal<PageResponse<Student> | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly createError = signal<string | null>(null);
  protected search = '';
  protected newStudent = { matricule: '', lastName: '', firstName: '', email: '', birthDate: '' };

  constructor() {
    this.load(0);
  }

  protected load(page: number): void {
    this.api.get<PageResponse<Student>>('/scolarite/students', { search: this.search, page, size: 20 }).subscribe({
      next: (result) => {
        this.page.set(result);
        this.error.set(null);
      },
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected create(): void {
    const s = this.newStudent;
    const body = {
      matricule: s.matricule.trim(),
      person: { firstName: s.firstName.trim(), lastName: s.lastName.trim(), email: s.email || null, birthDate: s.birthDate || null },
    };
    this.api.post<Student>('/scolarite/students', body).subscribe({
      next: (student) => void this.router.navigate(['/apprenants', student.id]),
      error: (e) => this.createError.set(errorMessage(e)),
    });
  }
}
