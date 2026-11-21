import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ApiService, STUDENT_STATUS, StudentProfile, errorMessage } from '@edu/core';
import { ThemeService } from '@edu/ui';

@Component({
  selector: 'edu-profile-page',
  imports: [DatePipe],
  template: `
    <h1>Mon profil</h1>
    @if (error()) {
      <p class="edu-error">{{ error() }}</p>
    }
    @if (profile(); as p) {
      <div class="card">
        <h2>Carte étudiant</h2>
        <p><strong>{{ p.student.person.lastName?.toUpperCase() }} {{ p.student.person.firstName }}</strong></p>
        <p>Matricule : {{ p.student.matricule }}</p>
        <p>Classe : {{ p.currentClass?.code ?? 'non affecté' }}</p>
        <p>Statut : {{ statuses[p.student.status] }}</p>
        @if (p.student.admissionDate) {
          <p>Admis le {{ p.student.admissionDate | date: 'dd/MM/yyyy' }}</p>
        }
        <p class="edu-muted">Vos informations sont tenues par la scolarité. Signalez-lui toute erreur.</p>
      </div>
    }
    <div class="card">
      <h2>Apparence</h2>
      <div class="actions">
        <button type="button" class="btn" [class.btn-secondary]="theme.isDark()" (click)="theme.set('light')">Clair</button>
        <button type="button" class="btn" [class.btn-secondary]="!theme.isDark()" (click)="theme.set('dark')">Sombre</button>
      </div>
      <p class="edu-muted">Le mot de passe se change sur la page de connexion (Keycloak).</p>
    </div>
  `,
})
export class ProfilePage {
  private readonly api = inject(ApiService);
  protected readonly theme = inject(ThemeService);
  protected readonly statuses = STUDENT_STATUS;
  protected readonly profile = signal<StudentProfile | null>(null);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.api.get<StudentProfile>('/student/profile').subscribe({
      next: (profile) => this.profile.set(profile),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
