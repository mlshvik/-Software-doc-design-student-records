import { Card, Ects, ErrorBox, Loader, PageHeader, StatCard } from '../../components/ui';
import { Link } from '../../router';
import { DAYS, DAY_TITLES, LESSON_TITLES, PAIR_TIMES, formatDate, formatNumber } from '../../utils/format';
import { useStudentRecord } from './useStudentRecord';

export function StudentHome() {
  const { data, error, loading, reload } = useStudentRecord();
  if (loading) return <Loader />;
  if (error || !data) return <ErrorBox error={error ?? 'Немає даних'} onRetry={reload} />;

  const { student, gpa } = data;
  const today = DAYS[(new Date().getDay() + 6) % 7];
  const todayLessons = data.schedule
    .filter((l) => l.dayOfWeek === today)
    .sort((a, b) => a.pairNumber - b.pairNumber);
  const latest = [...data.grades].sort((a, b) => b.gradedAt.localeCompare(a.gradedAt)).slice(0, 5);
  const gpaValue = Number(gpa.value);

  return (
    <>
      <PageHeader title={`Вітаємо, ${student.fullName.split(' ')[1] ?? student.fullName}!`} subtitle={`${student.groupName} · ${student.course} курс · ${student.specialty}`} />

      <div className="stats">
        <StatCard
          label="Середній бал"
          value={formatNumber(gpa.value, 2)}
          hint={`${gpa.strategy === 'CREDIT_WEIGHTED' ? 'зважений на кредити' : 'середнє арифметичне'} · ${gpa.subjectsCounted} дисциплін`}
          tone={gpaValue >= 82 ? 'good' : gpaValue >= 60 ? undefined : 'bad'}
        />
        <StatCard label="Оцінка ЄКТС" value={<Ects value={gpa.ects} />} hint="за середнім балом" />
        <StatCard
          label="Заборгованості"
          value={data.openDebts}
          hint={data.openDebts ? 'потрібно перескласти' : 'боргів немає'}
          tone={data.openDebts ? 'bad' : 'good'}
        />
        <StatCard label="Статус" value={student.statusTitle} hint={`залікова № ${student.recordBookNumber}`} />
      </div>

      <div className="grid-2">
        <Card title={`Сьогодні, ${DAY_TITLES[today].toLowerCase()}`} actions={<Link to="/student/schedule" className="link">Увесь розклад →</Link>}>
          {todayLessons.length === 0 ? (
            <p className="muted">Сьогодні занять немає.</p>
          ) : (
            <ul className="list">
              {todayLessons.map((l) => (
                <li key={l.id}>
                  <span className="pair-badge">{l.pairNumber}</span>
                  <div>
                    <strong>{l.subject}</strong>
                    <div className="muted small">
                      {PAIR_TIMES[l.pairNumber]} · {LESSON_TITLES[l.lessonType]} · ауд. {l.classroom} · {l.teacher}
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </Card>

        <Card title="Останні оцінки" actions={<Link to="/student/grades" className="link">Усі оцінки →</Link>}>
          {latest.length === 0 ? (
            <p className="muted">Оцінок ще немає.</p>
          ) : (
            <ul className="list">
              {latest.map((g) => (
                <li key={g.id}>
                  <Ects value={g.ects} />
                  <div className="grow">
                    <strong>{g.subject}</strong>
                    <div className="muted small">
                      {g.score} балів · {g.national} · {formatDate(g.gradedAt)}
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </Card>
      </div>
    </>
  );
}
