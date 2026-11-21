import { HttpErrorResponse } from '@angular/common/http';
import { errorMessage } from './problem';

describe('errorMessage', () => {
  it('uses the backend problem detail', () => {
    const error = new HttpErrorResponse({ status: 422, error: { code: 'student.matricule', detail: 'Matricule déjà utilisé.' } });
    expect(errorMessage(error)).toBe('Matricule déjà utilisé.');
  });

  it('explains missing permissions and unreachable servers', () => {
    expect(errorMessage(new HttpErrorResponse({ status: 403 }))).toContain('permission');
    expect(errorMessage(new HttpErrorResponse({ status: 0 }))).toContain('injoignable');
  });

  it('falls back for unknown errors', () => {
    expect(errorMessage(new Error('x'))).toBe('Une erreur inattendue est survenue.');
  });
});
