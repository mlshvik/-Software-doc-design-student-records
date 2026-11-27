import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import type { Debt, Group, Report, Student, Teacher } from '../../api/types';
import { ReportView } from '../../components/ReportView';
import { Card, ErrorBox, Loader, PageHeader, StatCard } from '../../components/ui';
import { Link } from '../../router';

interface Overview {
  students: Student[];
  teachers: Teacher[];
  groups: Group[];
  openDebts: Debt[];
  performance: Report;
}

/** Огляд для адміністратора: ключові показники та успішність груп. */
export function AdminHome() {
  const [data, setData] = useState<Overview | null>(null);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    Promise.all([
      api.get<Student[]>('/students'),
      api.get<Teacher[]>('/teachers'),
      api.get<Group[]>('/groups'),
      api.get<Debt[]>('/debts', { status: 'OPEN' }),
      api.get<Report>('/reports/group-performance'),
    ])
      .then(([students, teachers, groups, openDebts, performance]) => setData({ students, teachers, groups, openDebts, performance }))
      .catch((e: Error) => setError(e));
  }, []);

  if (error) return <ErrorBox error={error} />;
  if (!data) return <Loader />;
  const active = data.students.filter((s) => s.status === 'ACTIVE').length;
  const debtors = new Set(data.openDebts.map((d) => d.studentId)).size;

  return (
    <>
      <PageHeader title="Огляд" subtitle="Панель адміністратора (деканату)" />
      <div className="stats">
        <StatCard label="Студентів" value={data.students.length} hint={`навчаються: ${active}`} />
        <StatCard label="Викладачів" value={data.teachers.length} />
        <StatCard label="Груп" value={data.groups.length} />
        <StatCard label="Відкритих боргів" value={data.openDebts.length} hint={`студентів-боржників: ${debtors}`} tone={data.openDebts.length ? 'warn' : 'good'} />
      </div>
      <Card title="Успішність за групами" actions={<Link to="/admin/reports" className="link">Усі звіти →</Link>}>
        <ReportView report={data.performance} chartKey="averageScore" labelKey="group" />
      </Card>
    </>
  );
}
