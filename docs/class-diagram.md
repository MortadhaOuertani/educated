# Tekup · Educated — Class Diagram (domain model)

**Basis:** every screen, field, status and action found in the role survey (`Tekup_Educated_Roles_and_Functionalities.md`).
**Target architecture:** §9 of that file: a **Spring Boot + Spring Modulith modular monolith**, **Angular** front-ends, **Keycloak** for login and 2FA, **PostgreSQL with one schema per module**.

The model is split **by module**, exactly as in the architecture: `shared`, `parametrage`, `administration`, `scolarite`, `pedagogie`, `emploidutemps`, `assiduite`, `examens`, `stages`, `finances`, `communication`, `vieetablissement`.

---

## 0. How to read the diagrams

| Notation | Meaning |
|---|---|
| `*--` | Composition: the part lives and dies with its owner<br>(same module) |
| `o--` | Aggregation: grouped, but each part has its own life<br>(same module) |
| `-->` | Association inside the same module (JPA reference) |
| `..>` (dashed) | Reference **to another module, by ID only**<br>(a `UUID …Id` attribute, no join, no foreign key) |
| `<<module>>` on a box | Class owned by that other module, shown without<br>attributes (its full definition is in its own section) |
| `<<embeddable>>` | Value object stored inside its owner's table |
| `<<readModel>>` | Computed view for dashboards, never edited directly |

**Rules applied everywhere:**
- Every class extends **`TenantEntity`** (§2) unless stated otherwise. It inherits `id`, `establishmentId`, `createdAt`, `createdByUserId`, `updatedAt`, `updatedByUserId`, `version` and `archivedAt`. These are **not repeated** in the diagrams, so a "created/requested/uploaded by/at" value always comes from them.
- Inside a module, links are drawn as lines and their foreign-key columns aren't repeated as attributes.
- Between modules, the link is a `UUID xxxId` attribute **plus** a dashed arrow. The owning module's API checks that the ID exists, and domain events keep data in sync (§15). This keeps each module's schema independent, as the architecture requires.
- **Deletion is soft** (`archivedAt`) for business data. This fits the role rules, where delete is often denied.
- **No password or 2FA secret is stored in the application.** Keycloak holds them. `UserAccount` only stores the Keycloak ID and the flags shown on screen.
- Class and attribute names are in English (Java/JPA naming). §17 maps every French screen to its classes.

---

## 1. Modules and their dependencies

An arrow means "depends on" (calls its API or listens to its events). The graph has **no cycle**, which Spring Modulith enforces. Every module also depends on `shared`.

```mermaid
flowchart BT
  parametrage --> shared
  administration --> shared
  scolarite --> parametrage
  pedagogie --> scolarite
  pedagogie --> parametrage
  emploidutemps --> pedagogie
  emploidutemps --> parametrage
  assiduite --> emploidutemps
  assiduite --> pedagogie
  assiduite --> scolarite
  stages --> pedagogie
  stages --> scolarite
  examens --> stages
  examens --> pedagogie
  examens --> scolarite
  finances --> assiduite
  finances --> pedagogie
  finances --> scolarite
  vieetablissement --> assiduite
  vieetablissement --> pedagogie
  vieetablissement --> scolarite
  communication --> administration
  communication --> scolarite
  communication --> pedagogie
  communication --> assiduite
  communication --> examens
  communication --> finances
  communication --> stages
  communication --> emploidutemps
  communication --> vieetablissement
```

**Two references go "against" this direction on purpose.** They're stored as **opaque IDs**: no class or API of the other module is imported, so no cycle appears.
- `pedagogie` → `emploidutemps`: the class log (`CourseJournalEntry`), homework and make-up requests keep a `sessionOccurrenceId`. The Angular app picks the session through the `emploidutemps` API and passes its ID.
- `administration` → person modules: `UserAccount.personId` points to a `Student`, `Teacher`, `Guardian` or `StaffMember`, depending on `personType`.

---

## 2. `shared` — base classes and value objects

```mermaid
classDiagram
  class BaseEntity {
    <<abstract>>
    UUID id
    Instant createdAt
    UUID createdByUserId
    Instant updatedAt
    UUID updatedByUserId
    Long version
  }
  class TenantEntity {
    <<abstract>>
    UUID establishmentId
    Instant archivedAt
  }
  class LocalizedText {
    <<embeddable>>
    String fr
    String en
    String ar
  }
  class Address {
    <<embeddable>>
    String line1
    String line2
    String city
    String postalCode
    String governorate
    String country
  }
  class Money {
    <<embeddable>>
    BigDecimal amount
    String currency
  }
  class PersonInfo {
    <<embeddable>>
    String firstName
    String lastName
    String firstNameAr
    String lastNameAr
    Gender gender
    LocalDate birthDate
    String birthPlace
    String nationality
    String nationalIdNumber
    String passportNumber
    String email
    String phone
    String mobile
    Address address
    UUID photoFileId
  }
  class StoredFile {
    String originalName
    String contentType
    Long sizeBytes
    String storageKey
    String checksumSha256
    ScanStatus scanStatus
  }
  class AuditLog {
    UUID id
    UUID establishmentId
    String moduleName
    String action
    String entityType
    UUID entityId
    String changesJson
    UUID actorUserId
    String ipAddress
    String userAgent
    Instant occurredAt
  }
  BaseEntity <|-- TenantEntity
  TenantEntity <|-- StoredFile
  PersonInfo *-- Address
```

- `PersonInfo` is **embedded** in `Student`, `Guardian`, `Teacher` and `StaffMember`. A shared `Person` superclass would force one table across several module schemas, which the architecture forbids.
- `StoredFile` holds the metadata of a file kept in **SeaweedFS** (S3 API). Every `…FileId` attribute in the model points to a `StoredFile`.
- `AuditLog` is **append-only**, so it doesn't extend `BaseEntity` (no version, never updated). Hibernate Envers keeps the full record history next to it.

---

## 3. `parametrage` — establishment and reference data

```mermaid
classDiagram
  class Establishment {
    String code
    LocalizedText name
    String legalName
    String taxNumber
    Address address
    String email
    String phone
    String website
    UUID logoFileId
    String timezone
    String defaultLocale
    boolean active
  }
  class Site {
    String code
    String name
    Address address
  }
  class Building {
    String code
    String name
  }
  class Room {
    String code
    String name
    Integer capacity
    RoomType roomType
    Integer floor
    String equipment
    boolean active
  }
  class AcademicYear {
    String code
    LocalDate startDate
    LocalDate endDate
    AcademicYearStatus status
    boolean current
  }
  class AcademicPeriod {
    String code
    LocalizedText name
    PeriodType periodType
    Integer orderIndex
    LocalDate startDate
    LocalDate endDate
    boolean current
  }
  class CalendarEvent {
    LocalizedText title
    CalendarEventType eventType
    LocalDate startDate
    LocalDate endDate
    boolean blocksClasses
  }
  class Department {
    String code
    LocalizedText name
  }
  class Cycle {
    String code
    LocalizedText name
    Integer durationYears
  }
  class Program {
    String code
    LocalizedText name
    String diplomaTitle
    boolean active
  }
  class Specialization {
    String code
    LocalizedText name
  }
  class StudyLevel {
    String code
    LocalizedText name
    Integer yearOfStudy
    StudyRhythm rhythm
    boolean active
  }
  class SchoolClass {
    String code
    String name
    Integer capacity
    boolean active
  }
  class TeachingDomain {
    String code
    LocalizedText name
  }
  class FeatureToggle {
    String featureKey
    Portal portal
    boolean enabled
    String disabledMessage
  }
  Establishment "1" *-- "1..*" Site
  Site "1" *-- "0..*" Building
  Building "1" *-- "0..*" Room
  AcademicYear "1" *-- "1..*" AcademicPeriod
  AcademicYear "1" *-- "0..*" CalendarEvent
  CalendarEvent "0..*" --> "0..*" StudyLevel : appliesTo
  Department "1" --> "0..*" Program
  Program "1" *-- "0..*" Specialization
  Program "1" --> "1..*" StudyLevel
  StudyLevel "0..*" --> "1" Cycle
  StudyLevel "0..*" --> "0..1" Specialization
  StudyLevel "1" --> "0..*" SchoolClass
  SchoolClass "0..*" --> "1" AcademicYear
  SchoolClass "0..*" --> "1" Site
```

