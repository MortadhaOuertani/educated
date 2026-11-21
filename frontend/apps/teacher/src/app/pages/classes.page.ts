import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService, LocalizedText, addDays, errorMessage, isoDate } from '@edu/core';

export interface Assignment {
  id: string;
  subjectCode: string;
  subjectName: LocalizedText;
  sessionType: string;
  plannedHours: number | null;
  schoolClassId: string;
}

interface Material {
  id: string;
  title: string;
  materialType: string;
  visibleToStudents: boolean;
  createdAt: string;
}

interface JournalEntry {
  id: string;
  sessionOccurrenceId: string;
  content: string;
  confirmedAt: string;
}

interface Homework {
  id: string;
  title: string;
  assignedDate: string;
  dueDate: string | null;
}

interface Session {
  id: string;
  date: string;
  teachingAssignmentId: string;
}

const MATERIAL_TYPES = ['COURSE', 'TUTORIAL', 'LAB', 'PROJECT', 'QUIZ', 'SUPERVISED_TEST', 'EXAM'];

/** Mes classes: for each subject taught, course materials, the course journal and homework. */
@Component({
  selector: 'edu-classes-page',
  imports: [DatePipe, FormsModule],
  template: `
    <h1>Mes classes</h1>
    @if (error()) {
      <p class="edu-error">{{ error() }}</p>
    }
    <table class="data">
      <thead><tr><th>Matière</th><th>Type</th><th>Heures</th><th></th></tr></thead>
      <tbody>
        @for (a of assignments(); track a.id) {
          <tr [class.unread]="selected()?.id === a.id">
            <td>{{ a.subjectName.fr }} <span class="edu-muted">{{ a.subjectCode }}</span></td>
            <td>{{ a.sessionType }}</td>
            <td>{{ a.plannedHours }}</td>
            <td><button type="button" class="btn btn-secondary" (click)="select(a)">Ouvrir</button></td>
          </tr>
        }
      </tbody>
    </table>

    @if (selected(); as a) {
      <h2>{{ a.subjectName.fr }}</h2>

      <form class="card" (ngSubmit)="upload(a)">
        <h3>Déposer un support</h3>
        <div class="form">
          <div class="field">
            <label for="title">Titre</label>
            <input id="title" name="title" [(ngModel)]="materialTitle" required />
          </div>
          <div class="field">
            <label for="type">Type</label>
            <select id="type" name="type" [(ngModel)]="materialType">
              @for (t of materialTypes; track t) {
                <option [value]="t">{{ t }}</option>
              }
            </select>
          </div>
          <div class="field">
            <label for="file">Fichier (50 Mo maximum)</label>
            <input id="file" type="file" (change)="pickFile($event)" />
          </div>
          <button class="btn" type="submit" [disabled]="!materialTitle || !file">Déposer</button>
        </div>
      </form>
      <table class="data">
        <tbody>
          @for (m of materials(); track m.id) {
            <tr><td>{{ m.title }}</td><td>{{ m.materialType }}</td><td>{{ m.createdAt | date: 'dd/MM/yyyy' }}</td></tr>
          }
        </tbody>
      </table>

      <form class="card" (ngSubmit)="writeJournal(a)">
        <h3>Cahier de texte</h3>
        <div class="form">
          <div class="field">
            <label for="session">Séance</label>
            <select id="session" name="session" [(ngModel)]="journalSessionId" required>
              <option value="">Choisir une séance…</option>
              @for (s of recentSessions(); track s.id) {
                <option [value]="s.id">{{ s.date | date: 'EEEE dd/MM' }}</option>
              }
            </select>
          </div>
          <div class="field">
            <label for="content">Contenu de la séance</label>
            <textarea id="content" name="content" rows="3" [(ngModel)]="journalContent" required></textarea>
          </div>
          <button class="btn" type="submit" [disabled]="!journalSessionId || !journalContent">Enregistrer</button>
        </div>
      </form>
      <table class="data">
        <tbody>
          @for (j of journal(); track j.id) {
            <tr><td>{{ j.confirmedAt | date: 'dd/MM/yyyy' }}</td><td>{{ j.content }}</td></tr>
          }
        </tbody>
      </table>

      <form class="card" (ngSubmit)="giveHomework(a)">
        <h3>Devoir</h3>
        <div class="form">
          <div class="field">
            <label for="hw-title">Titre</label>
            <input id="hw-title" name="hwTitle" [(ngModel)]="homeworkTitle" required />
          </div>
          <div class="field">
            <label for="hw-due">Pour le</label>
            <input id="hw-due" name="hwDue" type="date" [(ngModel)]="homeworkDue" />
          </div>
          <button class="btn" type="submit" [disabled]="!homeworkTitle">Donner le devoir</button>
        </div>
      </form>
      <table class="data">
        <tbody>
          @for (h of homework(); track h.id) {
            <tr><td>{{ h.title }}</td><td>pour le {{ h.dueDate | date: 'dd/MM/yyyy' }}</td></tr>
          }
        </tbody>
      </table>
    }
  `,
})
export class ClassesPage {
  private readonly api = inject(ApiService);

