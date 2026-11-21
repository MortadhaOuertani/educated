import { Component, computed, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SessionService } from '@edu/core';
import { NavItem, ShellComponent } from '@edu/ui';

@Component({
  selector: 'edu-root',
  imports: [ShellComponent, RouterOutlet],
  template: `
    <edu-shell title="Espace enseignant" [navItems]="navItems">
      @if (isTeacher()) {
        <router-outlet />
      } @else {
        <div class="card">
          <h1>Espace réservé aux enseignants</h1>
          <p class="edu-muted">Votre compte n'est pas un compte enseignant. Adressez-vous à la scolarité.</p>
        </div>
      }
    </edu-shell>
  `,
})
export class App {
  private readonly session = inject(SessionService);
  protected readonly navItems: NavItem[] = [
    { label: 'Accueil', link: '/' },
    { label: 'Mes séances', link: '/seances' },
    { label: 'Mes classes', link: '/classes' },
    { label: 'Notes et surveillances', link: '/notes' },
    { label: 'Stages encadrés', link: '/stages' },
    { label: 'Notifications', link: '/notifications' },
  ];
  protected readonly isTeacher = computed(() => this.session.me()?.personType === 'TEACHER');
}