- `Establishment` extends **`BaseEntity`** (not `TenantEntity`): it *is* the tenant.
- Example of how the reference data fits together, taken from the portal: program "Informatique", cycle "Cycle d'Ingénieur", study level `ING-5-S-GLSI` (year 5, rhythm S, specialization GLSI), class `ING-5-S-GLSI-B`.
- `StudyRhythm` stores the J / S / P letter seen in class codes. Its exact meaning (day / evening / other) is to be confirmed with the school.
- `FeatureToggle` covers sections that are switched off, such as the student "Paiements" ("Cette rubrique est actuellement désactivée").
- The architecture puts the academic calendar in `parametrage`. Its screen stays in the "Emplois du temps" menu, because the Angular app can show any module's data on any screen.

---

## 4. `administration` — identity, roles and permissions

```mermaid
classDiagram
  class UserAccount {
    UUID keycloakId
    String username
    String email
    String phone
    PersonType personType
    UUID personId
    boolean active
    boolean forcePasswordChange
    boolean twoFactorRequired
    Instant lastLoginAt
    String preferredLocale
    ThemeMode theme
    DisplayDensity displayDensity
  }
  class UserRole {
    LocalDate validFrom
    LocalDate validTo
  }
  class Role {
    String name
    String description
    boolean administrative
    boolean canEditModel
    PermissionPolicy permissionPolicy
    RolePreset preset
    boolean systemRole
  }
  class RoleModuleAccess {
    AppModule module
    boolean visible
  }
  class TypePermission {
    String targetType
    PermissionState canRead
    PermissionState canWrite
    PermissionState canCreate
    PermissionState canDelete
    PermissionState canNavigate
  }
  class ObjectPermission {
    String criteria
    PermissionState canRead
    PermissionState canWrite
    PermissionState canDelete
    PermissionState canNavigate
  }
  class MemberPermission {
    String members
    String criteria
    PermissionState canRead
    PermissionState canWrite
  }
  class NavigationPermission {
    String itemPath
    PermissionState canNavigate
  }
  class DeniedAction {
    String actionKey
    String category
  }
  class UserFavorite {
    String navigationItem
    Integer orderIndex
  }
  class RoleChangeRequest {
    RoleChangeType changeType
    String payloadJson
    ApprovalStatus status
    UUID decidedByUserId
    Instant decidedAt
  }
  class StaffMember {
    PersonInfo person
    String employeeNumber
    String jobTitle
    String service
    boolean active
  }
  class Student {
    <<scolarite>>
  }
  class Guardian {
    <<scolarite>>
  }
  class Teacher {
    <<pedagogie>>
  }
  UserAccount "1" *-- "0..*" UserRole
  UserRole "0..*" --> "1" Role
  Role "1" *-- "0..*" RoleModuleAccess
  Role "1" *-- "0..*" TypePermission
  TypePermission "1" *-- "0..*" ObjectPermission
  TypePermission "1" *-- "0..*" MemberPermission
  Role "1" *-- "0..*" NavigationPermission
  Role "1" *-- "0..*" DeniedAction
  UserAccount "1" *-- "0..*" UserFavorite
  RoleChangeRequest "0..*" --> "1" Role
  UserAccount "0..1" --> "0..1" StaffMember : personId when STAFF
  UserAccount "0..1" ..> "0..1" Student : personId opaque
  UserAccount "0..1" ..> "0..1" Guardian : personId opaque
  UserAccount "0..1" ..> "0..1" Teacher : personId opaque
```

- These classes reproduce the **6-layer role editor** found in the survey:
  - identity: `Role.administrative`, `canEditModel`
  - module visibility: `RoleModuleAccess`
  - default policy: `permissionPolicy`
  - entity rights: `TypePermission`, plus row-level `ObjectPermission` and field-level `MemberPermission`
  - menu access: `NavigationPermission`
  - hidden buttons: `DeniedAction`
- `UserAccount` extends **`BaseEntity`** (not `TenantEntity`), because one account can work in several establishments. `UserRole` is per establishment (it inherits `establishmentId`), so the same person can hold different roles in different establishments.
- `UserAccount` reaches its person only through `personType` + `personId`. The line to `StaffMember` is solid only because it's in the same module. It uses the same `personId`, not a separate foreign key.
- **Keycloak** handles passwords, 2FA codes, lockouts and sign-in history. `twoFactorRequired` and `forcePasswordChange` are mirrored to Keycloak "required actions".
- `RoleChangeRequest` implements the "second Super Admin approval" for sensitive changes from the architecture (§9.6). An example is turning a role administrative.
- Only `administration` writes these tables. Other modules check permissions through an interface declared in `shared` and implemented here, which avoids a dependency loop.

---

## 5. `scolarite` — learners, guardians and student records

```mermaid
classDiagram
  class Student {
    PersonInfo person
    String matricule
    StudentStatus status
    LocalDate admissionDate
    String baccalaureateSection
    BigDecimal baccalaureateAverage
    Integer baccalaureateYear
    String previousInstitution
  }
  class Guardian {
    PersonInfo person
    String profession
    String employer
    String workPhone
    String preferredLanguage
    boolean smsOptIn
  }
  class StudentGuardian {
    boolean primaryContact
    boolean financialResponsible
    boolean livesWithStudent
    boolean notifyPush
    boolean notifySms
  }
  class Kinship {
    String code
    LocalizedText label
  }
  class SiblingRelation {
    String note
  }
  class PreRegistrationRequest {
    PersonInfo applicant
    UUID programId
    UUID studyLevelId
    UUID academicYearId
    String baccalaureateSection
    BigDecimal baccalaureateAverage
    PreRegistrationStatus status
    UUID reviewedByUserId
    Instant reviewedAt
    String decisionNote
  }
  class PreRegistrationDocument {
    String label
    UUID fileId
  }
  class Enrollment {
    UUID academicYearId
    UUID studyLevelId
    LocalDate enrollmentDate
    EnrollmentType enrollmentType
    EnrollmentStatus status
    boolean repeating
    Instant registrationFormPrintedAt
  }
  class ClassAssignment {
    UUID schoolClassId
    String groupCode
    LocalDate startDate
    LocalDate endDate
    PedagogicalStatus pedagogicalStatus
  }
  class Sanction {
    SanctionType sanctionType
    String reason
    LocalDate incidentDate
    LocalDate startDate
    LocalDate endDate
    UUID issuedByUserId
    boolean notifyGuardians
    Instant notifiedAt
  }
  class DocumentType {
    String code
    LocalizedText label
    DocumentLanguage language
    UUID templateFileId
    boolean active
  }
  class DocumentRequest {
    String reason
    UUID proofFileId
    DocumentRequestStatus status
    UUID processedByUserId
    Instant processedAt
    UUID generatedFileId
    Instant collectedAt
    String rejectionReason
  }
  class StudentCard {
    UUID academicYearId
    String cardNumber
    String qrToken
    LocalDate issuedOn
    LocalDate validUntil
    UUID pdfFileId
  }
  class PrintLog {
    PrintKind kind
    String registryNumber
    String documentName
  }
  class AcademicYear {
    <<parametrage>>
  }
  class StudyLevel {
    <<parametrage>>
  }
  class SchoolClass {
    <<parametrage>>
  }
  class Program {
    <<parametrage>>
  }
  Student "1" *-- "0..*" StudentGuardian
  StudentGuardian "0..*" --> "1" Guardian
  StudentGuardian "0..*" --> "1" Kinship
  SiblingRelation "0..*" --> "1" Student : student
  SiblingRelation "0..*" --> "1" Student : sibling
  PreRegistrationRequest "1" *-- "0..*" PreRegistrationDocument
  PreRegistrationRequest "0..1" --> "0..1" Student : convertedTo
  Student "1" *-- "0..*" Enrollment
  Enrollment "1" *-- "1..*" ClassAssignment
  Student "1" *-- "0..*" Sanction
  Student "1" --> "0..*" DocumentRequest
  DocumentRequest "0..*" --> "1" DocumentType
  Student "1" *-- "0..*" StudentCard
  PrintLog "0..*" --> "0..1" Student
  Enrollment "0..*" ..> "1" AcademicYear : academicYearId
  Enrollment "0..*" ..> "1" StudyLevel : studyLevelId
  ClassAssignment "0..*" ..> "1" SchoolClass : schoolClassId
  PreRegistrationRequest "0..*" ..> "1" Program : programId
```

- The login ID shown on the student profile (e.g. `12345678`) is `UserAccount.username`. The `matricule` (e.g. `10001`) is a separate number.
- **Exclusions** ("Exclus") are a `Sanction` of type `TEMPORARY_EXCLUSION` / `DEFINITIVE_EXCLUSION`, together with `Student.status = EXCLUDED`.
- The document flow Déposée → En traitement → Traitée maps to `DocumentRequestStatus` SUBMITTED → IN_PROGRESS → DONE. The 11 document types are `DocumentType` rows, one per language version.

