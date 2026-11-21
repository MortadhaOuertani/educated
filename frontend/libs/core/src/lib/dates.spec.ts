import { addDays, groupByDate, isoDate, mondayOf, shortTime } from './dates';

describe('dates', () => {
  it('formats a local date as yyyy-MM-dd', () => {
    expect(isoDate(new Date(2026, 10, 5))).toBe('2026-11-05');
  });

  it('finds the Monday of the week, also from a Sunday', () => {
    expect(isoDate(mondayOf(new Date(2026, 10, 18)))).toBe('2026-11-16'); // Wednesday
    expect(isoDate(mondayOf(new Date(2026, 10, 22)))).toBe('2026-11-16'); // Sunday
    expect(isoDate(mondayOf(new Date(2026, 10, 16)))).toBe('2026-11-16'); // Monday itself
  });

  it('adds days across months', () => {
    expect(isoDate(addDays(new Date(2026, 10, 30), 2))).toBe('2026-12-02');
  });

  it('groups items by date in order', () => {
    const groups = groupByDate([
      { date: '2026-11-16', name: 'a' },
      { date: '2026-11-18', name: 'b' },
      { date: '2026-11-16', name: 'c' },
    ]);
    expect(groups.map((g) => g.date)).toEqual(['2026-11-16', '2026-11-18']);
    expect(groups[0].items.map((i) => i.name)).toEqual(['a', 'c']);
  });

  it('shortens backend times', () => {
    expect(shortTime('08:30:00')).toBe('08:30');
    expect(shortTime(null)).toBe('');
  });
});
