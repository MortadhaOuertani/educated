import { Component, computed, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SessionService } from '@edu/core';
import { NavItem, ShellComponent } from '@edu/ui';

@Component({
  selector: 'edu-root',
  imports: [ShellComponent, RouterOutlet],
  template: `
    <edu-shell title="Espace étudiant" [navItems]="navItems">
      @if (isStudent()) {
        <router-outlet />
      } @else {
        <div class="card">
          <h1>Espace réservé aux étudiants</h1>
          <p class="edu-muted">Votre compte n'est pas lié à un dossier étudiant. Adressez-vous à la scolarité.</p>
        </div>
      }
    </edu-shell>
  `,
})
export class App {
  private readonly session = inject(SessionService);

  protected readonly navItems: NavItem[] = [
    { label: 'Accueil', link: '/' },
    { label: 'Emploi du temps', link: '/emploi-du-temps' },
    { label: 'Mes cours', link: '/cours' },
    { label: 'Mes absences', link: '/absences' },
    { label: 'Examens et notes', link: '/notes' },
    { label: 'Mon stage', link: '/stage' },
    { label: 'Demandes de documents', link: '/demandes' },
    { label: 'Paiements', link: '/paiements' },
    { label: 'Notifications', link: '/notifications' },
    { label: 'Mon profil', link: '/profil' },
  ];

  protected readonly isStudent = computed(() => this.session.me()?.personType === 'STUDENT');
}