---

## 6. `pedagogie` (1/2) — curriculum, teachers and workload

```mermaid
classDiagram
  class Teacher {
    PersonInfo person
    String employeeNumber
    UUID departmentId
    EmploymentType employmentType
    LocalDate hireDate
    Money hourlyRate
    Integer statutoryHours
    Integer maxWeeklyHours
    String bankAccountIban
    boolean departmentHead
    boolean active
  }
  class TeacherGrade {
    String code
    LocalizedText label
  }
  class TeacherDomain {
    UUID teachingDomainId
    boolean primaryDomain
  }
  class Subject {
    String code
    LocalizedText name
    String description
    UUID teachingDomainId
    BigDecimal defaultCoefficient
    Integer defaultCredits
    boolean active
  }
  class StudyPlan {
    String code
    String description
    UUID studyLevelId
    Integer semesterNumber
    StudyPlanStatus status
  }
  class TeachingUnit {
    String code
    LocalizedText name
    BigDecimal coefficient
    Integer credits
    Integer orderIndex
  }
  class StudyPlanSubject {
    BigDecimal coefficient
    Integer credits
    Integer lectureHours
    Integer tutorialHours
    Integer labHours
    Integer projectHours
  }
  class SubjectAssessment {
    AssessmentType assessmentType
    BigDecimal weightPercent
    BigDecimal maxScore
  }
  class TeachingAssignment {
    UUID schoolClassId
    UUID academicYearId
    SessionType sessionType
    Integer plannedHours
  }
  class TeacherWorkload {
    UUID academicYearId
    Integer statutoryHours
    Integer assignedHours
    WorkloadStatus status
  }
  class TeacherSubjectPreference {
    UUID academicYearId
    UUID studyLevelId
    Integer priority
    String comment
  }
  class TeacherAvailabilityPreference {
    UUID academicYearId
    DayOfWeek dayOfWeek
    LocalTime startTime
    LocalTime endTime
    PreferenceLevel preferenceLevel
  }
  class Department {
    <<parametrage>>
  }
  class TeachingDomain {
    <<parametrage>>
  }
  class StudyLevel {
    <<parametrage>>
  }
  class SchoolClass {
    <<parametrage>>
  }
  Teacher "0..*" --> "1" TeacherGrade
  Teacher "1" *-- "0..*" TeacherDomain
  StudyPlan "1" *-- "1..*" TeachingUnit
  TeachingUnit "1" *-- "1..*" StudyPlanSubject
  StudyPlanSubject "0..*" --> "1" Subject
  StudyPlanSubject "1" *-- "1..*" SubjectAssessment
  StudyPlanSubject "1" --> "0..*" TeachingAssignment
  TeachingAssignment "0..*" --> "0..1" Teacher
  TeacherWorkload "0..*" --> "1" Teacher
  TeacherWorkload "0..1" o-- "0..*" TeachingAssignment : lines
  Teacher "1" *-- "0..*" TeacherSubjectPreference
  TeacherSubjectPreference "0..*" --> "1" Subject
  Teacher "1" *-- "0..*" TeacherAvailabilityPreference
  Teacher "0..*" ..> "0..1" Department : departmentId
  TeacherDomain "0..*" ..> "1" TeachingDomain : teachingDomainId
  Subject "0..*" ..> "0..1" TeachingDomain : teachingDomainId
  StudyPlan "0..*" ..> "1" StudyLevel : studyLevelId
  TeachingAssignment "0..*" ..> "1" SchoolClass : schoolClassId
```

- `StudyPlan` (e.g. "ING-1-INFO-Semestre 1") contains `TeachingUnit`s, which contain `StudyPlanSubject`s with coefficients, credits and hours. Each `SubjectAssessment` defines one grade component (Projet, Test, Examen…) and its weight.
- `TeachingAssignment` is "which teacher teaches which subject, to which class, for how many hours". It is the line used by the *Fiche de charge* / *Répartition des matières* (`TeacherWorkload`) and by *Charge par matière*.
- "Enseignant par domaine" (*Spécialités des enseignants*) is `TeacherDomain`.

---

## 7. `pedagogie` (2/2) — course follow-up, requests and training

```mermaid
classDiagram
  class TeachingProgram {
    String title
    String objectives
    String prerequisites
    String bibliography
    Integer versionNumber
  }
  class ProgramChapter {
    Integer orderIndex
    String title
    String content
    Integer plannedHours
  }
  class ProgressEntry {
    LocalDate plannedDate
    LocalDate actualDate
    ProgressStatus status
    Integer completionPercent
    String comment
  }
  class CourseJournalEntry {
    UUID sessionOccurrenceId
    String content
    boolean confirmed
    UUID confirmedByUserId
    Instant confirmedAt
  }
  class Homework {
    UUID sessionOccurrenceId
    String title
    String description
    LocalDate assignedDate
    LocalDate dueDate
    UUID attachmentFileId
    boolean confirmed
  }
  class CourseMaterial {
    String title
    MaterialType materialType
    String description
    UUID fileId
    Instant publishedAt
    boolean visibleToStudents
  }
  class MakeUpRequest {
    UUID originalOccurrenceId
    UUID makeUpOccurrenceId
    LocalDate requestedDate
    LocalTime startTime
    LocalTime endTime
    UUID roomId
    String reason
    MakeUpStatus status
    UUID decidedByUserId
    Instant decidedAt
  }
  class TeacherMeetingSlot {
    DayOfWeek dayOfWeek
    LocalDate specificDate
    LocalTime startTime
    LocalTime endTime
    String location
    Integer capacity
    boolean active
  }
  class MeetingRequest {
    RequesterType requesterType
    UUID requesterPersonId
    UUID studentId
    LocalDate meetingDate
    String topic
    MeetingStatus status
    String note
  }
  class Observation {
    UUID studentId
    UUID schoolClassId
    ObservationCategory category
    String content
    LocalDate observedOn
    Visibility visibility
  }
  class ContinuousTraining {
    String title
    String provider
    String description
    LocalDate startDate
    LocalDate endDate
    Integer hours
    TrainingStatus status
  }
  class TrainingParticipation {
    boolean attended
    UUID certificateFileId
  }
  class StudyPlanSubject
  class TeachingAssignment
  class Teacher
  class SessionOccurrence {
    <<emploidutemps>>
  }
  class Student {
    <<scolarite>>
  }
  StudyPlanSubject "1" --> "0..*" TeachingProgram
  TeachingProgram "1" *-- "1..*" ProgramChapter
  TeachingAssignment "1" *-- "0..*" ProgressEntry
  ProgressEntry "0..*" --> "1" ProgramChapter
  TeachingAssignment "1" --> "0..*" CourseJournalEntry
  CourseJournalEntry "0..*" --> "0..1" ProgramChapter
  TeachingAssignment "1" --> "0..*" Homework
  TeachingAssignment "1" --> "0..*" CourseMaterial
  MakeUpRequest "0..*" --> "1" TeachingAssignment
  Teacher "1" --> "0..*" MakeUpRequest
  Teacher "1" *-- "0..*" TeacherMeetingSlot
  TeacherMeetingSlot "1" --> "0..*" MeetingRequest
  Observation "0..*" --> "0..1" Teacher
  ContinuousTraining "1" *-- "0..*" TrainingParticipation
  TrainingParticipation "0..*" --> "1" Teacher
  CourseJournalEntry "0..*" ..> "1" SessionOccurrence : sessionOccurrenceId opaque
  MakeUpRequest "0..*" ..> "1" SessionOccurrence : originalOccurrenceId opaque
  MeetingRequest "0..*" ..> "0..1" Student : studentId
  Observation "0..*" ..> "0..1" Student : studentId
```

- The boxes without attributes and without a `<<module>>` label (`StudyPlanSubject`, `TeachingAssignment`, `Teacher`) belong to `pedagogie` too. They're defined in §6.
- `MeetingRequest` covers both *Rendez-vous* (staff side) and *Demandes des responsables / Prise RDV* (parents). `requesterType` tells them apart.
- When a `MakeUpRequest` is **confirmed**, the `MakeUpConfirmed` event lets `emploidutemps` create the make-up `SessionOccurrence`. Its ID is then written back to `makeUpOccurrenceId`.

---

## 8. `emploidutemps` — timetables and sessions

