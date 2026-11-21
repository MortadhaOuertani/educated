import { Injectable, inject, signal } from '@angular/core';
import { AcademicYear, ApiService, SchoolClass, errorMessage } from '@edu/core';

/** The current academic year, its semesters and its classes, loaded once for the screens that need them. */
@Injectable({ providedIn: 'root' })
export class CurrentYear {
  private readonly api = inject(ApiService);

  readonly year = signal<AcademicYear | null>(null);
  readonly classes = signal<SchoolClass[]>([]);
  readonly error = signal<string | null>(null);

  load(): void {
    if (this.year()) {
      return;
    }
    this.api.get<AcademicYear[]>('/parametrage/academic-years').subscribe({
      next: (years) => {
        const year = years.find((y) => y.current) ?? years[0] ?? null;
        this.year.set(year);
        if (year) {
          this.api.get<SchoolClass[]>('/parametrage/school-classes', { academicYearId: year.id }).subscribe({
            next: (classes) => this.classes.set(classes),
            error: (e) => this.error.set(errorMessage(e)),
          });
        }
      },
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
