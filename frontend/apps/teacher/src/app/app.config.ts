import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideEducated } from '@edu/core';
import { appRoutes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(appRoutes),
    provideEducated({
      issuer: 'http://localhost:8081/realms/educated',
      clientId: 'educated-web',
      apiUrl: '/api',
      portal: 'TEACHER',
    }),
  ],
};
