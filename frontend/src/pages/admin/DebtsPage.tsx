import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import type { Debt, Group } from '../../api/types';
import { Badge, Card, EmptyState, ErrorBox, Loader, PageHeader } from '../../components/ui';
import { DEBT_TITLES, formatDate } from '../../utils/format';
import { useApi } from '../../utils/useApi';

export function DebtsPage() {
  const [status, setStatus] = useState('OPEN');
  const [groupId, setGroupId] = useState('');
  const [groups, setGroups] = useState<Group[]>([]);
  const { data, error, loading, reload } = useApi(() => api.get<Debt[]>('/debts', { status, groupId }), [status, groupId]);

  useEffect(() => {
    api.get<Group[]>('/groups').then(setGroups).catch(() => setGroups([]));
  }, []);

  return (
    <>
      <PageHeader
        title="Академічні заборгованості"
        subtitle="Відкриваються і закриваються автоматично після виставлення оцінок (шаблон Observer)"
      />
      <Card>
        <div className="filters">
          <select value={status} onChange={(e) => setStatus(e.target.value)}>
            <option value="OPEN">Відкриті</option>
            <option value="CLOSED">Закриті</option>
            <option value="">Усі</option>
          </select>
          <select value={groupId} onChange={(e) => setGroupId(e.target.value)}>
            <option value="">Усі групи</option>
            {groups.map((g) => <option key={g.id} value={g.id}>{g.name}</option>)}
          </select>
          {data && <span className="muted small">Знайдено: {data.length}</span>}
        </div>
        {loading && !data && <Loader />}
        {error && <ErrorBox error={error} onRetry={reload} />}
        {data && data.length === 0 && <EmptyState>Заборгованостей немає</EmptyState>}
        {data && data.length > 0 && (
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>Студент</th>
                  <th>Група</th>
                  <th>Дисципліна</th>
                  <th>Викладач</th>
                  <th>Семестр</th>
                  <th>Відкрито</th>
                  <th>Закрито</th>
                  <th>Стан</th>
                </tr>
              </thead>
              <tbody>
                {data.map((d) => (
                  <tr key={d.id}>
                    <td>{d.studentName}</td>
                    <td><span className="group-tag">{d.groupName}</span></td>
                    <td>{d.subjectName}</td>
                    <td>{d.teacherName}</td>
                    <td>{d.semester} ({d.academicYear})</td>
                    <td>{formatDate(d.openedAt)}</td>
                    <td>{formatDate(d.closedAt)}</td>
                    <td><Badge tone={d.status === 'OPEN' ? 'bad' : 'good'}>{DEBT_TITLES[d.status]}</Badge></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </>
  );
}
