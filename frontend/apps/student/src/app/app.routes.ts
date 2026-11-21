import { Route } from '@angular/router';
import { NotificationsPage } from '@edu/ui';

export const appRoutes: Route[] = [
  { path: '', loadComponent: () => import('./pages/home.page').then((m) => m.HomePage), title: 'Accueil' },
  {
    path: 'emploi-du-temps',
    loadComponent: () => import('./pages/timetable.page').then((m) => m.TimetablePage),
    title: 'Emploi du temps',
  },
  { path: 'cours', loadComponent: () => import('./pages/courses.page').then((m) => m.CoursesPage), title: 'Mes cours' },
  {
    path: 'absences',
    loadComponent: () => import('./pages/absences.page').then((m) => m.AbsencesPage),
    title: 'Mes absences',
  },
  {
    path: 'notes',
    loadComponent: () => import('./pages/grades.page').then((m) => m.GradesPage),
    title: 'Examens et notes',
  },
  { path: 'stage', loadComponent: () => import('./pages/internship.page').then((m) => m.InternshipPage), title: 'Mon stage' },
  {
    path: 'demandes',
    loadComponent: () => import('./pages/document-requests.page').then((m) => m.DocumentRequestsPage),
    title: 'Demandes de documents',
  },
  {
    path: 'paiements',
    loadComponent: () => import('./pages/payments.page').then((m) => m.PaymentsPage),
    title: 'Paiements',
  },
  { path: 'notifications', component: NotificationsPage, title: 'Notifications' },
  { path: 'profil', loadComponent: () => import('./pages/profile.page').then((m) => m.ProfilePage), title: 'Mon profil' },
  { path: '**', redirectTo: '' },
];
