import { useState } from 'react';
import { api } from '../../api/client';
import type { Setting } from '../../api/types';
import { Card, ErrorBox, Loader, PageHeader } from '../../components/ui';
import { useApi } from '../../utils/useApi';

const GPA_KEY = 'gpa.strategy';
const GPA_OPTIONS = [
  { value: 'CREDIT_WEIGHTED', label: 'Зважений на кредити ЄКТС', hint: 'Σ(бал × кредити) / Σ кредитів – для стипендійного рейтингу' },
  { value: 'SIMPLE', label: 'Середнє арифметичне', hint: 'усі підсумкові оцінки мають однакову вагу' },
];

export function SettingsPage() {
  const { data, error, loading, reload } = useApi(() => api.get<Setting[]>('/settings'));
  const [drafts, setDrafts] = useState<Record<string, string>>({});
  const [message, setMessage] = useState<{ tone: 'success' | 'error'; text: string } | null>(null);
  const [saving, setSaving] = useState<string | null>(null);

  const save = async (key: string, value: string) => {
    setSaving(key);
    setMessage(null);
    try {
      await api.put(`/settings/${encodeURIComponent(key)}`, { value });
      setMessage({ tone: 'success', text: `Налаштування «${key}» збережено` });
      setDrafts((d) => { const next = { ...d }; delete next[key]; return next; });
      reload();
    } catch (err) {
      setMessage({ tone: 'error', text: err instanceof Error ? err.message : 'Не вдалося зберегти' });
    } finally {
      setSaving(null);
    }
  };

  const gpa = data?.find((s) => s.key === GPA_KEY);
  const others = (data ?? []).filter((s) => s.key !== GPA_KEY);

  return (
    <>
      <PageHeader title="Налаштування системи" />
      {message && <div className={`alert alert-${message.tone}`}>{message.text}</div>}
      {loading && !data && <Loader />}
      {error && <ErrorBox error={error} onRetry={reload} />}
      {data && (
        <>
          <Card title="Спосіб розрахунку середнього балу (шаблон Strategy)">
            <div className="form">
              {GPA_OPTIONS.map((o) => (
                <label key={o.value} className="radio">
                  <input
                    type="radio"
                    name="gpa"
                    checked={gpa?.value === o.value}
                    disabled={saving === GPA_KEY}
                    onChange={() => save(GPA_KEY, o.value)}
                  />
                  <span>
                    <strong>{o.label}</strong>
                    <span className="muted small"> – {o.hint}</span>
                  </span>
                </label>
              ))}
              <p className="muted small">Зміна одразу впливає на середній бал усіх студентів у кабінетах і звітах.</p>
            </div>
          </Card>
          {others.length > 0 && (
            <Card title="Інші параметри">
              <div className="table-wrap">
                <table className="table">
                  <thead>
                    <tr><th>Ключ</th><th>Опис</th><th>Значення</th><th /></tr>
                  </thead>
                  <tbody>
                    {others.map((s) => (
                      <tr key={s.key}>
                        <td><code>{s.key}</code></td>
                        <td className="small">{s.description ?? '—'}</td>
                        <td>
                          <input
                            className="inline-input"
                            value={drafts[s.key] ?? s.value}
                            onChange={(e) => setDrafts((d) => ({ ...d, [s.key]: e.target.value }))}
                          />
                        </td>
                        <td className="actions-cell">
                          <button
                            type="button"
                            className="btn btn-small btn-primary"
                            disabled={drafts[s.key] === undefined || saving === s.key}
                            onClick={() => save(s.key, drafts[s.key])}
                          >
                            Зберегти
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </Card>
          )}
        </>
      )}
    </>
  );
}