```mermaid
classDiagram
  class TimeSlotTemplate {
    String code
    LocalTime startTime
    LocalTime endTime
    Integer orderIndex
  }
  class Timetable {
    UUID schoolClassId
    UUID academicPeriodId
    String name
    LocalDate validFrom
    LocalDate validTo
    TimetableStatus status
    GenerationMode generationMode
    Instant generatedAt
    Integer versionNumber
  }
  class TimetableSlot {
    UUID teachingAssignmentId
    UUID teacherId
    UUID subjectId
    UUID roomId
    DayOfWeek dayOfWeek
    LocalTime startTime
    LocalTime endTime
    WeekPattern weekPattern
    boolean outOfControl
  }
  class SessionOccurrence {
    UUID teachingAssignmentId
    UUID teacherId
    UUID subjectId
    UUID schoolClassId
    UUID roomId
    LocalDate sessionDate
    LocalTime startTime
    LocalTime endTime
    SessionType sessionType
    OccurrenceStatus status
    boolean makeUp
    UUID makeUpRequestId
    String cancellationReason
  }
  class SchedulingRule {
    String name
    RuleType ruleType
    String parametersJson
    Integer weight
    boolean hardConstraint
    UUID studyLevelId
    UUID schoolClassId
    UUID teacherId
    boolean active
  }
  class SlotConflict {
    <<readModel>>
    ConflictType conflictType
    LocalDate conflictDate
    Instant detectedAt
    boolean resolved
  }
  class TeachingAssignment {
    <<pedagogie>>
  }
  class Room {
    <<parametrage>>
  }
  class SchoolClass {
    <<parametrage>>
  }
  class AcademicPeriod {
    <<parametrage>>
  }
  Timetable "1" *-- "0..*" TimetableSlot
  TimetableSlot "0..*" --> "0..1" TimeSlotTemplate
  TimetableSlot "0..1" --> "0..*" SessionOccurrence : generates
  SessionOccurrence "0..1" --> "0..1" SessionOccurrence : makeUpOf
  SlotConflict "0..*" --> "1..*" SessionOccurrence
  Timetable "0..*" ..> "1" SchoolClass : schoolClassId
  Timetable "0..*" ..> "1" AcademicPeriod : academicPeriodId
  TimetableSlot "0..*" ..> "1" TeachingAssignment : teachingAssignmentId
  TimetableSlot "0..*" ..> "0..1" Room : roomId
  SessionOccurrence "0..*" ..> "0..1" Room : roomId
```

- `TimetableSlot` is the weekly pattern. `SessionOccurrence` is the actual dated session that calls, class logs, teacher attendance and make-ups attach to (*Programme des séances*).
- `teacherId` and `subjectId` are **copied** from the `TeachingAssignment` so that conflict checks don't call `pedagogie` for every slot. A replacement teacher simply changes `SessionOccurrence.teacherId`.
- **Générer (Optimisé)** runs the Timefold Solver worker on `TimetableSlot`s while respecting the `SchedulingRule`s. Then it sets `generationMode = OPTIMIZED`.
- **Contrôle des créneaux** reads `SlotConflict` (room conflict, no teacher, no room). Marking "hors contrôle" sets `TimetableSlot.outOfControl`.

---

## 9. `assiduite` — calls, absences and teacher attendance

```mermaid
classDiagram
  class AttendancePolicy {
    UUID studyLevelId
    BigDecimal atRiskPercent
    BigDecimal criticalPercent
    Integer callDeadlineMinutes
    Integer editWindowHours
    boolean countOnlyConfirmedCalls
  }
  class AttendanceSheet {
    UUID sessionOccurrenceId
    UUID schoolClassId
    UUID teacherId
    AttendanceSheetStatus status
    Instant confirmedAt
    Instant editableUntil
  }
  class AttendanceRecord {
    UUID studentId
    AttendanceStatus status
    Integer lateMinutes
    boolean justified
    String comment
    Instant guardiansNotifiedAt
  }
  class StudentAbsenceRequest {
    UUID studentId
    LocalDateTime fromDateTime
    LocalDateTime toDateTime
    String reason
    UUID proofFileId
    RequestStatus status
    UUID reviewedByUserId
    Instant reviewedAt
  }
  class TeacherAttendance {
    UUID sessionOccurrenceId
    UUID teacherId
    LocalDate sessionDate
    Integer weekNumber
    TeacherPresenceStatus status
    BigDecimal hours
    boolean validated
    UUID validatedByUserId
    Instant validatedAt
  }
  class AbsenceStatistic {
    <<readModel>>
    UUID studentId
    UUID subjectId
    UUID academicPeriodId
    Integer sessionsCount
    Integer absences
    Integer lates
    BigDecimal absenceRate
    RiskLevel riskLevel
  }
  class SessionOccurrence {
    <<emploidutemps>>
  }
  class Student {
    <<scolarite>>
  }
  class Teacher {
    <<pedagogie>>
  }
  AttendanceSheet "1" *-- "0..*" AttendanceRecord
  AttendanceRecord "0..*" --> "0..1" StudentAbsenceRequest : justifiedBy
  AttendanceSheet "0..1" ..> "1" SessionOccurrence : sessionOccurrenceId
  AttendanceSheet "0..*" ..> "1" Teacher : teacherId
  AttendanceRecord "0..*" ..> "1" Student : studentId
  StudentAbsenceRequest "0..*" ..> "1" Student : studentId
  TeacherAttendance "0..1" ..> "1" SessionOccurrence : sessionOccurrenceId
  TeacherAttendance "0..*" ..> "1" Teacher : teacherId
```

- When `emploidutemps` publishes `SessionScheduled`, an `AttendanceSheet` is opened for the session.
  - `callDeadlineMinutes` drives the "Délai dépassé" (deadline passed) state.
  - `editableUntil` drives *Modifier l'appel* (edit the call).
  - The 10 % / 25 % thresholds come from `AttendancePolicy`.
- `TeacherAttendance` is the *Présence des enseignants* line (week, date, hours). The **payment** of those hours belongs to `finances` (§12), so this class has no "paid" field. That keeps `assiduite` from depending on `finances`.

---

## 10. `examens` — exams, grades and results

```mermaid
classDiagram
  class ExamSession {
    String name
    ExamSessionType sessionType
    UUID academicPeriodId
    LocalDate startDate
    LocalDate endDate
    boolean published
  }
  class Exam {
    UUID studyPlanSubjectId
    AssessmentType assessmentType
    LocalDate examDate
    LocalTime startTime
    LocalTime endTime
    Integer durationMinutes
    boolean published
    boolean roomsPublished
  }
  class ExamClass {
    UUID schoolClassId
  }
  class ExamRoomAllocation {
    UUID roomId
    UUID schoolClassId
    Integer seatCount
    String seatingRange
  }
  class Invigilation {
    UUID teacherId
    UUID roomId
    InvigilationRole role
    boolean published
  }
  class GradeGrid {
    UUID studyPlanSubjectId
    UUID schoolClassId
    UUID academicPeriodId
    UUID teacherId
    AssessmentType assessmentType
    BigDecimal maxScore
    GradeGridStatus status
    Instant confirmedAt
    Instant publishedAt
  }
  class GradeEntry {
    UUID studentId
    BigDecimal score
    GradeReason reason
    String comment
  }
  class SubjectResult {
    UUID studentId
    UUID studyPlanSubjectId
    UUID academicPeriodId
    BigDecimal average
    Integer creditsEarned
    boolean validated
    ResultSession resultSession
  }
  class ReportCard {
    UUID studentId
    UUID academicPeriodId
    BigDecimal average
    Integer rankInClass
    Integer creditsEarned
    String appreciationText
    UUID pdfFileId
    Instant generatedAt
    boolean published
  }
  class ClassDecision {
    UUID studentId
    UUID academicYearId
    DecisionType decision
    LocalDate juryDate
    String comment
  }
  class AppreciationScale {
    LocalizedText label
    BigDecimal minAverage
    BigDecimal maxAverage
  }
  class StudyPlanSubject {
    <<pedagogie>>
  }
  class Teacher {
    <<pedagogie>>
  }
  class Student {
    <<scolarite>>
  }
  class Room {
    <<parametrage>>
  }
  ExamSession "1" *-- "0..*" Exam
  Exam "1" *-- "1..*" ExamClass
  Exam "1" *-- "0..*" ExamRoomAllocation
  Exam "1" *-- "0..*" Invigilation
  GradeGrid "0..*" --> "0..1" Exam
  GradeGrid "1" *-- "0..*" GradeEntry
  ReportCard "1" o-- "0..*" SubjectResult
  ReportCard "0..*" --> "0..1" AppreciationScale
  Exam "0..*" ..> "1" StudyPlanSubject : studyPlanSubjectId
  GradeGrid "0..*" ..> "1" StudyPlanSubject : studyPlanSubjectId
  ExamRoomAllocation "0..*" ..> "1" Room : roomId
  Invigilation "0..*" ..> "1" Teacher : teacherId
  GradeEntry "0..*" ..> "1" Student : studentId
```

