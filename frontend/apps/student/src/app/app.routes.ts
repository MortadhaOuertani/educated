import { Route } from '@angular/router';

export const appRoutes: Route[] = [
  { path: '', loadComponent: () => import('./pages/home.page').then((m) => m.HomePage), title: 'Accueil' },
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
  { path: 'profil', loadComponent: () => import('./pages/profile.page').then((m) => m.ProfilePage), title: 'Mon profil' },
  { path: '**', redirectTo: '' },
];
