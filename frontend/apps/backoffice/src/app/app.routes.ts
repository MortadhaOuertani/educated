import { Route } from '@angular/router';
import { requirePermission } from './access';

export const appRoutes: Route[] = [
  {
    path: '',
    loadComponent: () => import('./pages/dashboard.page').then((m) => m.DashboardPage),
    title: 'Tableau de bord',
  },
  {
    path: 'apprenants',
    canActivate: [requirePermission('Student', 'READ')],
    loadComponent: () => import('./pages/students.page').then((m) => m.StudentsPage),
    title: 'Apprenants',
  },
  {
    path: 'apprenants/:id',
    canActivate: [requirePermission('Student', 'READ')],
    loadComponent: () => import('./pages/student-detail.page').then((m) => m.StudentDetailPage),
    title: 'Fiche apprenant',
  },
  {
    path: 'demandes',
    canActivate: [requirePermission('DocumentRequest', 'READ')],
    loadComponent: () => import('./pages/document-requests.page').then((m) => m.DocumentRequestsPage),
    title: 'Demandes de documents',
  },
  {
    path: 'roles',
    canActivate: [requirePermission('Role', 'READ')],
    loadComponent: () => import('./pages/roles.page').then((m) => m.RolesPage),
    title: 'Rôles',
  },
  {
    path: 'utilisateurs',
    canActivate: [requirePermission('UserAccount', 'READ')],
    loadComponent: () => import('./pages/users.page').then((m) => m.UsersPage),
    title: 'Utilisateurs',
  },
  { path: '**', redirectTo: '' },
];