- **GradeEntry**: either a `score` out of `maxScore` (20) **or** a `reason` (Absence justifiée, Absence non justifiée, Dispensé(e), Fraude). One of the two must be set.
- **Saving vs confirming:** *Enregistrer* keeps the grid `IN_PROGRESS`. *Confirmer la grille* sets `CONFIRMED`. Publishing to students sets `PUBLISHED`. Hibernate Envers keeps every earlier value of a grade.
- **Generation buttons:** *Générer grille de saisie* creates `GradeGrid`s from the teacher workloads. *Générer bulletin des notes* creates `ReportCard`s. *Générer affectation des appréciations* applies the `AppreciationScale`. *Générer grilles de décisions* creates `ClassDecision`s.
- These endpoints live in `examens`, even when the button appears on a *Pédagogie* screen. That avoids a `pedagogie` → `examens` dependency.

---

## 11. `stages` — internships and defenses

```mermaid
classDiagram
  class InternshipNature {
    String code
    LocalizedText label
    UUID studyLevelId
    boolean requiresDefense
    Integer minWeeks
  }
  class Company {
    String name
    String service
    String taxNumber
    String sector
    Address address
    String phone
    String email
    String website
    boolean partner
    boolean active
  }
  class CompanySupervisor {
    String firstName
    String lastName
    String position
    String email
    String phone
  }
  class Internship {
    UUID studentId
    UUID academicYearId
    UUID academicSupervisorId
    String referenceNumber
    String title
    String objectives
    LocalDate startDate
    LocalDate endDate
    InternshipStatus status
    UUID approvedByUserId
    Instant approvedAt
    String refusalReason
    Instant defenseRequestedAt
  }
  class InternshipStatusChange {
    InternshipStatus fromStatus
    InternshipStatus toStatus
    String comment
  }
  class InternshipDocument {
    InternshipDocumentType documentType
    UUID fileId
    boolean locked
  }
  class DefenseSession {
    String name
    UUID academicYearId
    LocalDate startDate
    LocalDate endDate
    LocalDate requestsOpenFrom
    LocalDate requestsOpenTo
  }
  class Jury {
    String code
    LocalDate juryDate
    UUID roomId
  }
  class JuryMember {
    UUID teacherId
    String externalName
    JuryRole role
  }
  class Defense {
    LocalDate defenseDate
    LocalTime startTime
    LocalTime endTime
    DefenseStatus status
    BigDecimal grade
    Mention mention
    boolean gradeValidated
    boolean published
    Instant publishedAt
  }
  class DefenseEvaluation {
    BigDecimal score
    String comment
  }
  class Student {
    <<scolarite>>
  }
  class Teacher {
    <<pedagogie>>
  }
  class Room {
    <<parametrage>>
  }
  Internship "0..*" --> "1" InternshipNature
  Internship "0..*" --> "1" Company
  Internship "0..*" --> "0..1" CompanySupervisor
  Company "1" *-- "0..*" CompanySupervisor
  Internship "1" *-- "0..*" InternshipStatusChange
  Internship "1" *-- "0..*" InternshipDocument
  Internship "0..*" --> "0..1" DefenseSession : requestedSession
  DefenseSession "1" *-- "0..*" Jury
  Jury "1" *-- "2..*" JuryMember
  Jury "1" --> "0..*" Defense
  Internship "1" --> "0..1" Defense
  Defense "1" *-- "0..*" DefenseEvaluation
  DefenseEvaluation "0..*" --> "1" JuryMember
  Internship "0..*" ..> "1" Student : studentId
  Internship "0..*" ..> "0..1" Teacher : academicSupervisorId
  JuryMember "0..*" ..> "0..1" Teacher : teacherId
  Jury "0..*" ..> "0..1" Room : roomId
```

- `InternshipStatus` follows the student timeline exactly: En cours d'approbation → Approuvé → Demande de soutenance → Autorisé par l'encadrant → Autorisé à soutenir. `REFUSED`, `DEFENDED` and `VALIDATED` are added to finish the flow.
- Every change of status is kept in `InternshipStatusChange`.
- `InternshipDocument.locked` becomes `true` when the status reaches `AUTHORIZED_TO_DEFEND`. That's the student's "le rapport et l'attestation ne sont plus modifiables" (the report and certificate can no longer be changed).
- `JuryMember.teacherId` is empty for external members. `externalName` is filled instead.

---

## 12. `finances` — fees, payments and teacher pay

```mermaid
classDiagram
  class FeeSchedule {
    UUID academicYearId
    UUID studyLevelId
    FeeType feeType
    Money amount
    Integer installmentsCount
    boolean active
  }
  class StudentFeePlan {
    UUID studentId
    UUID enrollmentId
    Money totalAmount
    Money discount
    Money scholarship
    FeePlanStatus status
  }
  class Installment {
    Integer installmentNumber
    LocalDate dueDate
    Money amount
    Money paidAmount
    InstallmentStatus status
  }
  class Payment {
    UUID studentId
    Money amount
    Instant paidAt
    PaymentMethod method
    String reference
    String receiptNumber
  }
  class PaymentAllocation {
    Money amount
  }
  class PaymentReminder {
    Integer reminderLevel
    Channel channel
    String message
    Instant sentAt
  }
  class TeacherPayment {
    UUID teacherId
    LocalDate periodFrom
    LocalDate periodTo
    BigDecimal totalHours
    Money hourlyRate
    Money totalAmount
    TeacherPaymentStatus status
    UUID paidByUserId
    Instant paidAt
    Instant cancelledAt
  }
  class TeacherPaymentLine {
    UUID teacherAttendanceId
    LocalDate sessionDate
    BigDecimal hours
    Money amount
  }
  class Enrollment {
    <<scolarite>>
  }
  class Student {
    <<scolarite>>
  }
  class Teacher {
    <<pedagogie>>
  }
  class TeacherAttendance {
    <<assiduite>>
  }
  StudentFeePlan "0..*" --> "1..*" FeeSchedule : basedOn
  StudentFeePlan "1" *-- "1..*" Installment
  Payment "1" *-- "1..*" PaymentAllocation
  PaymentAllocation "0..*" --> "1" Installment
  Installment "1" *-- "0..*" PaymentReminder
  TeacherPayment "1" *-- "1..*" TeacherPaymentLine
  StudentFeePlan "0..1" ..> "1" Enrollment : enrollmentId
  Payment "0..*" ..> "1" Student : studentId
  TeacherPayment "0..*" ..> "1" Teacher : teacherId
  TeacherPaymentLine "0..1" ..> "1" TeacherAttendance : teacherAttendanceId
```

- This module covers **Tarification** (`StudentFeePlan`), *échéances, paiements, relances* (`Installment`, `Payment`, `PaymentReminder`), and the *Payer* / *Annuler paiements* actions (`TeacherPayment`, status `CANCELLED`). It also produces the data behind the *Attestation de paiement*.
- One `Payment` can settle several `Installment`s through `PaymentAllocation`.
- A `TeacherAttendance` line can be paid only once: `teacherAttendanceId` is **unique** among the lines of non-cancelled payments.

---

## 13. `communication` — messages, chat, news and notifications