  protected readonly materialTypes = MATERIAL_TYPES;
  protected readonly assignments = signal<Assignment[]>([]);
  protected readonly selected = signal<Assignment | null>(null);
  protected readonly materials = signal<Material[]>([]);
  protected readonly journal = signal<JournalEntry[]>([]);
  protected readonly homework = signal<Homework[]>([]);
  protected readonly recentSessions = signal<Session[]>([]);
  protected readonly error = signal<string | null>(null);

  protected materialTitle = '';
  protected materialType = 'COURSE';
  protected file: File | null = null;
  protected journalSessionId = '';
  protected journalContent = '';
  protected homeworkTitle = '';
  protected homeworkDue = '';

  constructor() {
    this.api.get<Assignment[]>('/teacher/assignments').subscribe({
      next: (list) => this.assignments.set(list),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected select(assignment: Assignment): void {
    this.selected.set(assignment);
    this.loadDetails(assignment);
    const today = new Date();
    this.api
      .get<Session[]>('/teacher/sessions', { from: isoDate(addDays(today, -14)), to: isoDate(today) })
      .subscribe({
        next: (sessions) => this.recentSessions.set(sessions.filter((s) => s.teachingAssignmentId === assignment.id)),
        error: (e) => this.error.set(errorMessage(e)),
      });
  }

  protected pickFile(event: Event): void {
    this.file = (event.target as HTMLInputElement).files?.[0] ?? null;
  }

  protected upload(assignment: Assignment): void {
    if (!this.file) {
      return;
    }
    const form = new FormData();
    form.append('title', this.materialTitle);
    form.append('materialType', this.materialType);
    form.append('file', this.file);
    this.api.postForm(`/teacher/assignments/${assignment.id}/materials`, form).subscribe({
      next: () => {
        this.materialTitle = '';
        this.file = null;
        this.loadDetails(assignment);
      },
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected writeJournal(assignment: Assignment): void {
    const body = { sessionOccurrenceId: this.journalSessionId, content: this.journalContent };
    this.api.post(`/teacher/assignments/${assignment.id}/journal`, body).subscribe({
      next: () => {
        this.journalSessionId = '';
        this.journalContent = '';
        this.loadDetails(assignment);
      },
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected giveHomework(assignment: Assignment): void {
    const body = { title: this.homeworkTitle, assignedDate: isoDate(new Date()), dueDate: this.homeworkDue || null };
    this.api.post(`/teacher/assignments/${assignment.id}/homework`, body).subscribe({
      next: () => {
        this.homeworkTitle = '';
        this.homeworkDue = '';
        this.loadDetails(assignment);
      },
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  private loadDetails(assignment: Assignment): void {
    const fail = (e: unknown) => this.error.set(errorMessage(e));
    this.api.get<Material[]>(`/teacher/assignments/${assignment.id}/materials`).subscribe({
      next: (list) => this.materials.set(list),
      error: fail,
    });
    this.api.get<JournalEntry[]>(`/teacher/assignments/${assignment.id}/journal`).subscribe({
      next: (list) => this.journal.set(list),
      error: fail,
    });
    this.api.get<Homework[]>(`/teacher/assignments/${assignment.id}/homework`).subscribe({
      next: (list) => this.homework.set(list),
      error: fail,
    });
  }
}
