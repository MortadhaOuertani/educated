import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { EDUCATED_CONFIG } from './config';
import { EstablishmentStore } from './establishment-store';

export const ESTABLISHMENT_HEADER = 'X-Establishment-Id';

/** Adds the chosen establishment to every backend call. */
export const establishmentInterceptor: HttpInterceptorFn = (request, next) => {
  const config = inject(EDUCATED_CONFIG);
  const establishmentId = inject(EstablishmentStore).selected();
  if (!establishmentId || !request.url.startsWith(config.apiUrl)) {
    return next(request);
  }
  return next(request.clone({ setHeaders: { [ESTABLISHMENT_HEADER]: establishmentId } }));
};