```mermaid
classDiagram
  class Message {
    String subject
    String body
    String smsText
    Audience audience
    MessageCategory category
    boolean sendPush
    boolean sendSms
    boolean sendEmail
    boolean alsoNotifyGuardians
    Instant sentAt
  }
  class MessageAttachment {
    UUID fileId
  }
  class MessageAudienceFilter {
    boolean allRecipients
    UUID studyLevelId
    UUID schoolClassId
  }
  class MessageRecipient {
    UUID recipientUserId
    Instant deliveredAt
    Instant readAt
    DeliveryStatus smsStatus
    boolean archived
  }
  class Conversation {
    ConversationType conversationType
    ConversationScope scope
    String title
    Instant lastMessageAt
  }
  class ConversationParticipant {
    UUID userId
    Instant lastReadAt
    boolean muted
  }
  class ChatMessage {
    String content
    UUID attachmentFileId
  }
  class News {
    String title
    String body
    UUID coverImageFileId
    Audience audience
    NewsStatus status
    UUID publishedByUserId
    Instant publishedAt
    Instant notifiedAt
  }
  class NewsAttachment {
    UUID fileId
  }
  class Notification {
    UUID recipientUserId
    NotificationType notificationType
    String title
    String body
    String targetType
    UUID targetId
    Instant readAt
    Instant pushSentAt
    Instant smsSentAt
  }
  class NotificationPreference {
    UUID userId
    NotificationType notificationType
    boolean inApp
    boolean push
    boolean email
    boolean sms
  }
  class DoNotDisturbSetting {
    UUID userId
    DndMode mode
    LocalTime fromTime
    LocalTime toTime
  }
  class AttentionPoint {
    <<readModel>>
    AppModule sourceModule
    String code
    Severity severity
    Integer itemCount
    LocalizedText title
    String targetLink
    Instant computedAt
  }
  class UserAccount {
    <<administration>>
  }
  class SchoolClass {
    <<parametrage>>
  }
  Message "1" *-- "0..*" MessageAttachment
  Message "1" *-- "0..*" MessageAudienceFilter
  Message "1" *-- "1..*" MessageRecipient
  Message "0..*" --> "0..1" Message : replyTo
  Conversation "1" *-- "2..*" ConversationParticipant
  Conversation "1" *-- "0..*" ChatMessage
  News "1" *-- "0..*" NewsAttachment
  MessageRecipient "0..*" ..> "1" UserAccount : recipientUserId
  ConversationParticipant "0..*" ..> "1" UserAccount : userId
  Notification "0..*" ..> "1" UserAccount : recipientUserId
  MessageAudienceFilter "0..*" ..> "0..1" SchoolClass : schoolClassId
```

- **Sender and timestamps:** the sender of a `Message` or `ChatMessage` is `createdByUserId`, and its time is `createdAt`. Editing a chat message updates `updatedAt`. Deleting it sets `archivedAt`.
- **Audience:** *Nouveau message* uses `Audience` = STUDENTS / GUARDIANS / TEACHERS, with groups (`MessageAudienceFilter`: all, by level, by class). The send options are push / SMS (`smsText`, 160 characters) and "Prévenir aussi les parents" (also notify parents).
- **Conversation scopes:** `STAFF_INTERNAL` covers *Discussions internes*, `TEACHER_STUDENT` covers *Chat enseignants – apprenants*, and `STUDENT_STUDENT` covers student-to-classmate discussions.
- **Notifications envoyées** (push statistics) is a query over `Notification`. **Points d'attention** (home dashboard, bell) come from `AttentionPoint`, refreshed from the other modules' events and APIs.

---

## 14. `vieetablissement` — campus life and services

```mermaid
classDiagram
  class Survey {
    String title
    String description
    SurveyPurpose purpose
    Audience audience
    UUID teachingAssignmentId
    Instant opensAt
    Instant closesAt
    boolean anonymous
  }
  class SurveyQuestion {
    Integer orderIndex
    String text
    QuestionType questionType
    String optionsJson
    boolean required
  }
  class SurveyResponse {
    UUID respondentUserId
    String answersJson
  }
  class CanteenMenu {
    LocalDate menuDate
    MealType mealType
    String items
    Money price
  }
  class MealReservation {
    PersonType personType
    UUID personId
    ReservationStatus status
  }
  class InfirmaryVisit {
    UUID studentId
    Instant visitedAt
    String reason
    String careGiven
    boolean guardiansNotified
    String followUp
  }
  class AccessPoint {
    UUID siteId
    String code
    String name
  }
  class AccessEvent {
    PersonType personType
    UUID personId
    Instant occurredAt
    AccessDirection direction
    AccessMethod method
    boolean granted
  }
  class EntryTicket {
    UUID studentId
    UUID attendanceRecordId
    EntryTicketReason reason
    LocalDate validForDate
    String note
  }
  class Student {
    <<scolarite>>
  }
  class AttendanceRecord {
    <<assiduite>>
  }
  class TeachingAssignment {
    <<pedagogie>>
  }
  Survey "1" *-- "1..*" SurveyQuestion
  Survey "1" *-- "0..*" SurveyResponse
  CanteenMenu "1" --> "0..*" MealReservation
  AccessPoint "1" --> "0..*" AccessEvent
  InfirmaryVisit "0..*" ..> "1" Student : studentId
  EntryTicket "0..*" ..> "1" Student : studentId
  EntryTicket "0..1" ..> "0..1" AttendanceRecord : attendanceRecordId
  Survey "0..*" ..> "0..1" TeachingAssignment : teachingAssignmentId
```

- **Where these come from:** this module covers screens found in the navigation-permission list (Cantine, Infirmerie, Contrôle d'accès, Billets d'entrée, Evaluation) and the module description (actualités, sondages, services). Their attributes are a **proposal**, because these screens weren't visible to the tested role.
- **Course evaluations:** "Evaluation" is modelled as a `Survey` with `purpose = COURSE_EVALUATION`, linked to a `TeachingAssignment`.
- **Anonymous surveys:** `respondentUserId` stays empty, and `SurveyResponse` must be **excluded from auditing**. Otherwise `createdByUserId` would reveal who answered.

---

## 15. Domain events (Spring Modulith)

Published through the event publication registry (outbox) and delivered to RabbitMQ for the workers.

| Event | Published by | Consumed by |
|---|---|---|
| `RoleChanged` | administration | administration (permission cache),<br>communication |
| `UserDeactivated` | administration | communication |
| `AcademicYearOpened` | parametrage | scolarite, pedagogie |
| `StudentEnrolled` | scolarite | finances, communication |
| `DocumentRequestStatusChanged` | scolarite | communication |
| `SanctionIssued` | scolarite | communication |
| `MaterialPublished` | pedagogie | communication |
| `MakeUpConfirmed` | pedagogie | emploidutemps, communication |
| `SessionScheduled` | emploidutemps | assiduite, communication |
| `SessionCancelled` | emploidutemps | assiduite, communication |
| `AbsenceRecorded` | assiduite | communication |
| `AbsenceThresholdExceeded` | assiduite | communication |
| `AbsenceRequestDecided` | assiduite | communication |
| `TeacherSessionValidated` | assiduite | finances |
| `GradeGridConfirmed` | examens | communication |
| `GradesPublished` | examens | communication |
| `InvigilationPublished` | examens | communication |
| `DefenseAuthorized` | stages | communication |
| `DefenseGradePublished` | stages | examens, communication |
| `PaymentReceived` | finances | communication |
| `TeacherHoursPaid` | finances | communication |
| `NewsPublished` | communication | communication (push) |

Every consumer depends on the publisher in the §1 graph, so listening to events never creates a cycle.

---

## 16. Enumerations

**`shared`** (used by several modules)
- `Gender`: MALE · FEMALE
- `PersonType`: STUDENT · TEACHER · GUARDIAN · STAFF
- `AppModule`: SCOLARITE · PEDAGOGIE · EMPLOIS_DU_TEMPS · EXAMENS_NOTES · STAGES · FINANCES · VIE_ETABLISSEMENT · COMMUNICATION · PARAMETRAGE · ADMINISTRATION
- `SessionType`: LECTURE · TUTORIAL · LAB · PROJECT
- `AssessmentType`: PROJECT · TEST · SUPERVISED_TEST · EXAM · PRACTICAL · ORAL · PFE
- `Audience`: ALL · STUDENTS · GUARDIANS · TEACHERS · STAFF
- `ScanStatus`: PENDING · CLEAN · INFECTED

**`parametrage`**
- `RoomType`: CLASSROOM · LAB · AMPHITHEATRE · MEETING_ROOM · OTHER
- `AcademicYearStatus`: PLANNED · OPEN · CLOSED
- `PeriodType`: SEMESTER · TRIMESTER · YEAR
- `CalendarEventType`: HOLIDAY · EXAM_PERIOD · MAKE_UP_WEEK · DEFENSE_PERIOD · BREAK · EVENT
- `StudyRhythm`: DAY (J) · EVENING (S) · OTHER (P), meaning to confirm
- `Portal`: STUDENT · TEACHER · BACK_OFFICE · MOBILE

**`administration`**
- `PermissionPolicy`: DENY_ALL_BY_DEFAULT · READ_ONLY_ALL_BY_DEFAULT · ALLOW_ALL_BY_DEFAULT
- `PermissionState`: ALLOW · DENY · INHERIT
- `RolePreset`: DIRECTION · SCOLARITE · PEDAGOGIE · EXAMENS · FINANCES · CUSTOM
- `RoleChangeType`: CREATE_ROLE · CHANGE_POLICY · GRANT_ADMINISTRATIVE · GRANT_CAN_EDIT_MODEL · ASSIGN_ROLE · DELETE_ROLE
- `ApprovalStatus`: PENDING · APPROVED · REJECTED
- `ThemeMode`: SYSTEM · LIGHT · DARK
- `DisplayDensity`: COMPACT · NORMAL · LARGE

