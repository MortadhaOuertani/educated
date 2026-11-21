import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  AcademicYear,
  ApiService,
  DOCUMENT_STATUS,
  DocumentRequest,
  Enrollment,
  GuardianLink,
  LocalizedText,
  STUDENT_STATUS,
  SchoolClass,
  SessionService,
  Student,
  errorMessage,
} from '@edu/core';

interface Kinship {
  code: string;
  label: LocalizedText;
}

@Component({
  selector: 'edu-student-detail-page',
  imports: [FormsModule, RouterLink, DatePipe],
  template: `
    <p><a routerLink="/apprenants">← Apprenants</a></p>
    @if (error()) {
      <p class="edu-error">{{ error() }}</p>
    }
    @if (student(); as s) {
      <h1>{{ s.person.lastName?.toUpperCase() }} {{ s.person.firstName }}</h1>
      <div class="card">
        <p>Matricule : {{ s.matricule }} · Statut : {{ statuses[s.status] }}</p>
        @if (s.person.email) {
          <p>E-mail : {{ s.person.email }}</p>
        }
      </div>

      <h2>Responsables</h2>
      @if (guardians().length === 0) {
        <p class="edu-muted">Aucun responsable enregistré.</p>
      }
      @for (g of guardians(); track g.guardianId) {
        <div class="card">
          <strong>{{ g.person.lastName }} {{ g.person.firstName }}</strong> — {{ g.kinship.fr }}
          @if (g.primaryContact) {
            <span class="badge badge-info">Contact principal</span>
          }
          @if (g.person.mobile) {
            <div class="edu-muted">{{ g.person.mobile }}</div>
          }
        </div>
      }
      @if (session.can('Guardian', 'CREATE')) {
        <form class="card" (ngSubmit)="addGuardian()">
          <div class="form">
            <div class="field"><label for="gLast">Nom</label><input id="gLast" name="gLast" [(ngModel)]="guardian.lastName" required /></div>
            <div class="field"><label for="gFirst">Prénom</label><input id="gFirst" name="gFirst" [(ngModel)]="guardian.firstName" required /></div>
            <div class="field"><label for="gMobile">GSM</label><input id="gMobile" name="gMobile" [(ngModel)]="guardian.mobile" /></div>
            <div class="field">
              <label for="gKinship">Lien de parenté</label>
              <select id="gKinship" name="gKinship" [(ngModel)]="guardian.kinshipCode" required>
                @for (k of kinships(); track k.code) {
                  <option [value]="k.code">{{ k.label.fr }}</option>
                }
              </select>
            </div>
            <label><input type="checkbox" name="gPrimary" [(ngModel)]="guardian.primaryContact" /> Contact principal</label>
            <button class="btn" type="submit" [disabled]="!guardian.lastName || !guardian.firstName || !guardian.kinshipCode">Ajouter</button>
          </div>
        </form>
      }

      <h2>Inscriptions</h2>
      @for (e of enrollments(); track e.id) {
        <div class="card">
          Inscrit le {{ e.enrollmentDate | date: 'dd/MM/yyyy' }} · {{ e.status }}
          @for (a of e.assignments; track a.startDate) {
            <div class="edu-muted">
              Classe {{ classCode(a.schoolClassId) }} depuis le {{ a.startDate | date: 'dd/MM/yyyy' }}
              @if (a.endDate) {
                jusqu'au {{ a.endDate | date: 'dd/MM/yyyy' }}
              }
            </div>
          }
        </div>
      }
      @if (session.can('Enrollment', 'CREATE') && classes().length > 0) {
        <form class="card" (ngSubmit)="enroll()">
          <div class="form">
            <div class="field">
              <label for="class">Classe ({{ currentYear()?.code }})</label>
              <select id="class" name="class" [(ngModel)]="enrollment.schoolClassId" required>
                @for (c of classes(); track c.id) {
                  <option [value]="c.id">{{ c.code }}</option>
                }
              </select>
            </div>
            <div class="field">
              <label for="type">Type</label>
              <select id="type" name="type" [(ngModel)]="enrollment.enrollmentType">
                <option value="NEW">Nouvelle inscription</option>
                <option value="RE_ENROLLMENT">Réinscription</option>
                <option value="TRANSFER">Transfert</option>
              </select>
            </div>
            <div class="field"><label for="date">Date</label><input id="date" name="date" type="date" [(ngModel)]="enrollment.enrollmentDate" required /></div>
            <button class="btn" type="submit" [disabled]="!enrollment.schoolClassId || !enrollment.enrollmentDate">Inscrire</button>
          </div>
        </form>
      }

      @if (session.can('DocumentRequest', 'READ')) {
        <h2>Demandes de documents</h2>
        @for (r of requests(); track r.id) {
          <div class="card">
            {{ r.documentType.label.fr }} · <span [class]="'badge ' + documentStatuses[r.status].badge">{{ documentStatuses[r.status].label }}</span>
          </div>
        } @empty {
          <p class="edu-muted">Aucune demande.</p>
        }
      }
      @if (actionError()) {
        <p class="edu-error">{{ actionError() }}</p>
      }
    }
  `,
})
export class StudentDetailPage implements OnInit {
  /** Route parameter (component input binding). */
  readonly id = input.required<string>();

