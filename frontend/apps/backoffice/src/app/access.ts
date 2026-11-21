import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Operation, SessionService } from '@edu/core';
import { NavItem } from '@edu/ui';

type Can = (entityType: string, operation: Operation) => boolean;

/** The side menu: only the screens the user's roles allow. */
export function buildNav(can: Can): NavItem[] {
  const items: NavItem[] = [{ label: 'Tableau de bord', link: '/' }];
  if (can('Student', 'READ')) {
    items.push({ label: 'Apprenants', link: '/apprenants' });
  }
  if (can('DocumentRequest', 'READ')) {
    items.push({ label: 'Demandes de documents', link: '/demandes' });
  }
  if (can('Role', 'READ')) {
    items.push({ label: 'Rôles', link: '/roles' });
  }
  if (can('UserAccount', 'READ')) {
    items.push({ label: 'Utilisateurs', link: '/utilisateurs' });
  }
  return items;
}

/** Route guard: sends the user back to the dashboard when their roles do not allow the screen. */
export function requirePermission(entityType: string, operation: Operation): CanActivateFn {
  return () => inject(SessionService).can(entityType, operation) || inject(Router).parseUrl('/');
}
