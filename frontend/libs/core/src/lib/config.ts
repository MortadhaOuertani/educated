import { InjectionToken } from '@angular/core';

export type Portal = 'STUDENT' | 'TEACHER' | 'BACK_OFFICE';

/** Where the app finds Keycloak and the backend. */
export interface EducatedConfig {
  /** Keycloak realm URL, e.g. http://localhost:8081/realms/educated */
  issuer: string;
  /** Public Keycloak client used by the web portals. */
  clientId: string;
  /** Base path of the backend API (proxied to the backend in development). */
  apiUrl: string;
  portal: Portal;
}

export const EDUCATED_CONFIG = new InjectionToken<EducatedConfig>('EDUCATED_CONFIG');
