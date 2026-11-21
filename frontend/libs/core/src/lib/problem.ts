import { HttpErrorResponse } from '@angular/common/http';

/** A readable message for an API error (the backend answers with RFC 9457 problem details). */
export function errorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return 'Le serveur est injoignable. Vérifiez que le backend est démarré.';
    }
    if (error.status === 403) {
      return "Vous n'avez pas la permission pour cette action.";
    }
    const detail = error.error?.detail;
    if (typeof detail === 'string' && detail.length > 0) {
      return detail;
    }
    return `Erreur ${error.status}.`;
  }
  return 'Une erreur inattendue est survenue.';
}
