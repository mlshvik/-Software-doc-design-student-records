import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import type { Faculty, Report, ReportTypeInfo } from '../../api/types';
import { ReportView } from '../../components/ReportView';
import { Card, ErrorBox, Loader, PageHeader } from '../../components/ui';

/** Для кожного звіту – числова колонка для діаграми та колонка підписів. */
const CHART: Record<string, { chartKey?: string; labelKey?: string }> = {
  'group-performance': { chartKey: 'averageScore', labelKey: 'group' },
  'teacher-statistics': { chartKey: 'averageScore', labelKey: 'teacher' },
  'academic-debts': { chartKey: 'daysOpen', labelKey: 'student' },
};

/**
 * Звіти: тип обирається вкладкою, бекенд створює потрібний генератор (Factory Method),
 * який формує звіт за єдиним алгоритмом (Template Method) через будівельника (Builder).
 */
export function ReportsPage() {
  const [types, setTypes] = useState<ReportTypeInfo[]>([]);
  const [slug, setSlug] = useState('group-performance');
  const [faculties, setFaculties] = useState<Faculty[]>([]);
  const [params, setParams] = useState({ academicYear: '', semester: '', course: '', facultyId: '' });
  const [report, setReport] = useState<Report | null>(null);
  const [error, setError] = useState<Error | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    api.get<ReportTypeInfo[]>('/reports').then(setTypes).catch(() => setTypes([]));
    api.get<Faculty[]>('/faculties').then(setFaculties).catch(() => setFaculties([]));
  }, []);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    api.get<Report>(`/reports/${slug}`, params)
      .then((r) => { if (!cancelled) setReport(r); })
      .catch((e: Error) => { if (!cancelled) setError(e); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [slug, params]);

  const set = (key: keyof typeof params, value: string) => setParams((p) => ({ ...p, [key]: value }));

  return (
    <>
      <PageHeader title="Звіти" subtitle="Успішність, статистика викладачів, академічні заборгованості" actions={
        <button type="button" className="btn" onClick={() => window.print()}>🖨 Друк</button>
      } />
      <div className="tabs">
        {types.map((t) => (
          <button key={t.slug} type="button" className={`tab ${t.slug === slug ? 'active' : ''}`} onClick={() => setSlug(t.slug)}>
            {t.title}
          </button>
        ))}
      </div>
      <Card>
        <div className="filters">
          <select value={params.academicYear} onChange={(e) => set('academicYear', e.target.value)}>
            <option value="">Усі навчальні роки</option>
            <option value="2025/2026">2025/2026</option>
            <option value="2026/2027">2026/2027</option>
          </select>
          <select value={params.semester} onChange={(e) => set('semester', e.target.value)}>
            <option value="">Усі семестри</option>
            {[1, 2, 3, 4, 5, 6, 7, 8].map((s) => <option key={s} value={s}>{s} семестр</option>)}
          </select>
          <select value={params.course} onChange={(e) => set('course', e.target.value)}>
            <option value="">Усі курси</option>
            {[1, 2, 3, 4, 5, 6].map((c) => <option key={c} value={c}>{c} курс</option>)}
          </select>
          <select value={params.facultyId} onChange={(e) => set('facultyId', e.target.value)}>
            <option value="">Усі факультети</option>
            {faculties.map((f) => <option key={f.id} value={f.id}>{f.shortName}</option>)}
          </select>
        </div>
        {loading && <Loader text="Формування звіту…" />}
        {error && <ErrorBox error={error} />}
        {report && !loading && (
          <>
            <h2 className="report-title">{report.title}</h2>
            <ReportView report={report} chartKey={CHART[slug]?.chartKey} labelKey={CHART[slug]?.labelKey} />
          </>
        )}
      </Card>
    </>
  );
}
