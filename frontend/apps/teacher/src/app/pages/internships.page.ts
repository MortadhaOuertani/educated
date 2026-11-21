import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ApiService, errorMessage, shortTime } from '@edu/core';

interface Internship {
  id: string;
  title: string;
  studentName: string | null;
  matricule: string | null;
  companyName: string;
  status: string;
  startDate: string;
  endDate: string;
}

interface Jury {
  id: string;
  code: string;
  juryDate: string;
  members: { name: string | null; role: string }[];
  defenses: { id: string; studentName: string | null; startTime: string; endTime: string; grade: number | null }[];
}

/** Stages encadrés: authorize the defense of the students you supervise, see your juries. */
@Component({
  selector: 'edu-internships-page',
  imports: [DatePipe],
  template: `
    <h1>Stages encadrés</h1>
    @if (error()) {
      <p class="edu-error">{{ error() }}</p>
    }
    <table class="data">
      <thead><tr><th>Apprenant</th><th>Sujet</th><th>Entreprise</th><th>Statut</th><th></th></tr></thead>
      <tbody>
        @for (i of internships(); track i.id) {
          <tr>
            <td>{{ i.studentName }} <span class="edu-muted">{{ i.matricule }}</span></td>
            <td>{{ i.title }}</td>
            <td>{{ i.companyName }}</td>
            <td>{{ i.status }}</td>
            <td>
              @if (i.status === 'DEFENSE_REQUESTED') {
                <button type="button" class="btn" (click)="authorize(i)">Autoriser la soutenance</button>
              }
            </td>
          </tr>
        } @empty {
          <tr><td class="edu-muted">Aucun stage encadré.</td></tr>
        }
      </tbody>
    </table>

    <h2>Mes jurys</h2>
    @for (j of juries(); track j.id) {
      <div class="card">
        <strong>{{ j.code }}</strong> · {{ j.juryDate | date: 'dd/MM/yyyy' }}
        <div class="edu-muted">
          @for (m of j.members; track $index) {
            {{ m.name }} ({{ m.role }}){{ $last ? '' : ', ' }}
          }
        </div>
        <ul>
          @for (d of j.defenses; track d.id) {
            <li>{{ time(d.startTime) }} – {{ time(d.endTime) }} : {{ d.studentName }}</li>
          }
        </ul>
      </div>
    } @empty {
      <p class="edu-muted">Aucun jury.</p>
    }
  `,
})
export class InternshipsPage {
  private readonly api = inject(ApiService);

  protected readonly time = shortTime;
  protected readonly internships = signal<Internship[]>([]);
  protected readonly juries = signal<Jury[]>([]);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.load();
    this.api.get<Jury[]>('/teacher/juries').subscribe({
      next: (juries) => this.juries.set(juries),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected authorize(internship: Internship): void {
    this.api.post(`/teacher/internships/${internship.id}/authorize-defense`, {}).subscribe({
      next: () => this.load(),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  private load(): void {
    this.api.get<Internship[]>('/teacher/internships').subscribe({
      next: (list) => this.internships.set(list),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
