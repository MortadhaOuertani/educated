import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ApiService, errorMessage } from '@edu/core';

interface NotificationItem {
  id: string;
  type: string;
  title: string;
  body: string | null;
  createdAt: string;
  readAt: string | null;
}

interface InboxItem {
  id: string;
  subject: string;
  body: string | null;
  sentAt: string;
  readAt: string | null;
}

interface NewsItem {
  id: string;
  title: string;
  body: string | null;
  publishedAt: string;
}

/** Notifications, received messages and news, the same in the three portals. */
@Component({
  selector: 'edu-notifications-page',
  imports: [DatePipe],
  template: `
    <h1>Notifications</h1>
    @if (error()) {
      <p class="edu-error">{{ error() }}</p>
    }
    <div class="actions">
      <button type="button" class="btn btn-secondary" (click)="markAllRead()" [disabled]="unread() === 0">
        Tout marquer comme lu
      </button>
    </div>
    @if (notifications().length === 0) {
      <p class="edu-muted">Aucune notification.</p>
    } @else {
      <table class="data">
        <tbody>
          @for (n of notifications(); track n.id) {
            <tr [class.unread]="!n.readAt">
              <td>
                <strong>{{ n.title }}</strong>
                <div>{{ n.body }}</div>
              </td>
              <td class="edu-muted">{{ n.createdAt | date: 'dd/MM/yyyy HH:mm' }}</td>
              <td>
                @if (!n.readAt) {
                  <button type="button" class="btn btn-secondary" (click)="markRead(n)">Lu</button>
                }
              </td>
            </tr>
          }
        </tbody>
      </table>
    }

    <h2>Messages reçus</h2>
    @if (messages().length === 0) {
      <p class="edu-muted">Aucun message.</p>
    }
    @for (m of messages(); track m.id) {
      <div class="card">
        <strong>{{ m.subject }}</strong>
        <span class="edu-muted"> · {{ m.sentAt | date: 'dd/MM/yyyy HH:mm' }}</span>
        <p>{{ m.body }}</p>
        @if (!m.readAt) {
          <button type="button" class="btn btn-secondary" (click)="markMessageRead(m)">Marquer comme lu</button>
        }
      </div>
    }

    <h2>Actualités</h2>
    @if (news().length === 0) {
      <p class="edu-muted">Aucune actualité.</p>
    }
    @for (item of news(); track item.id) {
      <div class="card">
        <strong>{{ item.title }}</strong>
        <span class="edu-muted"> · {{ item.publishedAt | date: 'dd/MM/yyyy' }}</span>
        <p>{{ item.body }}</p>
      </div>
    }
  `,
})
export class NotificationsPage {
  private readonly api = inject(ApiService);

  protected readonly notifications = signal<NotificationItem[]>([]);
  protected readonly messages = signal<InboxItem[]>([]);
  protected readonly news = signal<NewsItem[]>([]);
  protected readonly unread = signal(0);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.load();
    this.api.get<InboxItem[]>('/me/messages').subscribe({
      next: (messages) => this.messages.set(messages),
      error: (e) => this.error.set(errorMessage(e)),
    });
    this.api.get<NewsItem[]>('/me/news').subscribe({
      next: (news) => this.news.set(news),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected markRead(item: NotificationItem): void {
    this.api.post(`/me/notifications/${item.id}/read`, {}).subscribe({
      next: () => this.load(),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected markAllRead(): void {
    this.api.post('/me/notifications/read-all', {}).subscribe({
      next: () => this.load(),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected markMessageRead(message: InboxItem): void {
    this.api.post<InboxItem>(`/me/messages/${message.id}/read`, {}).subscribe({
      next: (updated) => this.messages.update((list) => list.map((m) => (m.id === updated.id ? updated : m))),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  private load(): void {
    this.api.get<NotificationItem[]>('/me/notifications').subscribe({
      next: (list) => {
        this.notifications.set(list);
        this.unread.set(list.filter((n) => !n.readAt).length);
      },
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
