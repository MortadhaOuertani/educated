/** Types returned by the backend API. They mirror the backend DTOs. */

export type AppModule =
  | 'SCOLARITE'
  | 'PEDAGOGIE'
  | 'EMPLOIS_DU_TEMPS'
  | 'EXAMENS_NOTES'
  | 'STAGES'
  | 'FINANCES'
  | 'VIE_ETABLISSEMENT'
  | 'COMMUNICATION'
  | 'PARAMETRAGE'
  | 'ADMINISTRATION';

export type Operation = 'READ' | 'WRITE' | 'CREATE' | 'DELETE' | 'NAVIGATE';
export type PersonType = 'STUDENT' | 'TEACHER' | 'GUARDIAN' | 'STAFF';
export type ThemeMode = 'SYSTEM' | 'LIGHT' | 'DARK';
export type DisplayDensity = 'COMPACT' | 'NORMAL' | 'LARGE';

export interface Me {
  userId: string;
  username: string;
  email: string | null;
  firstName: string | null;
  lastName: string | null;
  personType: PersonType | null;
  personId: string | null;
  administrative: boolean;
  theme: ThemeMode;
  displayDensity: DisplayDensity;
  preferredLocale: string;
  establishmentId: string | null;
  visibleModules: AppModule[];
  permissions: Record<string, Operation[]>;
  deniedActions: string[];
}

export interface LocalizedText {
  fr: string | null;
  en: string | null;
  ar: string | null;
}

export interface Establishment {
  id: string;
  code: string;
  name: LocalizedText;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface PersonInfo {
  firstName: string | null;
  lastName: string | null;
  gender?: 'MALE' | 'FEMALE' | null;
  birthDate?: string | null;
  email?: string | null;
  phone?: string | null;
  mobile?: string | null;
}

export type StudentStatus = 'ACTIVE' | 'SUSPENDED' | 'EXCLUDED' | 'GRADUATED' | 'WITHDRAWN';

export interface Student {
  id: string;
  matricule: string;
  status: StudentStatus;
  person: PersonInfo;
  admissionDate: string | null;
}

export interface SchoolClassInfo {
  id: string;
  code: string;
  name: string | null;
  studyLevelId: string;
  academicYearId: string;
}

export interface StudentProfile {
  student: Student;
  currentClass: SchoolClassInfo | null;
}

export interface GuardianLink {
  guardianId: string;
  person: PersonInfo;
  profession: string | null;
  kinshipCode: string;
  kinship: LocalizedText;
  primaryContact: boolean;
  financialResponsible: boolean;
}

export interface Enrollment {
  id: string;
  academicYearId: string;
  studyLevelId: string;
  enrollmentDate: string;
  enrollmentType: 'NEW' | 'RE_ENROLLMENT' | 'TRANSFER';
  status: 'PENDING' | 'ENROLLED' | 'CANCELLED' | 'TRANSFERRED_OUT';
  repeating: boolean;
  assignments: { schoolClassId: string; startDate: string; endDate: string | null }[];
}

export interface AcademicYear {
  id: string;
  code: string;
  startDate: string;
  endDate: string;
  status: 'PLANNED' | 'OPEN' | 'CLOSED';
  current: boolean;
}

export interface SchoolClass {
  id: string;
  code: string;
  name: string | null;
  capacity: number | null;
  studyLevelId: string;
  academicYearId: string;
}

export type DocumentRequestStatus = 'SUBMITTED' | 'IN_PROGRESS' | 'DONE' | 'REJECTED';

export interface DocumentType {
  id: string;
  code: string;
  label: LocalizedText;
  language: 'FR' | 'EN' | 'AR';
}

export interface DocumentRequest {
  id: string;
  studentId: string;
  studentName: string | null;
  matricule: string;
  documentType: DocumentType;
  reason: string | null;
  hasProof: boolean;
  status: DocumentRequestStatus;
  requestedAt: string;
  processedAt: string | null;
  rejectionReason: string | null;
}

export interface FeatureToggle {
  featureKey: string;
  portal: string;
  enabled: boolean;
  disabledMessage: string | null;
}

export type PermissionPolicy = 'DENY_ALL_BY_DEFAULT' | 'READ_ONLY_ALL_BY_DEFAULT' | 'ALLOW_ALL_BY_DEFAULT';

export interface RoleSummary {
  id: string;
  name: string;
  description: string | null;
  administrative: boolean;
  canEditModel: boolean;
  permissionPolicy: PermissionPolicy;
  preset: string;
  systemRole: boolean;
}

export interface TypeRule {
  targetType: string;
  read: string;
  write: string;
  create: string;
  delete: string;
  navigate: string;
}

export interface RoleDetail {
  role: RoleSummary;
  visibleModules: AppModule[];
  typePermissions: TypeRule[];
  navigationPermissions: { itemPath: string; navigate: string }[];
  deniedActions: string[];
}

export interface UserAccount {
  id: string;
  username: string;
  email: string | null;
  active: boolean;
  personType: PersonType | null;
  lastLoginAt: string | null;
}