  protected readonly session = inject(SessionService);
  private readonly api = inject(ApiService);

  protected readonly statuses = STUDENT_STATUS;
  protected readonly documentStatuses = DOCUMENT_STATUS;
  protected readonly student = signal<Student | null>(null);
  protected readonly guardians = signal<GuardianLink[]>([]);
  protected readonly enrollments = signal<Enrollment[]>([]);
  protected readonly requests = signal<DocumentRequest[]>([]);
  protected readonly kinships = signal<Kinship[]>([]);
  protected readonly currentYear = signal<AcademicYear | null>(null);
  protected readonly classes = signal<SchoolClass[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly actionError = signal<string | null>(null);

  protected guardian = { lastName: '', firstName: '', mobile: '', kinshipCode: '', primaryContact: false };
  protected enrollment = { schoolClassId: '', enrollmentType: 'NEW', enrollmentDate: new Date().toISOString().slice(0, 10) };

  ngOnInit(): void {
    this.api.get<Student>(`/scolarite/students/${this.id()}`).subscribe({
      next: (student) => this.student.set(student),
      error: (e) => this.error.set(errorMessage(e)),
    });
    this.loadGuardians();
    this.loadEnrollments();
    if (this.session.can('DocumentRequest', 'READ')) {
      this.api.get<DocumentRequest[]>(`/scolarite/students/${this.id()}/document-requests`).subscribe({
        next: (requests) => this.requests.set(requests),
      });
    }
    if (this.session.can('Kinship', 'READ')) {
      this.api.get<Kinship[]>('/scolarite/kinships').subscribe({ next: (kinships) => this.kinships.set(kinships) });
    }
    if (this.session.can('AcademicYear', 'READ') && this.session.can('SchoolClass', 'READ')) {
      this.api.get<AcademicYear[]>('/parametrage/academic-years').subscribe({
        next: (years) => {
          const current = years.find((y) => y.current) ?? null;
          this.currentYear.set(current);
          if (current) {
            this.api
              .get<SchoolClass[]>('/parametrage/school-classes', { academicYearId: current.id })
              .subscribe({ next: (classes) => this.classes.set(classes) });
          }
        },
      });
    }
  }

  protected classCode(id: string): string {
    return this.classes().find((c) => c.id === id)?.code ?? '—';
  }

  protected addGuardian(): void {
    const g = this.guardian;
    const body = {
      person: { lastName: g.lastName.trim(), firstName: g.firstName.trim(), mobile: g.mobile || null },
      kinshipCode: g.kinshipCode,
      primaryContact: g.primaryContact,
      smsOptIn: !!g.mobile,
    };
    this.api.post(`/scolarite/students/${this.id()}/guardians`, body).subscribe({
      next: () => {
        this.guardian = { lastName: '', firstName: '', mobile: '', kinshipCode: '', primaryContact: false };
        this.actionError.set(null);
        this.loadGuardians();
      },
      error: (e) => this.actionError.set(errorMessage(e)),
    });
  }

  protected enroll(): void {
    this.api.post(`/scolarite/students/${this.id()}/enrollments`, this.enrollment).subscribe({
      next: () => {
        this.actionError.set(null);
        this.loadEnrollments();
      },
      error: (e) => this.actionError.set(errorMessage(e)),
    });
  }

  private loadGuardians(): void {
    if (this.session.can('StudentGuardian', 'READ')) {
      this.api.get<GuardianLink[]>(`/scolarite/students/${this.id()}/guardians`).subscribe({
        next: (guardians) => this.guardians.set(guardians),
      });
    }
  }

  private loadEnrollments(): void {
    if (this.session.can('Enrollment', 'READ')) {
      this.api.get<Enrollment[]>(`/scolarite/students/${this.id()}/enrollments`).subscribe({
        next: (enrollments) => this.enrollments.set(enrollments),
      });
    }
  }
}
