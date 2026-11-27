import type { ControlType, DayOfWeek, DebtStatus, GradeAttempt, LessonType, StudentStatus, WeekType } from '../api/types';

export const DAYS: DayOfWeek[] = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];

export const DAY_TITLES: Record<DayOfWeek, string> = {
  MONDAY: 'Понеділок',
  TUESDAY: 'Вівторок',
  WEDNESDAY: 'Середа',
  THURSDAY: 'Четвер',
  FRIDAY: "П'ятниця",
  SATURDAY: 'Субота',
  SUNDAY: 'Неділя',
};

/** Розклад дзвінків (пари 1–8). */
export const PAIR_TIMES: Record<number, string> = {
  1: '08:30–09:50',
  2: '10:05–11:25',
  3: '11:40–13:00',
  4: '13:15–14:35',
  5: '14:50–16:10',
  6: '16:25–17:45',
  7: '18:00–19:20',
  8: '19:30–20:50',
};

export const WEEK_TITLES: Record<WeekType, string> = {
  ALL: 'щотижня',
  ODD: 'чисельник',
  EVEN: 'знаменник',
};

export const LESSON_TITLES: Record<LessonType, string> = {
  LECTURE: 'Лекція',
  PRACTICE: 'Практика',
  LAB: 'Лабораторна',
};

export const CONTROL_TITLES: Record<ControlType, string> = {
  EXAM: 'Іспит',
  CREDIT: 'Залік',
  DIFF_CREDIT: 'Диф. залік',
};

export const ATTEMPT_TITLES: Record<GradeAttempt, string> = {
  REGULAR: 'Основна',
  RETAKE_TEACHER: 'Перескладання',
  RETAKE_COMMISSION: 'Комісія',
};

export const STATUS_TITLES: Record<StudentStatus, string> = {
  ACTIVE: 'Навчається',
  ACADEMIC_LEAVE: 'Академвідпустка',
  EXPELLED: 'Відрахований',
  GRADUATED: 'Випускник',
};

export const DEBT_TITLES: Record<DebtStatus, string> = {
  OPEN: 'Відкрита',
  CLOSED: 'Закрита',
};

export function formatDate(value: string | null | undefined): string {
  if (!value) return '—';
  const d = new Date(value);
  return Number.isNaN(d.getTime()) ? value : d.toLocaleDateString('uk-UA');
}

export function formatDateTime(value: string | null | undefined): string {
  if (!value) return '—';
  const d = new Date(value);
  return Number.isNaN(d.getTime())
    ? value
    : d.toLocaleString('uk-UA', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

export function formatNumber(value: unknown, digits = 1): string {
  if (value === null || value === undefined || value === '') return '—';
  const n = typeof value === 'number' ? value : Number(value);
  if (Number.isNaN(n)) return String(value);
  return Number.isInteger(n) ? String(n) : n.toFixed(digits);
}

/** CSS-клас для оцінки ЄКТС. */
export function ectsClass(ects: string | null | undefined): string {
  switch (ects) {
    case 'A':
      return 'ects ects-a';
    case 'B':
    case 'C':
      return 'ects ects-bc';
    case 'D':
    case 'E':
      return 'ects ects-de';
    case 'FX':
    case 'F':
      return 'ects ects-f';
    default:
      return 'ects';
  }
}

/** Ініціали для аватарки. */
export function initials(fullName: string): string {
  return fullName
    .split(' ')
    .slice(0, 2)
    .map((p) => p.charAt(0))
    .join('')
    .toUpperCase();
}
