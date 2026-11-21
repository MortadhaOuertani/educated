import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService, SessionService, errorMessage } from '@edu/core';
import { CurrentYear } from '../current-year';

interface SentMessage {
  id: string;
  subject: string;
  audience: string;
  sentAt: string;
  recipients: number;
}

interface News {
  id: string;
  title: string;
  audience: string;
  status: string;
  publishedAt: string | null;
}

export const AUDIENCES: { value: string; label: string }[] = [
  { value: 'STUDENTS', label: 'Apprenants' },
  { value: 'TEACHERS', label: 'Enseignants' },
  { value: 'STAFF', label: 'Personnel' },
  { value: 'ALL', label: 'Tout le monde' },
];

/** Communication: send a message to an audience (one class if needed) and publish news. */
@Component({
  selector: 'edu-communication-page',
  imports: [DatePipe, FormsModule],
  template: `
    <h1>Communication</h1>
    @if (error()) {
      <p class="edu-error">{{ error() }}</p>
    }
    @if (canSend) {
      <form class="card" (ngSubmit)="send()">
        <h2>Nouveau message</h2>
        <div class="form">
          <div class="field">
            <label for="audience">Destinataires</label>
            <select id="audience" name="audience" [(ngModel)]="audience">
              @for (a of audiences; track a.value) {
                <option [value]="a.value">{{ a.label }}</option>
              }
            </select>
          </div>
          @if (audience === 'STUDENTS') {
            <div class="field">
              <label for="class">Classe (facultatif)</label>
              <select id="class" name="class" [(ngModel)]="classId">
                <option value="">Toutes les classes</option>
                @for (c of year.classes(); track c.id) {
                  <option [value]="c.id">{{ c.code }}</option>
                }
              </select>
            </div>
          }
          <div class="field">
            <label for="subject">Objet</label>
            <input id="subject" name="subject" [(ngModel)]="subject" required />
          </div>
          <div class="field">
            <label for="body">Message</label>
            <textarea id="body" name="body" rows="3" [(ngModel)]="body"></textarea>
          </div>
          <button class="btn" type="submit" [disabled]="!subject">Envoyer</button>
        </div>
      </form>
    }
    <h2>Messages envoyés</h2>
    <table class="data">
      <tbody>
        @for (m of sent(); track m.id) {
          <tr>
            <td>{{ m.subject }}</td>
            <td>{{ m.audience }}</td>
            <td>{{ m.recipients }} destinataire(s)</td>
            <td>{{ m.sentAt | date: 'dd/MM/yyyy HH:mm' }}</td>
          </tr>
        } @empty {
          <tr><td class="edu-muted">Aucun message envoyé.</td></tr>
        }
      </tbody>
    </table>

    @if (canNews) {
      <form class="card" (ngSubmit)="createNews()">
        <h2>Nouvelle actualité</h2>
        <div class="form">
          <div class="field">
            <label for="news-title">Titre</label>
            <input id="news-title" name="newsTitle" [(ngModel)]="newsTitle" required />
          </div>
          <div class="field">
            <label for="news-audience">Pour</label>
            <select id="news-audience" name="newsAudience" [(ngModel)]="newsAudience">
              @for (a of audiences; track a.value) {
                <option [value]="a.value">{{ a.label }}</option>
              }
            </select>
          </div>
          <button class="btn" type="submit" [disabled]="!newsTitle">Créer le brouillon</button>
        </div>
      </form>
      <table class="data">
        <tbody>
          @for (n of news(); track n.id) {
            <tr>
              <td>{{ n.title }}</td>
              <td>{{ n.audience }}</td>
              <td>{{ n.status }}</td>
              <td>
                @if (n.status === 'DRAFT') {
                  <button type="button" class="btn" (click)="publish(n)">Publier</button>
                }
              </td>
            </tr>
          }
        </tbody>
      </table>
    }
  `,
})
export class CommunicationPage {
  private readonly api = inject(ApiService);
  private readonly session = inject(SessionService);

  protected readonly year = inject(CurrentYear);
  protected readonly audiences = AUDIENCES;
  protected readonly canSend = this.session.can('Message', 'CREATE');
  protected readonly canNews = this.session.can('News', 'READ');
  protected readonly sent = signal<SentMessage[]>([]);
  protected readonly news = signal<News[]>([]);
  protected readonly error = signal<string | null>(null);

  protected audience = 'STUDENTS';
  protected classId = '';
  protected subject = '';
  protected body = '';
  protected newsTitle = '';
  protected newsAudience = 'ALL';

  constructor() {
    this.year.load();
    this.loadSent();
    if (this.canNews) {
      this.loadNews();
    }
  }

  protected send(): void {
    const message = {
      subject: this.subject,
      body: this.body,
      audience: this.audience,
      schoolClassId: this.audience === 'STUDENTS' && this.classId ? this.classId : null,
      category: 'SCOLARITE',
    };
    this.api.post('/communication/messages', message).subscribe({
      next: () => {
        this.subject = '';
        this.body = '';
        this.error.set(null);
        this.loadSent();
      },
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected createNews(): void {
    this.api.post('/communication/news', { title: this.newsTitle, audience: this.newsAudience }).subscribe({
      next: () => {
        this.newsTitle = '';
        this.loadNews();
      },
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected publish(item: News): void {
    this.api.post(`/communication/news/${item.id}/publish`, {}).subscribe({
      next: () => this.loadNews(),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  private loadSent(): void {
    this.api.get<SentMessage[]>('/communication/messages').subscribe({
      next: (list) => this.sent.set(list),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  private loadNews(): void {
    this.api.get<News[]>('/communication/news').subscribe({
      next: (list) => this.news.set(list),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
