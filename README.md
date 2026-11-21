# Educated (Tekup)

Rebuild of the Educated school platform: student, teacher and back-office portals.
Design documents are in [`docs/`](docs/): roles and functionalities (EN/FR) and the class diagram.

| Part | Technology |
|---|---|
| Backend | Spring Boot 4.2, Java 25, Spring Modulith 2.2 (modular monolith) |
| Database | PostgreSQL 18, one schema per module, Flyway migrations |
| Sign-in | Keycloak 26.8 (OIDC, 2FA). Authorization is done by the backend's role model |
| Messaging, cache, files | RabbitMQ, Redis, SeaweedFS (S3 API) |
| Frontend | Angular 22 in an Nx 24 monorepo: `student`, `teacher`, `backoffice` apps |

```
backend/    Spring Boot application (one package per module)
frontend/   Nx workspace (apps/ and libs/core, libs/ui)
infra/      docker-compose.yml, Keycloak realm, database init
docs/       design documents
```

## Prerequisites

- **JDK 25** (Eclipse Temurin). The Maven wrapper (`mvnw`) downloads Maven itself.
- **Node.js 24** and npm.
- **Docker Desktop** (with WSL 2 on Windows) to run PostgreSQL, Keycloak and the other services.

## Run it locally

1. Start the infrastructure:

   ```bash
   docker compose -f infra/docker-compose.yml up -d
   ```

   Keycloak is on http://localhost:8081 (admin console user `admin`), RabbitMQ on http://localhost:15672,
   Mailpit (catches e-mails) on http://localhost:8025.

2. Start the backend (port 8080). On first start it creates the TEK-UP establishment, the system roles and,
   with `DEMO_DATA=true` (the default), a demo class and a demo student:

   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

   In Windows PowerShell use `.\mvnw.cmd spring-boot:run`. Open a new terminal after installing the JDK so
   `java` is on the PATH.

   API documentation: http://localhost:8080/swagger-ui.html

3. Start the portals (each in its own terminal):

   ```bash
   cd frontend
   npm install
   npx nx serve student      # http://localhost:4200
   npx nx serve teacher      # http://localhost:4201
   npx nx serve backoffice   # http://localhost:4202
   ```

4. Sign in with the local test accounts defined in `infra/keycloak/educated-realm.json`
   (`superadmin`, `scolarite.agent`, `teacher.demo`, `student.demo`). Their passwords are in that file and are
   for local development only. `superadmin` becomes Super Admin automatically on first sign-in.

## Tests

```bash
cd backend && ./mvnw test
cd frontend && npx nx run-many -t build test lint
```

The backend tests start an embedded PostgreSQL 18, so they do not need Docker. They check the database
migrations against every entity, the separation between establishments, the module boundaries, the permission
rules and the main workflows over real HTTP calls with simulated Keycloak tokens.

## Architecture rules

- Each backend module (`parametrage`, `administration`, `scolarite`, ...) owns its tables in its own schema.
  Other modules only use what it exposes in its `api` package. `ModularityTest` fails if a module reaches into
  another module's internals or if module dependencies form a cycle.
- Links between modules are stored as `UUID` ids, never as database foreign keys across schemas.
- Modules react to each other through domain events (for example `StudentEnrolled` creates the fee plan,
  `MakeUpConfirmed` schedules the make-up session, and most events become notifications). Listeners run after the
  publishing transaction commits, in their own transaction.
- Every business table carries `establishment_id`. Hibernate fills it and filters on it automatically, so one
  establishment never sees another's data.
- Keycloak only signs users in. What a user may do is decided by the backend's roles (module visibility, default
  policy, per-entity rights, menus, denied actions) and checked on every endpoint with `@PreAuthorize`. Portal
  endpoints (`/api/student/...`, `/api/teacher/...`, `/api/me/...`) only ever return the signed-in person's data.
- Nobody can change their own roles, and giving admin rights needs the approval of a second Super Admin.

## Modules

| Module | What it does |
|---|---|
| `parametrage` | Establishments, academic years and semesters, levels, classes, rooms, feature switches |
| `administration` | Accounts, roles (6-layer model), role change approvals, preferences |
| `scolarite` | Learners, guardians, enrollments, sanctions, document requests |
| `pedagogie` | Subjects, study plans, teachers, assignments, course materials, course journal, homework, make-up requests |
| `emploidutemps` | Weekly timetables with clash checks, dated sessions, make-up sessions, cancellations |
| `assiduite` | Calls (editable 48 h after confirming), absence rates per subject, justification requests, teacher hours |
| `stages` | Internship declaration and approval, defense requests, juries, defenses, grades and mentions |
| `examens` | Exam calendar with room and invigilator checks, grade grids, publication, report cards and ranks |
| `finances` | Fees per level, fee plans split into installments, payments with receipts, reminders, teacher payments |
| `vieetablissement` | Surveys (anonymous answers keep no author), canteen menus and reservations, infirmary, entry tickets, access control |
| `communication` | Notifications from the other modules' events, messages to an audience or a class, news, discussions |

The demo data (`educated.bootstrap.demo-data`, on by default) creates one class, one student (`student.demo`), one
teacher (`teacher.demo`) who teaches two subjects of that class, and a published first-semester timetable.

## Status

Every module above is built, with its back-office screens and the matching student and teacher portal screens.
Tests: 37 backend tests (on embedded PostgreSQL) and 38 frontend tests.

Not done yet:

- Push, SMS and e-mail sending: notifications are stored and shown in the apps, but no message leaves the server.
- Report card PDFs, the attention points of the dashboard, and skipping public holidays when a timetable creates
  its sessions.
- Answers to anonymous surveys cannot be limited to one per person, because nothing links an answer to its author.
- The whole stack has not been run end to end in a browser yet: that needs Docker for PostgreSQL and Keycloak.
