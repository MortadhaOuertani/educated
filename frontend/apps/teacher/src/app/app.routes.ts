import { Route } from '@angular/router';

export const appRoutes: Route[] = [
  { path: '', loadComponent: () => import('./pages/home.page').then((m) => m.HomePage), title: 'Accueil' },
  { path: '**', redirectTo: '' },
];
