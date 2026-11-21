import { Component, inject, input } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { SessionService } from '@edu/core';
import { ThemeService } from './theme.service';

export interface NavItem {
  label: string;
  link: string;
}

/** Page frame shared by the three portals: header, side menu and the page content. */
@Component({
  selector: 'edu-shell',
  imports: [RouterLink, RouterLinkActive],
  template: `
    @switch (session.status()) {
      @case ('loading') {
        <div class="edu-center" role="status">Connexion…</div>
      }
      @case ('error') {
        <div class="edu-center">
          <h1>Connexion impossible</h1>
          <p class="edu-error">{{ session.error() }}</p>
          <button type="button" class="btn" (click)="reload()">Réessayer</button>
        </div>
      }
      @default {
        <header class="edu-header">
          <span class="edu-brand">Educated</span>
          <span class="edu-portal">{{ title() }}</span>
          <span class="edu-spacer"></span>
          @if (session.establishments().length > 1) {
            <select
              aria-label="Établissement"
              [value]="session.selectedEstablishmentId()"
              (change)="switchEstablishment($event)"
            >
              @for (establishment of session.establishments(); track establishment.id) {
                <option [value]="establishment.id">{{ establishment.name.fr ?? establishment.code }}</option>
              }
            </select>
          } @else if (session.establishments().length === 1) {
            <span class="edu-muted">{{ session.establishments()[0].name.fr ?? session.establishments()[0].code }}</span>
          }
          <button
            type="button"
            class="btn-icon"
            (click)="theme.toggle()"
            [attr.aria-label]="theme.isDark() ? 'Passer en mode clair' : 'Passer en mode sombre'"
          >
            {{ theme.isDark() ? '☀' : '☾' }}
          </button>
          <span class="edu-user">{{ session.displayName() }}</span>
          <button type="button" class="btn btn-secondary" (click)="session.logout()">Se déconnecter</button>
        </header>
        <div class="edu-layout">
          <nav class="edu-nav" aria-label="Navigation principale">
            @for (item of navItems(); track item.link) {
              <a [routerLink]="item.link" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: item.link === '/' }">
                {{ item.label }}
              </a>
            }
          </nav>
          <main class="edu-main">
            <ng-content />
          </main>
        </div>
      }
    }
  `,
})
export class ShellComponent {
  readonly title = input.required<string>();
  readonly navItems = input<NavItem[]>([]);

  protected readonly session = inject(SessionService);
  protected readonly theme = inject(ThemeService);

  protected switchEstablishment(event: Event): void {
    void this.session.switchEstablishment((event.target as HTMLSelectElement).value);
  }

  protected reload(): void {
    window.location.reload();
  }
}
