import { useState } from 'react';
import { ApiError, api } from '../../api/client';
import type { GradeSheet, GradeSheetRow } from '../../api/types';
import { Badge, Card, Ects, ErrorBox, Loader, PageHeader } from '../../components/ui';
import { Link } from '../../router';
import { ATTEMPT_TITLES, CONTROL_TITLES, STATUS_TITLES } from '../../utils/format';
import { useApi } from '../../utils/useApi';

/**
 * Відомість групи з дисципліни. Для кожного студента показано всі спроби і поле
 * для наступної дозволеної спроби (основна → перескладання → комісія).
 * Перевірки виконує бекенд (Chain of Responsibility), а після збереження оцінки
 * автоматично оновлюються борги та сповіщення (Observer).
 */
export function GradeSheetPage({ assignmentId }: { assignmentId: number }) {
  const { data, error, loading, reload } = useApi(
    () => api.get<GradeSheet>(`/assignments/${assignmentId}/sheet`),
    [assignmentId],
  );
  const [scores, setScores] = useState<Record<number, string>>({});
  const [rowErrors, setRowErrors] = useState<Record<number, string>>({});
  const [saving, setSaving] = useState<number | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const save = async (row: GradeSheetRow) => {
    const raw = scores[row.studentId];
    const score = Number(raw);
    if (raw === undefined || raw === '' || !Number.isInteger(score) || score < 0 || score > 100) {
      setRowErrors((e) => ({ ...e, [row.studentId]: 'Введіть ціле число від 0 до 100' }));
      return;
    }
    setSaving(row.studentId);
    setRowErrors((e) => ({ ...e, [row.studentId]: '' }));
    try {
      await api.post('/grades', { studentId: row.studentId, assignmentId, score, attempt: row.nextAttempt });
      setScores((s) => ({ ...s, [row.studentId]: '' }));
      setMessage(`Оцінку ${score} для ${row.studentName} збережено`);
      reload();
    } catch (err) {
      setRowErrors((e) => ({ ...e, [row.studentId]: err instanceof ApiError ? err.message : 'Не вдалося зберегти' }));
    } finally {
      setSaving(null);
    }
  };

  if (loading && !data) return <Loader />;
  if (error || !data) return <ErrorBox error={error ?? 'Немає даних'} onRetry={reload} />;

  const { assignment, rows } = data;
  const graded = rows.filter((r) => r.finalScore !== null).length;
  const failed = rows.filter((r) => r.finalScore !== null && r.finalScore < 60).length;

  return (
    <>
      <PageHeader
        title={assignment.subjectName}
        subtitle={`Група ${assignment.groupName} · ${assignment.semester} семестр ${assignment.academicYear} · ${CONTROL_TITLES[assignment.controlType]}`}
        actions={<Link to="/teacher" className="btn">← До дисциплін</Link>}
      />
      <div className="stats">
        <div className="stat">
          <div className="stat-label">Студентів</div>
          <div className="stat-value">{rows.length}</div>
        </div>
        <div className="stat">
          <div className="stat-label">Оцінено</div>
          <div className="stat-value">{graded}</div>
        </div>
        <div className={`stat ${failed ? 'stat-bad' : ''}`}>
          <div className="stat-label">Незадовільно</div>
          <div className="stat-value">{failed}</div>
        </div>
      </div>
      {message && <div className="alert alert-success">{message}</div>}
      <Card title="Відомість">
        <div className="table-wrap">
          <table className="table sheet">
            <thead>
              <tr>
                <th>#</th>
                <th>Студент</th>
                <th>Залікова</th>
                <th>Спроби</th>
                <th className="num">Підсумок</th>
                <th>ЄКТС</th>
                <th>Нова оцінка</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((row, i) => (
                <tr key={row.studentId} className={row.status !== 'ACTIVE' ? 'row-muted' : ''}>
                  <td>{i + 1}</td>
                  <td>
                    {row.studentName}
                    {row.status !== 'ACTIVE' && <Badge tone="warn">{STATUS_TITLES[row.status]}</Badge>}
                  </td>
                  <td>{row.recordBookNumber}</td>
                  <td>
                    {row.grades.length === 0
                      ? <span className="muted">—</span>
                      : row.grades.map((g) => (
                          <span key={g.id} className={`attempt ${g.passed ? 'ok' : 'fail'}`} title={`${ATTEMPT_TITLES[g.attempt]}: ${g.national}`}>
                            {g.score}
                          </span>
                        ))}
                  </td>
                  <td className="num">
                    <strong>{row.finalScore ?? '—'}</strong>
                  </td>
                  <td>{row.finalEcts ? <Ects value={row.finalEcts} /> : '—'}</td>
                  <td>
                    {row.canBeGraded && row.nextAttempt ? (
                      <div className="grade-input">
                        <input
                          type="number"
                          min={0}
                          max={100}
                          placeholder="0–100"
                          value={scores[row.studentId] ?? ''}
                          onChange={(e) => setScores((s) => ({ ...s, [row.studentId]: e.target.value }))}
                          onKeyDown={(e) => e.key === 'Enter' && save(row)}
                        />
                        <button type="button" className="btn btn-small btn-primary" disabled={saving === row.studentId} onClick={() => save(row)}>
                          {saving === row.studentId ? '…' : 'Зберегти'}
                        </button>
                        <span className="muted small">{ATTEMPT_TITLES[row.nextAttempt]}</span>
                        {rowErrors[row.studentId] && <div className="field-error">{rowErrors[row.studentId]}</div>}
                      </div>
                    ) : (
                      <span className="muted small">
                        {!row.canBeGraded ? 'оцінювання недоступне' : row.finalScore !== null && row.finalScore >= 60 ? 'складено' : 'спроби вичерпано'}
                      </span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>
    </>
  );
}
