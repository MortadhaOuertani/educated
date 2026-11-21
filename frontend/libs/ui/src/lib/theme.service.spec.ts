import { TestBed } from '@angular/core/testing';
import { ThemeService } from './theme.service';

describe('ThemeService', () => {
  beforeEach(() => localStorage.clear());

  it('toggles between light and dark and remembers the choice', () => {
    const theme = TestBed.inject(ThemeService);
    theme.set('light');

    theme.toggle();

    expect(theme.isDark()).toBe(true);
    expect(document.documentElement.dataset['theme']).toBe('dark');
    expect(localStorage.getItem('educated.theme')).toBe('dark');
  });
});
