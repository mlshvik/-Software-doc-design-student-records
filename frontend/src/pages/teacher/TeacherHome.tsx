import { useMemo } from 'react';
import { api } from '../../api/client';
import type { Assignment } from '../../api/types';
import { Card, EmptyState, ErrorBox, Loader, PageHeader } from '../../components/ui';
import { Link } from '../../router';
import { CONTROL_TITLES, formatNumber } from '../../utils/format';
import { useApi } from '../../utils/useApi';

/** Дисципліни викладача, згруповані за навчальними роками й семестрами. */
export function TeacherHome() {
  const { data, error, loading, reload } = useApi(() => api.get<Assignment[]>('/assignments'));

  const groups = useMemo(() => {
    const map = new Map<string, Assignment[]>();
    (data ?? []).forEach((a) => {
      const key = `${a.academicYear} · ${a.semester} семестр`;
      map.set(key, [...(map.get(key) ?? []), a]);
    });
    return [...map.entries()].sort((a, b) => b[0].localeCompare(a[0]));
  }, [data]);

  return (
    <>
      <PageHeader title="Мої дисципліни" subtitle="Оберіть дисципліну, щоб відкрити відомість групи та виставити оцінки" />
      {loading && <Loader />}
      {error && <ErrorBox error={error} onRetry={reload} />}
      {data && data.length === 0 && (
        <Card>
          <EmptyState>Вам ще не призначено жодної дисципліни.</EmptyState>
        </Card>
      )}
      {groups.map(([period, items]) => (
        <Card key={period} title={period}>
          <div className="tiles">
            {items.map((a) => (
              <Link key={a.id} to={`/teacher/sheet/${a.id}`} className="tile">
                <div className="tile-title">{a.subjectName}</div>
                <div className="tile-meta">
                  <span className="group-tag">{a.groupName}</span>
                  {a.course} курс · {CONTROL_TITLES[a.controlType]} · {formatNumber(a.credits)} кр.
                </div>
                <div className="tile-action">Відкрити відомість →</div>
              </Link>
            ))}
          </div>
        </Card>
      ))}
    </>
  );
}
