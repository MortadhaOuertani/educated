import { Route } from '@angular/router';
import { NotificationsPage } from '@edu/ui';
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
  {
    path: 'assiduite',
    canActivate: [requirePermission('StudentAbsenceRequest', 'READ')],
    loadComponent: () => import('./pages/attendance.page').then((m) => m.AttendancePage),
    title: 'Assiduité',
  },
  {
    path: 'pedagogie',
    canActivate: [requirePermission('Teacher', 'READ')],
    loadComponent: () => import('./pages/teaching.page').then((m) => m.TeachingPage),
    title: 'Pédagogie',
  },
  {
    path: 'emplois-du-temps',
    canActivate: [requirePermission('Timetable', 'READ')],
    loadComponent: () => import('./pages/timetables.page').then((m) => m.TimetablesPage),
    title: 'Emplois du temps',
  },
  {
    path: 'examens',
    canActivate: [requirePermission('GradeGrid', 'READ')],
    loadComponent: () => import('./pages/exams.page').then((m) => m.ExamsPage),
    title: 'Examens et notes',
  },
  {
    path: 'stages',
    canActivate: [requirePermission('Internship', 'READ')],
    loadComponent: () => import('./pages/internships.page').then((m) => m.InternshipsPage),
    title: 'Stages',
  },
  {
    path: 'finances',
    canActivate: [requirePermission('Installment', 'READ')],
    loadComponent: () => import('./pages/finances.page').then((m) => m.FinancesPage),
    title: 'Finances',
  },
  {
    path: 'communication',
    canActivate: [requirePermission('Message', 'READ')],
    loadComponent: () => import('./pages/communication.page').then((m) => m.CommunicationPage),
    title: 'Communication',
  },
  {
    path: 'vie-etablissement',
    canActivate: [requirePermission('CanteenMenu', 'READ')],
    loadComponent: () => import('./pages/campus.page').then((m) => m.CampusPage),
    title: "Vie de l'établissement",
  },
  { path: 'notifications', component: NotificationsPage, title: 'Notifications' },
  { path: '**', redirectTo: '' },
];
