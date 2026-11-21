import { Component, computed, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SessionService } from '@edu/core';
import { ShellComponent } from '@edu/ui';
import { buildNav } from './access';

@Component({
  selector: 'edu-root',
  imports: [ShellComponent, RouterOutlet],
  template: `
    <edu-shell title="Espace de gestion" [navItems]="navItems()">
      @if (hasAccess()) {
        <router-outlet />
      } @else {
        <div class="card">
          <h1>Aucun accès à l'espace de gestion</h1>
          <p class="edu-muted">Aucun rôle ne vous donne accès à cet espace. Contactez un administrateur.</p>
        </div>
      }
    </edu-shell>
  `,
})
export class App {
  private readonly session = inject(SessionService);

  protected readonly navItems = computed(() => buildNav((type, operation) => this.session.can(type, operation)));

  protected readonly hasAccess = computed(() => {
    const me = this.session.me();
    return !!me && (me.administrative || Object.keys(me.permissions).length > 0);
  });
}
