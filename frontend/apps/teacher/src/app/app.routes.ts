import { Route } from '@angular/router';
import { NotificationsPage } from '@edu/ui';

export const appRoutes: Route[] = [
  { path: '', loadComponent: () => import('./pages/home.page').then((m) => m.HomePage), title: 'Accueil' },
  { path: 'seances', loadComponent: () => import('./pages/sessions.page').then((m) => m.SessionsPage), title: 'Mes séances' },
  {
    path: 'seances/:id/appel',
    loadComponent: () => import('./pages/attendance.page').then((m) => m.AttendancePage),
    title: 'Appel',
  },
  { path: 'classes', loadComponent: () => import('./pages/classes.page').then((m) => m.ClassesPage), title: 'Mes classes' },
  { path: 'notes', loadComponent: () => import('./pages/grades.page').then((m) => m.GradesPage), title: 'Notes' },
  {
    path: 'stages',
    loadComponent: () => import('./pages/internships.page').then((m) => m.InternshipsPage),
    title: 'Stages encadrés',
  },
  { path: 'notifications', component: NotificationsPage, title: 'Notifications' },
  { path: '**', redirectTo: '' },
];
