import { useMemo } from 'react';
import type { RecordGrade } from '../../api/types';
import { Badge, Card, Ects, EmptyState, ErrorBox, Loader, PageHeader } from '../../components/ui';
import { ATTEMPT_TITLES, CONTROL_TITLES, formatDate, formatNumber } from '../../utils/format';
import { useStudentRecord } from './useStudentRecord';

export function StudentGrades() {
  const { data, error, loading, reload } = useStudentRecord();

  // Групуємо оцінки за семестрами (новіші семестри – першими).
  const bySemester = useMemo(() => {
    const map = new Map<string, RecordGrade[]>();
    (data?.grades ?? []).forEach((g) => {
      const key = `${g.semester} семестр (${g.academicYear})`;
      map.set(key, [...(map.get(key) ?? []), g]);
    });
    return [...map.entries()].sort((a, b) => b[1][0].semester - a[1][0].semester);
  }, [data]);

  if (loading) return <Loader />;
  if (error || !data) return <ErrorBox error={error ?? 'Немає даних'} onRetry={reload} />;

  return (
    <>
      <PageHeader
        title="Мої оцінки"
        subtitle={`Середній бал: ${formatNumber(data.gpa.value, 2)} (${data.gpa.ects ?? '—'}) · кредитів ЄКТС: ${formatNumber(data.gpa.totalCredits)}`}
      />
      {bySemester.length === 0 && (
        <Card>
          <EmptyState>Оцінок поки немає.</EmptyState>
        </Card>
      )}
      {bySemester.map(([semester, grades]) => {
        const finals = grades.filter((g) => g.finalResult);
        const avg = finals.length ? finals.reduce((s, g) => s + g.score, 0) / finals.length : 0;
        return (
          <Card key={semester} title={semester} actions={<span className="muted">середній бал: {formatNumber(avg, 1)}</span>}>
            <div className="table-wrap">
              <table className="table">
                <thead>
                  <tr>
                    <th>Дисципліна</th>
                    <th>Контроль</th>
                    <th className="num">Кредити</th>
                    <th>Викладач</th>
                    <th>Спроба</th>
                    <th className="num">Бал</th>
                    <th>Оцінка</th>
                    <th>ЄКТС</th>
                    <th>Дата</th>
                  </tr>
                </thead>
                <tbody>
                  {grades.map((g) => (
                    <tr key={g.id} className={g.finalResult ? '' : 'row-muted'}>
                      <td>
                        {g.subject} {!g.finalResult && <Badge>не підсумкова</Badge>}
                      </td>
                      <td>{CONTROL_TITLES[g.controlType]}</td>
                      <td className="num">{formatNumber(g.credits)}</td>
                      <td>{g.teacher}</td>
                      <td>{ATTEMPT_TITLES[g.attempt]}</td>
                      <td className="num">
                        <strong>{g.score}</strong>
                      </td>
                      <td>{g.national}</td>
                      <td>
                        <Ects value={g.ects} />
                      </td>
                      <td>{formatDate(g.gradedAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </Card>
        );
      })}
    </>
  );
}
