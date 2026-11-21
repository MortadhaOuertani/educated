import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService, SessionService, addDays, errorMessage, isoDate } from '@edu/core';
import { AUDIENCES } from './communication.page';

interface Menu {
  id: string;
  menuDate: string;
  mealType: string;
  items: string | null;
  price: number | null;
  reserved: number;
}

interface Survey {
  id: string;
  title: string;
  audience: string;
  anonymous: boolean;
  responseCount: number;
  questions: { id: string; text: string }[];
}

/** Vie de l'établissement: canteen menus of the coming week and surveys. */
@Component({
  selector: 'edu-campus-page',
  imports: [DatePipe, FormsModule],
  template: `
    <h1>Vie de l'établissement</h1>
    @if (error()) {
      <p class="edu-error">{{ error() }}</p>
    }
    <h2>Cantine · 7 prochains jours</h2>
    <table class="data">
      <tbody>
        @for (m of menus(); track m.id) {
          <tr>
            <td>{{ m.menuDate | date: 'EEEE dd/MM' }}</td>
            <td>{{ m.mealType }}</td>
            <td>{{ m.items }}</td>
            <td>{{ m.reserved }} réservation(s)</td>
          </tr>
        } @empty {
          <tr><td class="edu-muted">Aucun menu.</td></tr>
        }
      </tbody>
    </table>
    @if (canWriteMenus) {
      <form class="card" (ngSubmit)="createMenu()">
        <h3>Ajouter un menu</h3>
        <div class="form">
          <div class="field">
            <label for="menu-date">Date</label>
            <input id="menu-date" name="menuDate" type="date" [(ngModel)]="menuDate" required />
          </div>
          <div class="field">
            <label for="meal">Repas</label>
            <select id="meal" name="meal" [(ngModel)]="mealType">
              <option value="BREAKFAST">Petit-déjeuner</option>
              <option value="LUNCH">Déjeuner</option>
              <option value="DINNER">Dîner</option>
            </select>
          </div>
          <div class="field">
            <label for="items">Plats</label>
            <input id="items" name="items" [(ngModel)]="items" />
          </div>
          <button class="btn" type="submit" [disabled]="!menuDate">Ajouter</button>
        </div>
      </form>
    }

    @if (canSurveys) {
      <h2>Sondages</h2>
      <table class="data">
        <tbody>
          @for (s of surveys(); track s.id) {
            <tr>
              <td>{{ s.title }}</td>
              <td>{{ s.audience }}{{ s.anonymous ? ' · anonyme' : '' }}</td>
              <td>{{ s.responseCount }} réponse(s)</td>
            </tr>
          } @empty {
            <tr><td class="edu-muted">Aucun sondage.</td></tr>
          }
        </tbody>
      </table>
      <form class="card" (ngSubmit)="createSurvey()">
        <h3>Nouveau sondage</h3>
        <div class="form">
          <div class="field">
            <label for="survey-title">Titre</label>
            <input id="survey-title" name="surveyTitle" [(ngModel)]="surveyTitle" required />
          </div>
          <div class="field">
            <label for="question">Question (note de 1 à 5)</label>
            <input id="question" name="question" [(ngModel)]="question" required />
          </div>
          <div class="field">
            <label for="survey-audience">Pour</label>
            <select id="survey-audience" name="surveyAudience" [(ngModel)]="surveyAudience">
              @for (a of audiences; track a.value) {
                <option [value]="a.value">{{ a.label }}</option>
              }
            </select>
          </div>
          <label><input type="checkbox" name="anonymous" [(ngModel)]="anonymous" /> Anonyme</label>
          <button class="btn" type="submit" [disabled]="!surveyTitle || !question">Créer</button>
        </div>
      </form>
    }
  `,
})
export class CampusPage {
  private readonly api = inject(ApiService);
  private readonly session = inject(SessionService);

  protected readonly audiences = AUDIENCES;
  protected readonly canWriteMenus = this.session.can('CanteenMenu', 'CREATE');
  protected readonly canSurveys = this.session.can('Survey', 'READ');
  protected readonly menus = signal<Menu[]>([]);
  protected readonly surveys = signal<Survey[]>([]);
  protected readonly error = signal<string | null>(null);

  protected menuDate = '';
  protected mealType = 'LUNCH';
  protected items = '';
  protected surveyTitle = '';
  protected question = '';
  protected surveyAudience = 'STUDENTS';
  protected anonymous = true;

  constructor() {
    this.loadMenus();
    if (this.canSurveys) {
      this.loadSurveys();
    }
  }

  protected createMenu(): void {
    this.api.post('/vie-etablissement/canteen-menus', { menuDate: this.menuDate, mealType: this.mealType, items: this.items })
      .subscribe({
        next: () => {
          this.items = '';
          this.loadMenus();
        },
        error: (e) => this.error.set(errorMessage(e)),
      });
  }

  protected createSurvey(): void {
    const survey = {
      title: this.surveyTitle,
      audience: this.surveyAudience,
      anonymous: this.anonymous,
      questions: [{ text: this.question, questionType: 'RATING', required: true }],
    };
    this.api.post('/vie-etablissement/surveys', survey).subscribe({
      next: () => {
        this.surveyTitle = '';
        this.question = '';
        this.loadSurveys();
      },
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  private loadMenus(): void {
    const today = new Date();
    this.api.get<Menu[]>('/vie-etablissement/canteen-menus', { from: isoDate(today), to: isoDate(addDays(today, 7)) })
      .subscribe({
        next: (list) => this.menus.set(list),
        error: (e) => this.error.set(errorMessage(e)),
      });
  }

  private loadSurveys(): void {
    this.api.get<Survey[]>('/vie-etablissement/surveys').subscribe({
      next: (list) => this.surveys.set(list),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
