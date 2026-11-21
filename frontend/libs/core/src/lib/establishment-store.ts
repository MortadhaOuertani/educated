import { Injectable, signal } from '@angular/core';

const STORAGE_KEY = 'educated.establishmentId';

/** The establishment chosen in the header. Kept apart from the session so the HTTP interceptor can read it. */
@Injectable({ providedIn: 'root' })
export class EstablishmentStore {
  readonly selected = signal<string | null>(readStored());

  select(id: string | null): void {
    this.selected.set(id);
    try {
      if (id) {
        localStorage.setItem(STORAGE_KEY, id);
      } else {
        localStorage.removeItem(STORAGE_KEY);
      }
    } catch {
      // Storage can be unavailable (private mode); the choice then only lasts for this page.
    }
  }
}

function readStored(): string | null {
  try {
    return localStorage.getItem(STORAGE_KEY);
  } catch {
    return null;
  }
}
