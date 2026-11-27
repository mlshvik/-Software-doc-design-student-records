import type { Report } from '../api/types';
import { BarChart } from './BarChart';
import { formatDateTime, formatNumber } from '../utils/format';

/** Підписи для ключів підсумків звітів. */
const SUMMARY_TITLES: Record<string, string> = {
  groups: 'Груп',
  teachers: 'Викладачів',
  assignments: 'Призначень',
  averageScore: 'Середній бал',
  passRate: 'Успішність, %',
  qualityRate: 'Якість знань, %',
  open: 'Відкритих боргів',
  closed: 'Закритих боргів',
  studentsWithDebts: 'Студентів із боргами',
  course: 'Курс',
  grades: 'Оцінок',
  students: 'Студентів',
};

const title = (key: string) => SUMMARY_TITLES[key] ?? key;

function isScalar(value: unknown): value is string | number | boolean {
  return ['string', 'number', 'boolean'].includes(typeof value);
}

/** Вкладені підсумки (напр., розбивка за курсами чи групами). */
function NestedSummary({ name, value }: { name: string; value: unknown }) {
  if (Array.isArray(value) && value.length && typeof value[0] === 'object') {
    const rows = value as Record<string, unknown>[];
    const keys = Object.keys(rows[0]);
    return (
      <div className="nested-summary">
        <h3>{name === 'byCourse' ? 'За курсами' : title(name)}</h3>
        <table className="table table-compact">
          <thead>
            <tr>{keys.map((k) => <th key={k}>{title(k)}</th>)}</tr>
          </thead>
          <tbody>
            {rows.map((r, i) => (
              <tr key={i}>{keys.map((k) => <td key={k}>{formatNumber(r[k])}</td>)}</tr>
            ))}
          </tbody>
        </table>
      </div>
    );
  }
  if (value && typeof value === 'object') {
    const entries = Object.entries(value as Record<string, unknown>);
    return (
      <div className="nested-summary">
        <h3>{name === 'byGroup' ? 'За групами' : title(name)}</h3>
        <div className="chips">
          {entries.map(([k, v]) => (
            <span key={k} className="chip">
              {k}: <strong>{isScalar(v) ? formatNumber(v) : '…'}</strong>
            </span>
          ))}
        </div>
      </div>
    );
  }
  return null;
}

/**
 * Відображення звіту, сформованого на бекенді (Template Method + Builder):
 * підсумки, діаграма за вибраною числовою колонкою і таблиця.
 */
export function ReportView({ report, chartKey, labelKey, highlight }: {
  report: Report;
  /** Числова колонка для діаграми. */
  chartKey?: string;
  /** Колонка з підписами для діаграми (за замовчуванням – перша колонка). */
  labelKey?: string;
  /** Підсвітити рядки, для яких функція повертає true. */
  highlight?: (row: Record<string, unknown>) => boolean;
}) {
  const scalarSummary = Object.entries(report.summary).filter(([, v]) => isScalar(v));
  const nestedSummary = Object.entries(report.summary).filter(([, v]) => !isScalar(v) && v !== null);
  const labelColumn = labelKey ?? report.columns[0]?.key;
  const chartColumn = report.columns.find((c) => c.key === chartKey && c.numeric);
  const chartData = chartColumn && labelColumn
    ? report.rows.map((r) => ({ label: String(r[labelColumn] ?? ''), value: Number(r[chartColumn.key] ?? 0) }))
    : [];

  return (
    <div className="report">
      <div className="report-meta muted small">Сформовано: {formatDateTime(report.generatedAt)}</div>
      {scalarSummary.length > 0 && (
        <div className="stats">
          {scalarSummary.map(([k, v]) => (
            <div key={k} className="stat">
              <div className="stat-label">{title(k)}</div>
              <div className="stat-value">{formatNumber(v)}</div>
            </div>
          ))}
        </div>
      )}
      {chartColumn && chartData.length > 0 && (
        <div className="chart-block">
          <h3>{chartColumn.title}</h3>
          <BarChart
            data={chartData}
            max={chartColumn.key.toLowerCase().includes('rate') ? 100 : chartColumn.key === 'averageScore' ? 100 : undefined}
            threshold={chartColumn.key === 'averageScore' ? 60 : undefined}
            unit={chartColumn.key.toLowerCase().includes('rate') ? '%' : ''}
          />
        </div>
      )}
      <div className="table-wrap">
        <table className="table">
          <thead>
            <tr>
              {report.columns.map((c) => (
                <th key={c.key} className={c.numeric ? 'num' : ''}>
                  {c.title}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {report.rows.length === 0 && (
              <tr>
                <td colSpan={report.columns.length} className="muted center">
                  Немає даних за вибраними параметрами
                </td>
              </tr>
            )}
            {report.rows.map((row, i) => (
              <tr key={i} className={highlight?.(row) ? 'row-highlight' : ''}>
                {report.columns.map((c) => (
                  <td key={c.key} className={c.numeric ? 'num' : ''}>
                    {c.numeric ? formatNumber(row[c.key]) : String(row[c.key] ?? '—')}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {nestedSummary.map(([k, v]) => (
        <NestedSummary key={k} name={k} value={v} />
      ))}
    </div>
  );
}
