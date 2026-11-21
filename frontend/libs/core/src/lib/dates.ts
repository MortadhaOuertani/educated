/** Small date helpers for week views. Dates travel as 'yyyy-MM-dd' strings, like the backend's LocalDate. */

/** 'yyyy-MM-dd' of a date in local time. */
export function isoDate(date: Date): string {
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${date.getFullYear()}-${month}-${day}`;
}

export function addDays(date: Date, days: number): Date {
  const copy = new Date(date);
  copy.setDate(copy.getDate() + days);
  return copy;
}

/** The Monday of the date's week (weeks start on Monday in Tunisia). */
export function mondayOf(date: Date): Date {
  const day = date.getDay(); // 0 = Sunday
  const monday = addDays(date, day === 0 ? -6 : 1 - day);
  monday.setHours(0, 0, 0, 0);
  return monday;
}

/** Groups items by their 'date' field, keeping the order of first appearance. */
export function groupByDate<T extends { date: string }>(items: T[]): { date: string; items: T[] }[] {
  const groups: { date: string; items: T[] }[] = [];
  for (const item of items) {
    const group = groups.find((g) => g.date === item.date);
    if (group) {
      group.items.push(item);
    } else {
      groups.push({ date: item.date, items: [item] });
    }
  }
  return groups;
}

/** 'HH:mm' from the backend's 'HH:mm:ss'. */
export function shortTime(time: string | null | undefined): string {
  return time ? time.substring(0, 5) : '';
}
