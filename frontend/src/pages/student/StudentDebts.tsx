import { Badge, Card, EmptyState, ErrorBox, Loader, PageHeader } from '../../components/ui';
import { DEBT_TITLES, formatDate } from '../../utils/format';
import { useStudentRecord } from './useStudentRecord';

export function StudentDebts() {
  const { data, error, loading, reload } = useStudentRecord();
  if (loading) return <Loader />;
  if (error || !data) return <ErrorBox error={error ?? 'Немає даних'} onRetry={reload} />;

  const open = data.debts.filter((d) => d.status === 'OPEN');
  return (
    <>
      <PageHeader
        title="Академічні заборгованості"
        subtitle={open.length ? `Відкритих: ${open.length}. Перескладання – спершу викладачу, потім комісії.` : 'Відкритих заборгованостей немає'}
      />
      <Card>
        {data.debts.length === 0 ? (
          <EmptyState>Заборгованостей не було. Так тримати!</EmptyState>
        ) : (
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>Дисципліна</th>
                  <th>Семестр</th>
                  <th>Викладач</th>
                  <th>Відкрито</th>
                  <th>Закрито</th>
                  <th>Стан</th>
                </tr>
              </thead>
              <tbody>
                {data.debts.map((d) => (
                  <tr key={d.id}>
                    <td>{d.subject}</td>
                    <td>
                      {d.semester} ({d.academicYear})
                    </td>
                    <td>{d.teacher}</td>
                    <td>{formatDate(d.openedAt)}</td>
                    <td>{formatDate(d.closedAt)}</td>
                    <td>
                      <Badge tone={d.status === 'OPEN' ? 'bad' : 'good'}>{DEBT_TITLES[d.status]}</Badge>
                    </td>
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
