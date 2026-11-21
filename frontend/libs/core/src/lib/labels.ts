import { DocumentRequestStatus, StudentStatus } from './models';

/** French labels and badge colours shown in the portals. */

export const DOCUMENT_STATUS: Record<DocumentRequestStatus, { label: string; badge: string }> = {
  SUBMITTED: { label: 'Déposée', badge: 'badge-info' },
  IN_PROGRESS: { label: 'En traitement', badge: 'badge-warning' },
  DONE: { label: 'Traitée', badge: 'badge-success' },
  REJECTED: { label: 'Refusée', badge: 'badge-danger' },
};

export const STUDENT_STATUS: Record<StudentStatus, string> = {
  ACTIVE: 'Actif',
  SUSPENDED: 'Suspendu',
  EXCLUDED: 'Exclu',
  GRADUATED: 'Diplômé',
  WITHDRAWN: 'Abandon',
};

export const DOCUMENT_LANGUAGE: Record<'FR' | 'EN' | 'AR', string> = {
  FR: 'français',
  EN: 'anglais',
  AR: 'arabe',
};
