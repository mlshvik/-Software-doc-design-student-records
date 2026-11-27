import type { DayOfWeek, LessonType, WeekType } from '../api/types';
import { DAYS, DAY_TITLES, LESSON_TITLES, PAIR_TIMES, WEEK_TITLES } from '../utils/format';

export interface GridLesson {
  id: number;
  dayOfWeek: DayOfWeek;
  pairNumber: number;
  weekType: WeekType;
  lessonType: LessonType;
  subject: string;
  classroom: string;
  /** Додатковий рядок: викладач або група. */
  extra?: string;
}

const WEEK_ORDER: Record<WeekType, number> = { ALL: 0, ODD: 1, EVEN: 2 };

/**
 * Сітка розкладу «дні тижня × пари». Заняття за чисельником і знаменником
 * у тій самій клітинці показуються одне під одним.
 */
export function ScheduleGrid({ lessons, onDelete }: { lessons: GridLesson[]; onDelete?: (id: number) => void }) {
  const usedDays = DAYS.filter((d) => d !== 'SATURDAY' || lessons.some((l) => l.dayOfWeek === 'SATURDAY'));
  const maxPair = Math.max(4, ...lessons.map((l) => l.pairNumber));
  const pairs = Array.from({ length: maxPair }, (_, i) => i + 1);
  return (
    <div className="schedule-wrap">
      <table className="schedule">
        <thead>
          <tr>
            <th className="pair-col">Пара</th>
            {usedDays.map((d) => (
              <th key={d}>{DAY_TITLES[d]}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {pairs.map((p) => (
            <tr key={p}>
              <td className="pair-col">
                <strong>{p}</strong>
                <div className="muted small">{PAIR_TIMES[p]}</div>
              </td>
              {usedDays.map((d) => {
                const cell = lessons
                  .filter((l) => l.dayOfWeek === d && l.pairNumber === p)
                  .sort((a, b) => WEEK_ORDER[a.weekType] - WEEK_ORDER[b.weekType]);
                return (
                  <td key={d} className={cell.length ? 'has-lesson' : ''}>
                    {cell.map((l) => (
                      <div key={l.id} className={`lesson lesson-${l.lessonType.toLowerCase()}`}>
                        <div className="lesson-subject">{l.subject}</div>
                        <div className="lesson-meta">
                          {LESSON_TITLES[l.lessonType]} · ауд. {l.classroom}
                          {l.weekType !== 'ALL' && <span className="week-tag">{WEEK_TITLES[l.weekType]}</span>}
                        </div>
                        {l.extra && <div className="lesson-meta">{l.extra}</div>}
                        {onDelete && (
                          <button type="button" className="lesson-delete" title="Видалити заняття" onClick={() => onDelete(l.id)}>
                            ×
                          </button>
                        )}
                      </div>
                    ))}
                  </td>
                );
              })}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
