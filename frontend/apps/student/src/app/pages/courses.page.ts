import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ApiService, LocalizedText, errorMessage } from '@edu/core';

interface Subject {
  id: string;
  subjectCode: string;
  subjectName: LocalizedText;
  sessionType: string;
  plannedHours: number | null;
  teacherName: string | null;
}

interface Material {
  id: string;
  subjectName: LocalizedText;
  title: string;
  materialType: string;
  description: string | null;
  publishedAt: string | null;
}

interface Homework {
  id: string;
  subjectName: LocalizedText;
  title: string;
  description: string | null;
  dueDate: string | null;
}

/** Mes cours: subjects of the class, course materials to download and homework. */
@Component({
  selector: 'edu-courses-page',
  imports: [DatePipe],
  template: `
    <h1>Mes cours</h1>
    @if (error()) {
      <p class="edu-error">{{ error() }}</p>
    }
    <h2>Matières</h2>
    <table class="data">
      <thead><tr><th>Matière</th><th>Type</th><th>Heures</th><th>Enseignant</th></tr></thead>
      <tbody>
        @for (s of subjects(); track s.id) {
          <tr>
            <td>{{ s.subjectName.fr }} <span class="edu-muted">{{ s.subjectCode }}</span></td>
            <td>{{ s.sessionType }}</td>
            <td>{{ s.plannedHours }}</td>
            <td>{{ s.teacherName ?? '—' }}</td>
          </tr>
        }
      </tbody>
    </table>

    <h2>Supports de cours</h2>
    @if (materials().length === 0) {
      <p class="edu-muted">Aucun support publié.</p>
    }
    <table class="data">
      <tbody>
        @for (m of materials(); track m.id) {
          <tr>
            <td><strong>{{ m.title }}</strong><div class="edu-muted">{{ m.subjectName.fr }} · {{ m.materialType }}</div></td>
            <td>{{ m.publishedAt | date: 'dd/MM/yyyy' }}</td>
            <td><button type="button" class="btn btn-secondary" (click)="download(m)">Télécharger</button></td>
          </tr>
        }
      </tbody>
    </table>

    <h2>Devoirs</h2>
    @if (homework().length === 0) {
      <p class="edu-muted">Aucun devoir.</p>
    }
    <table class="data">
      <tbody>
        @for (h of homework(); track h.id) {
          <tr>
            <td><strong>{{ h.title }}</strong><div class="edu-muted">{{ h.subjectName.fr }}</div><div>{{ h.description }}</div></td>
            <td>Pour le {{ h.dueDate | date: 'dd/MM/yyyy' }}</td>
          </tr>
        }
      </tbody>
    </table>
  `,
})
export class CoursesPage {
  private readonly api = inject(ApiService);

  protected readonly subjects = signal<Subject[]>([]);
  protected readonly materials = signal<Material[]>([]);
  protected readonly homework = signal<Homework[]>([]);
  protected readonly error = signal<string | null>(null);

  constructor() {
    const fail = (e: unknown) => this.error.set(errorMessage(e));
    this.api.get<Subject[]>('/student/subjects').subscribe({ next: (s) => this.subjects.set(s), error: fail });
    this.api.get<Material[]>('/student/materials').subscribe({ next: (m) => this.materials.set(m), error: fail });
    this.api.get<Homework[]>('/student/homework').subscribe({ next: (h) => this.homework.set(h), error: fail });
  }

  /** Asks for a short-lived download link, then opens it. */
  protected download(material: Material): void {
    this.api.get<{ url: string }>(`/student/materials/${material.id}/link`).subscribe({
      next: (link) => window.open(link.url, '_blank'),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
