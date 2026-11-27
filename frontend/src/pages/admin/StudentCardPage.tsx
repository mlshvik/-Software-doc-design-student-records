import { api } from '../../api/client';
import type { StudentRecord } from '../../api/types';
import { Badge, Card, Ects, ErrorBox, Loader, PageHeader, StatCard } from '../../components/ui';
import { Link } from '../../router';
import { ATTEMPT_TITLES, DEBT_TITLES, formatDate, formatNumber } from '../../utils/format';
import { useApi } from '../../utils/useApi';

/** Картка студента для адміністратора (той самий фасад, що й у кабінеті студента). */
export function StudentCardPage({ studentId }: { studentId: number }) {
  const { data, error, loading, reload } = useApi(() => api.get<StudentRecord>(`/students/${studentId}/record`), [studentId]);
  if (loading && !data) return <Loader />;
  if (error || !data) return <ErrorBox error={error ?? 'Немає даних'} onRetry={reload} />;
  const { student, gpa } = data;
  return (
    <>
      <PageHeader
        title={student.fullName}
        subtitle={`${student.groupName} · ${student.course} курс · ${student.specialty} · ${student.faculty} · залікова № ${student.recordBookNumber}`}
        actions={<Link to="/admin/students" className="btn">← До списку</Link>}
      />
      <div className="stats">
        <StatCard label="Середній бал" value={formatNumber(gpa.value, 2)} hint={`${gpa.subjectsCounted} дисциплін · ${formatNumber(gpa.totalCredits)} кредитів`} />
        <StatCard label="ЄКТС" value={<Ects value={gpa.ects} />} />
        <StatCard label="Відкриті борги" value={data.openDebts} tone={data.openDebts ? 'bad' : 'good'} />
        <StatCard label="Статус" value={student.statusTitle} />
      </div>
      <Card title="Оцінки">
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Семестр</th>
                <th>Дисципліна</th>
                <th>Викладач</th>
                <th>Спроба</th>
                <th className="num">Бал</th>
                <th>ЄКТС</th>
                <th>Дата</th>
              </tr>
            </thead>
            <tbody>
              {data.grades.map((g) => (
                <tr key={g.id} className={g.finalResult ? '' : 'row-muted'}>
                  <td>{g.semester} ({g.academicYear})</td>
                  <td>{g.subject}</td>
                  <td>{g.teacher}</td>
                  <td>{ATTEMPT_TITLES[g.attempt]}</td>
                  <td className="num"><strong>{g.score}</strong></td>
                  <td><Ects value={g.ects} /></td>
                  <td>{formatDate(g.gradedAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>
      {data.debts.length > 0 && (
        <Card title="Заборгованості">
          <ul className="list">
            {data.debts.map((d) => (
              <li key={d.id}>
                <Badge tone={d.status === 'OPEN' ? 'bad' : 'good'}>{DEBT_TITLES[d.status]}</Badge>
                <div className="grow">
                  <strong>{d.subject}</strong>
                  <div className="muted small">{d.teacher} · відкрито {formatDate(d.openedAt)}{d.closedAt ? ` · закрито ${formatDate(d.closedAt)}` : ''}</div>
                </div>
              </li>
            ))}
          </ul>
        </Card>
      )}
    </>
  );
}
