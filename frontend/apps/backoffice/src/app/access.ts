import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Operation, SessionService } from '@edu/core';
import { NavItem } from '@edu/ui';

type Can = (entityType: string, operation: Operation) => boolean;

/** Each screen of the menu and the entity the role must be allowed to read to see it. */
const SCREENS: { label: string; link: string; entityType: string }[] = [
  { label: 'Apprenants', link: '/apprenants', entityType: 'Student' },
  { label: 'Demandes de documents', link: '/demandes', entityType: 'DocumentRequest' },
  { label: 'Assiduité', link: '/assiduite', entityType: 'StudentAbsenceRequest' },
  { label: 'Pédagogie', link: '/pedagogie', entityType: 'Teacher' },
  { label: 'Emplois du temps', link: '/emplois-du-temps', entityType: 'Timetable' },
  { label: 'Examens et notes', link: '/examens', entityType: 'GradeGrid' },
  { label: 'Stages', link: '/stages', entityType: 'Internship' },
  { label: 'Finances', link: '/finances', entityType: 'Installment' },
  { label: 'Communication', link: '/communication', entityType: 'Message' },
  { label: "Vie de l'établissement", link: '/vie-etablissement', entityType: 'CanteenMenu' },
  { label: 'Rôles', link: '/roles', entityType: 'Role' },
  { label: 'Utilisateurs', link: '/utilisateurs', entityType: 'UserAccount' },
];

/** The side menu: only the screens the user's roles allow, plus the notifications everyone has. */
export function buildNav(can: Can): NavItem[] {
  const items: NavItem[] = [{ label: 'Tableau de bord', link: '/' }];
  for (const screen of SCREENS) {
    if (can(screen.entityType, 'READ')) {
      items.push({ label: screen.label, link: screen.link });
    }
  }
  items.push({ label: 'Notifications', link: '/notifications' });
  return items;
}

/** Route guard: sends the user back to the dashboard when their roles do not allow the screen. */
export function requirePermission(entityType: string, operation: Operation): CanActivateFn {
  return () => inject(SessionService).can(entityType, operation) || inject(Router).parseUrl('/');
}
