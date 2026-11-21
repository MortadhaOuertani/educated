import { provideHttpClient, withInterceptors, withInterceptorsFromDi } from '@angular/common/http';
import { EnvironmentProviders, inject, makeEnvironmentProviders, provideAppInitializer } from '@angular/core';
import { provideOAuthClient } from 'angular-oauth2-oidc';
import { EDUCATED_CONFIG, EducatedConfig } from './config';
import { establishmentInterceptor } from './establishment.interceptor';
import { SessionService } from './session.service';

/**
 * Everything an Educated portal needs: HTTP with the Keycloak token and the establishment header,
 * and sign-in before the first screen is shown.
 */
export function provideEducated(config: EducatedConfig): EnvironmentProviders {
  return makeEnvironmentProviders([
    { provide: EDUCATED_CONFIG, useValue: config },
    provideHttpClient(withInterceptorsFromDi(), withInterceptors([establishmentInterceptor])),
    provideOAuthClient({ resourceServer: { allowedUrls: [config.apiUrl], sendAccessToken: true } }),
    provideAppInitializer(() => inject(SessionService).start()),
  ]);
}