**`scolarite`**
- `StudentStatus`: ACTIVE · SUSPENDED · EXCLUDED · GRADUATED · WITHDRAWN
- `PreRegistrationStatus`: SUBMITTED · UNDER_REVIEW · ACCEPTED · REJECTED · CONVERTED
- `EnrollmentType`: NEW · RE_ENROLLMENT · TRANSFER
- `EnrollmentStatus`: PENDING · ENROLLED · CANCELLED · TRANSFERRED_OUT
- `PedagogicalStatus`: REGULAR · REPEATING · AUDITOR
- `SanctionType`: WARNING · BLAME · TEMPORARY_EXCLUSION · DEFINITIVE_EXCLUSION
- `DocumentLanguage`: FR · EN · AR
- `DocumentRequestStatus`: SUBMITTED · IN_PROGRESS · DONE · REJECTED
- `PrintKind`: ATTENDANCE_SHEET · REGISTRATION_FORM

**`pedagogie`**
- `EmploymentType`: PERMANENT · CONTRACT · VISITING
- `StudyPlanStatus`: DRAFT · ACTIVE · ARCHIVED
- `WorkloadStatus`: DRAFT · VALIDATED
- `PreferenceLevel`: PREFERRED · AVAILABLE · UNAVAILABLE
- `ProgressStatus`: NOT_STARTED · IN_PROGRESS · DONE
- `MaterialType`: COURSE · SUPERVISED_TEST · EXAM · PROJECT · QUIZ · TUTORIAL · LAB
- `MakeUpStatus`: REQUESTED · CONFIRMED · REJECTED · DONE
- `RequesterType`: GUARDIAN · STUDENT · STAFF
- `MeetingStatus`: REQUESTED · CONFIRMED · CANCELLED · DONE
- `ObservationCategory`: PEDAGOGICAL · BEHAVIOUR · ADMINISTRATIVE · OTHER
- `Visibility`: PRIVATE · STAFF · TEACHERS
- `TrainingStatus`: PLANNED · ONGOING · COMPLETED · CANCELLED

**`emploidutemps`**
- `TimetableStatus`: DRAFT · PUBLISHED · ARCHIVED
- `GenerationMode`: MANUAL · OPTIMIZED
- `WeekPattern`: EVERY_WEEK · ODD_WEEKS · EVEN_WEEKS · CUSTOM
- `OccurrenceStatus`: SCHEDULED · DONE · CANCELLED · TEACHER_ABSENT
- `RuleType`: MAX_HOURS_PER_DAY · NO_GAPS · ROOM_TYPE_REQUIRED · TEACHER_UNAVAILABLE · CONSECUTIVE_SESSIONS · OTHER
- `ConflictType`: ROOM_CONFLICT · TEACHER_CONFLICT · NO_TEACHER · NO_ROOM

**`assiduite`**
- `AttendanceSheetStatus`: DRAFT · CONFIRMED · LOCKED
- `AttendanceStatus`: PRESENT · ABSENT · LATE
- `RequestStatus`: PENDING · APPROVED · REJECTED
- `TeacherPresenceStatus`: PRESENT · ABSENT · REPLACED
- `RiskLevel`: OK · AT_RISK · CRITICAL

**`examens`**
- `ExamSessionType`: MAIN · RESIT · CONTINUOUS
- `InvigilationRole`: INVIGILATOR · HEAD_INVIGILATOR
- `GradeGridStatus`: OPEN · IN_PROGRESS · CONFIRMED · PUBLISHED
- `GradeReason`: JUSTIFIED_ABSENCE · UNJUSTIFIED_ABSENCE · EXEMPTED · FRAUD
- `ResultSession`: MAIN · RESIT
- `DecisionType`: ADMITTED · ADMITTED_WITH_DEBTS · RESIT · REPEAT · EXCLUDED

**`stages`**
- `InternshipStatus`: PENDING_APPROVAL · APPROVED · REFUSED · DEFENSE_REQUESTED · AUTHORIZED_BY_SUPERVISOR · AUTHORIZED_TO_DEFEND · DEFENDED · VALIDATED
- `InternshipDocumentType`: REPORT · CERTIFICATE · AGREEMENT · OTHER
- `JuryRole`: PRESIDENT · REPORTER · MEMBER · SUPERVISOR
- `DefenseStatus`: PLANNED · HELD · POSTPONED · CANCELLED
- `Mention`: PASS · FAIRLY_GOOD · GOOD · VERY_GOOD · EXCELLENT

**`finances`**
- `FeeType`: REGISTRATION · TUITION · EXAM · DOCUMENT · OTHER
- `FeePlanStatus`: ACTIVE · SETTLED · CANCELLED
- `InstallmentStatus`: DUE · PARTIALLY_PAID · PAID · OVERDUE
- `PaymentMethod`: CASH · CHEQUE · BANK_TRANSFER · CARD · ONLINE
- `Channel`: SMS · EMAIL · PUSH · LETTER
- `TeacherPaymentStatus`: PENDING · PAID · CANCELLED

**`communication`**
- `MessageCategory`: SCOLARITE · ADMINISTRATION · PEDAGOGY · OTHER
- `DeliveryStatus`: PENDING · SENT · DELIVERED · FAILED
- `ConversationType`: DIRECT · GROUP
- `ConversationScope`: STAFF_INTERNAL · TEACHER_STUDENT · STUDENT_STUDENT
- `NewsStatus`: DRAFT · PUBLISHED · ARCHIVED
- `NotificationType`: ATTENTION_POINT · REQUEST · MESSAGE · NEWS · DISCUSSION · ABSENCE · CALL · DOCUMENT · INTERNSHIP · INVIGILATION · GRADE · OTHER
- `DndMode`: OFF · ON · SCHEDULED
- `Severity`: CRITICAL · WATCH · INFO

**`vieetablissement`**
- `SurveyPurpose`: GENERAL · COURSE_EVALUATION
- `QuestionType`: SINGLE_CHOICE · MULTIPLE_CHOICE · TEXT · RATING
- `MealType`: BREAKFAST · LUNCH · DINNER
- `ReservationStatus`: RESERVED · CONSUMED · CANCELLED
- `AccessDirection`: IN · OUT
- `AccessMethod`: CARD · QR_CODE · MANUAL
- `EntryTicketReason`: LATE_ARRIVAL · AFTER_ABSENCE · OTHER

`DayOfWeek` is `java.time.DayOfWeek`.

---

## 17. Screen coverage (survey → classes)

