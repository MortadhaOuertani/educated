import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService, PageResponse, SessionService, Student, errorMessage } from '@edu/core';

interface Installment {
  id: string;
  number: number;
  dueDate: string;
  amount: number;
  paidAmount: number;
  matricule: string | null;
  studentName: string | null;
}

interface TeacherPayment {
  id: string;
  teacherName: string | null;
  periodFrom: string;
  periodTo: string;
  totalHours: number;
  totalAmount: number;
  status: 'PENDING' | 'PAID' | 'CANCELLED';
}

/** Amount still due on an installment. */
export function remainingAmount(installment: Pick<Installment, 'amount' | 'paidAmount'>): number {
  return Math.round((installment.amount - installment.paidAmount) * 1000) / 1000;
}

/** Finances: overdue installments and reminders, recording a payment, paying teachers. */
@Component({
  selector: 'edu-finances-page',
  imports: [DatePipe, DecimalPipe, FormsModule],
  template: `
    <h1>Finances</h1>
    @if (error()) {
      <p class="edu-error">{{ error() }}</p>
    }
    @if (message()) {
      <p class="edu-success">{{ message() }}</p>
    }

    @if (canRecordPayment) {
      <form class="card" (ngSubmit)="findStudent()">
        <h2>Encaisser un paiement</h2>
        <div class="form">
          <div class="field">
            <label for="matricule">Matricule</label>
            <input id="matricule" name="matricule" [(ngModel)]="matricule" required />
          </div>
          <button class="btn btn-secondary" type="submit" [disabled]="!matricule">Rechercher</button>
        </div>
        @if (student(); as s) {
          <p>{{ s.person.lastName }} {{ s.person.firstName }} ({{ s.matricule }})</p>
          <div class="form">
            <div class="field">
              <label for="amount">Montant (TND)</label>
              <input id="amount" name="amount" type="number" step="0.001" min="0" [(ngModel)]="amount" />
            </div>
            <div class="field">
              <label for="method">Mode</label>
              <select id="method" name="method" [(ngModel)]="method">
                <option value="CASH">Espèces</option>
                <option value="CHEQUE">Chèque</option>
                <option value="BANK_TRANSFER">Virement</option>
                <option value="CARD">Carte</option>
              </select>
            </div>
            <button class="btn" type="button" [disabled]="!amount" (click)="pay(s)">Encaisser</button>
          </div>
        }
      </form>
    }

    <h2>Échéances en retard</h2>
    <table class="data">
      <thead><tr><th>Apprenant</th><th>Échéance</th><th>Reste à payer</th><th></th></tr></thead>
      <tbody>
        @for (i of overdue(); track i.id) {
          <tr>
            <td>{{ i.studentName }} <span class="edu-muted">{{ i.matricule }}</span></td>
            <td>n° {{ i.number }} du {{ i.dueDate | date: 'dd/MM/yyyy' }}</td>
            <td>{{ remaining(i) | number: '1.3-3' }} TND</td>
            <td><button type="button" class="btn btn-secondary" (click)="remind(i)">Relancer</button></td>
          </tr>
        } @empty {
          <tr><td class="edu-muted">Aucune échéance en retard.</td></tr>
        }
      </tbody>
    </table>

    <h2>Paiement des enseignants</h2>
    <table class="data">
      <tbody>
        @for (p of teacherPayments(); track p.id) {
          <tr>
            <td>{{ p.teacherName }}</td>
            <td>{{ p.periodFrom | date: 'dd/MM' }} → {{ p.periodTo | date: 'dd/MM/yyyy' }}</td>
            <td>{{ p.totalHours }} h · {{ p.totalAmount | number: '1.3-3' }} TND</td>
            <td>{{ p.status }}</td>
            <td>
              @if (p.status === 'PENDING') {
                <button type="button" class="btn" (click)="payTeacher(p)">Payer</button>
              }
              @if (p.status !== 'CANCELLED') {
                <button type="button" class="btn btn-danger" (click)="cancelTeacherPayment(p)">Annuler</button>
              }
            </td>
          </tr>
        } @empty {
          <tr><td class="edu-muted">Aucun paiement préparé.</td></tr>
        }
      </tbody>
    </table>
  `,
})
export class FinancesPage {
  private readonly api = inject(ApiService);

  protected readonly canRecordPayment = inject(SessionService).can('Payment', 'CREATE');
  protected readonly remaining = remainingAmount;
  protected readonly overdue = signal<Installment[]>([]);
  protected readonly teacherPayments = signal<TeacherPayment[]>([]);
  protected readonly student = signal<Student | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly message = signal<string | null>(null);

  protected matricule = '';
  protected amount: number | null = null;
  protected method = 'CASH';

  constructor() {
    this.loadOverdue();
    this.loadTeacherPayments();
  }

  protected findStudent(): void {
    this.api.get<PageResponse<Student>>('/scolarite/students', { search: this.matricule.trim() }).subscribe({
      next: (page) => {
        const found = page.content.find((s) => s.matricule === this.matricule.trim()) ?? null;
        this.student.set(found);
        this.error.set(found ? null : 'Aucun apprenant avec ce matricule.');
      },
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected pay(student: Student): void {
    const body = { studentId: student.id, amount: this.amount, method: this.method };
    this.api.post<{ receiptNumber: string }>('/finances/payments', body).subscribe({
      next: (payment) => {
        this.message.set(`Paiement enregistré, reçu ${payment.receiptNumber}.`);
        this.amount = null;
        this.loadOverdue();
      },
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected remind(installment: Installment): void {
    this.api.post(`/finances/installments/${installment.id}/reminders`, { channel: 'SMS' }).subscribe({
      next: () => this.message.set('Relance envoyée.'),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected payTeacher(payment: TeacherPayment): void {
    this.teacherAction(`/finances/teacher-payments/${payment.id}/pay`);
  }

  protected cancelTeacherPayment(payment: TeacherPayment): void {
    this.teacherAction(`/finances/teacher-payments/${payment.id}/cancel`);
  }

  private teacherAction(path: string): void {
    this.api.post(path, {}).subscribe({
      next: () => this.loadTeacherPayments(),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  private loadOverdue(): void {
    this.api.get<Installment[]>('/finances/installments/overdue').subscribe({
      next: (list) => this.overdue.set(list),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  private loadTeacherPayments(): void {
    this.api.get<TeacherPayment[]>('/finances/teacher-payments').subscribe({
      next: (list) => this.teacherPayments.set(list),
      error: () => this.teacherPayments.set([]),
    });
  }
}