| Screen (French name) | Classes |
|---|---|
| Emploi du temps (all portals) | `Timetable`, `TimetableSlot`, `SessionOccurrence` |
| Supports et programmes / Supports de cours | `CourseMaterial`, `TeachingProgram`, `ProgramChapter` |
| Notes (student) | `GradeEntry`, `SubjectResult`, `ReportCard` |
| Examens (student) | `Exam`, `ExamClass`, `ExamRoomAllocation` |
| Absences (student) | `AttendanceRecord`, `AbsenceStatistic`,<br>`AttendancePolicy` |
| Mon stage | `Internship`, `InternshipDocument`,<br>`InternshipStatusChange`, `Defense`, `Jury` |
| Messages / Discussions | `Message`, `MessageRecipient`,<br>`Conversation`, `ChatMessage` |
| Notifications / Réglages | `Notification`, `NotificationPreference`,<br>`DoNotDisturbSetting` |
| Actualités | `News`, `NewsAttachment` |
| Demandes de documents | `DocumentType`, `DocumentRequest` |
| Paiements (disabled) | `FeatureToggle`, `StudentFeePlan`,<br>`Installment`, `Payment` |
| Mon profil / Carte étudiant | `Student`, `StudentCard`, `UserAccount` |
| Appels (teacher) | `AttendanceSheet`, `AttendanceRecord` |
| Saisie des notes | `GradeGrid`, `GradeEntry` |
| Surveillances | `Invigilation`, `Exam` |
| Stages et soutenances (teacher) | `Internship`, `Jury`, `JuryMember`, `Defense` |
| Apprenants (actifs / liste / tous) | `Student`, `Enrollment`, `ClassAssignment` |
| Responsables / Fratries /<br>Liens de parenté | `Guardian`, `StudentGuardian`,<br>`SiblingRelation`, `Kinship` |
| Demandes de préinscription | `PreRegistrationRequest`,<br>`PreRegistrationDocument` |
| Historique d'affectation | `ClassAssignment` |
| Pilotage des absences | `AbsenceStatistic`, `AttendancePolicy` |
| Absences / Demandes d'absence | `AttendanceRecord`, `StudentAbsenceRequest` |
| Sanctions / Exclus | `Sanction` |
| Registres des fiches | `PrintLog` |
| Plans d'étude | `StudyPlan`, `TeachingUnit`,<br>`StudyPlanSubject`, `SubjectAssessment` |
| Matières | `Subject` |
| Enseignants / Spécialités | `Teacher`, `TeacherGrade`, `TeacherDomain` |
| Fiches de charge / Répartition /<br>Charge par matière | `TeacherWorkload`, `TeachingAssignment` |
| Vœux matières / emploi du temps | `TeacherSubjectPreference`,<br>`TeacherAvailabilityPreference` |
| Présence des enseignants / Payer | `TeacherAttendance`, `TeacherPayment`,<br>`TeacherPaymentLine` |
| États d'avancement / Programmes | `ProgressEntry`, `TeachingProgram`,<br>`ProgramChapter` |
| Cahier de texte / Devoirs | `CourseJournalEntry`, `Homework` |
| Séances de rattrapage | `MakeUpRequest`, `SessionOccurrence` |
| Rendez-vous / Créneaux /<br>Demandes des responsables | `MeetingRequest`, `TeacherMeetingSlot` |
| Observations | `Observation` |
| Formation continue | `ContinuousTraining`, `TrainingParticipation` |
| Emplois du temps / Générer (Optimisé) | `Timetable`, `TimetableSlot`,<br>`SchedulingRule`, `TimeSlotTemplate` |
| Contrôle des créneaux | `SlotConflict`, `TimetableSlot` |
| Programmes des séances | `SessionOccurrence` |
| Règles pédagogiques | `SchedulingRule` |
| Calendrier universitaire | `CalendarEvent` |
| Demandes de stage | `Internship`, `InternshipStatusChange`,<br>`InternshipNature` |
| Soutenances / Jurys / Sessions | `Defense`, `DefenseEvaluation`, `Jury`,<br>`JuryMember`, `DefenseSession` |
| Entreprises / Encadrants pro. | `Company`, `CompanySupervisor` |
| Nouveau message / reçus / envoyés | `Message`, `MessageAudienceFilter`,<br>`MessageRecipient`, `MessageAttachment` |
| Discussions internes / Chat | `Conversation`, `ConversationParticipant`,<br>`ChatMessage` |
| Notifications envoyées | `Notification` |
| Tableau de bord / Points d'attention | `AttentionPoint` |
| Éditeur de rôle | `Role`, `RoleModuleAccess`, `TypePermission`,<br>`ObjectPermission`, `MemberPermission`,<br>`NavigationPermission`, `DeniedAction` |
| Mes informations / 2FA / mot de passe | `UserAccount`, `UserRole` (+ Keycloak) |
| Favoris / taille d'affichage / thème | `UserFavorite`, `UserAccount` |
| Cantine / Infirmerie / Contrôle<br>d'accès / Billets d'entrée | `CanteenMenu`, `MealReservation`,<br>`InfirmaryVisit`, `AccessPoint`, `AccessEvent`,<br>`EntryTicket` |
| Sondages / Evaluation | `Survey`, `SurveyQuestion`, `SurveyResponse` |
| Examens et notes (module not seen) | `ExamSession`, `ReportCard`,<br>`ClassDecision`, `AppreciationScale` |
| Finances (module not seen) | `FeeSchedule`, `PaymentReminder`,<br>`PaymentAllocation` |
| Paramétrage (module not seen) | `Establishment`, `Site`, `Building`, `Room`,<br>`AcademicYear`, `AcademicPeriod`, `Cycle`,<br>`Program`, `Specialization`, `StudyLevel`,<br>`SchoolClass`, `Department`, `TeachingDomain` |
| Administration (module not seen) | `UserAccount`, `StaffMember`, `RoleChangeRequest` |

---

## 18. Key constraints (to enforce in the database and services)

Every unique rule below also includes `establishmentId`, except for `UserAccount`, which is global.

**Identity and roles**
- `UserAccount.username` and `UserAccount.keycloakId` are unique.
- `Role.name` is unique.
- `(role, module)` in `RoleModuleAccess`, `(role, targetType)` in `TypePermission`, `(role, itemPath)` in `NavigationPermission` and `(role, actionKey)` in `DeniedAction` are unique.
- `(userAccount, role)` in `UserRole` is unique per establishment.
- Changing `Role.administrative`, `Role.canEditModel` or `Role.permissionPolicy` goes through a `RoleChangeRequest`, approved by a second Super Admin.

**Learners**
- `Student.matricule` is unique.
- One non-cancelled `Enrollment` per student and academic year.
- One open `ClassAssignment` (no `endDate`) per enrollment.
- `(student, guardian)` in `StudentGuardian` is unique, with at most one `primaryContact` per student.
- One `StudentCard` per student and academic year.

**Curriculum**
- `SchoolClass.code` is unique per academic year.
- `StudyPlan.code` is unique.
- `(teachingUnit, subject)` in `StudyPlanSubject` is unique.
- The `SubjectAssessment.weightPercent` values of a subject add up to **100**.
- One `TeacherWorkload` per teacher and academic year.

**Attendance**
- One `AttendanceSheet` and one `TeacherAttendance` per `sessionOccurrenceId`.
- `(sheet, studentId)` in `AttendanceRecord` is unique.
- `lateMinutes` is set only when the status is `LATE`.
- A sheet can't be edited after `editableUntil`. Only an authorized role can reopen it.

**Grades**
- `(studyPlanSubjectId, schoolClassId, academicPeriodId, assessmentType)` in `GradeGrid` is unique.
- `(grid, studentId)` in `GradeEntry` is unique.
- **Exactly one** of `score` / `reason` is set, and `0 ≤ score ≤ maxScore`.
- No change after `CONFIRMED` unless an authorized role reopens the grid.
- `Valider la note` and `Publier` should belong to a different role than grade entry (separation of duties).

**Internships**
- `Internship.referenceNumber` is unique.
- One `Defense` per internship.
- Exactly one `PRESIDENT` per jury.
- `InternshipDocument.locked = true` from `AUTHORIZED_TO_DEFEND` onward.

**Finance**
- `Payment.receiptNumber` is unique.
- The sum of a payment's `PaymentAllocation.amount` values equals `Payment.amount`.
- `Installment.paidAmount` equals the sum of its allocations.
- A `TeacherAttendance` can be paid only if it is `validated`, and only once: `teacherAttendanceId` is unique among the lines of non-cancelled `TeacherPayment`s.

**Communication and settings**
- `(userId, notificationType)` in `NotificationPreference` is unique.
- One `DoNotDisturbSetting` per user.
- `(featureKey, portal)` in `FeatureToggle` is unique.

---

## 19. Checks performed

An automated script read this file and checked the following. Final result: **0 errors, 0 warnings**.

- **Mermaid syntax:** every line of every diagram matches the expected syntax (class, attribute, annotation, relationship, cardinality). A test diagram with each kind of construct used here (annotations, self-links, composition, aggregation, inheritance, dashed arrows with labels and cardinalities) rendered correctly in Mermaid.
- **One definition per class:** every class is fully defined exactly once, in its own module. Every box drawn in another module carries that module's label.
- **Types:** every attribute type is a Java type, one of the 4 value objects, or one of the listed enumerations.
- **Enumerations:** every enum is used, none is listed twice, and enums shared between modules live in `shared`.
- **Inherited fields:** none of them is repeated in a subclass.
- **Identifier naming:** every `…Id` attribute is a `UUID`, and every `UUID` attribute ends with `Id`.
- **Module boundaries:**
  - Solid lines never cross a module boundary.
  - Every cross-module ID (`studentId`, `teacherId`, `sessionOccurrenceId`…) points to a module that the dependency graph allows.
  - The only exceptions are the documented opaque IDs (§1).
- **No cycles:** the module dependency graph is acyclic.
- **Events:** every consumer in §15 depends on the event's publisher.
- **Coverage:** every class appears in the screen coverage table (§17) or is a base or value class.

**Problems found and fixed during the checks:**
- `communication` needed direct dependencies on `scolarite` and `pedagogie`, because it listens to their events.
- `keycloakId` is now a `UUID`.
- The `Channel` enum moved from `shared` to `finances`, its only user.
- Business numbers renamed so they don't look like database IDs: `nationalIdNumber`, `taxNumber`, `actionKey`.
- 3 classes added to the coverage table.

**Size:** 12 modules · 137 classes · 738 attributes · 84 enumerations · 163 relationships.
