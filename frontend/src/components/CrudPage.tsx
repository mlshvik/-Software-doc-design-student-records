import { useMemo, useState, type ReactNode } from 'react';
import { ApiError, api } from '../api/client';
import { useApi } from '../utils/useApi';
import { FormFields, type FieldDef, type FormValues } from './FormFields';
import { ConfirmDialog, Modal } from './Modal';
import { Card, EmptyState, ErrorBox, Loader, PageHeader } from './ui';

export interface ColumnDef<T> {
  title: string;
  render: (row: T) => ReactNode;
  numeric?: boolean;
}

export interface CrudConfig<T extends { id: number }> {
  title: string;
  subtitle?: string;
  /** Шлях ресурсу REST API, напр. `/faculties`. */
  endpoint: string;
  entityName: string;
  columns: ColumnDef<T>[];
  fields: FieldDef[];
  /** Значення форми для редагування наявного запису. */
  toForm: (row: T) => FormValues;
  /** Тіло запиту POST/PUT з даних форми. */
  toRequest: (values: FormValues) => unknown;
  /** Рядок для пошуку по таблиці. */
  searchText?: (row: T) => string;
  deleteLabel?: (row: T) => string;
}

/**
 * Універсальна сторінка довідника: таблиця з пошуком, створення й редагування в модальному
 * вікні, видалення з підтвердженням. Помилки валідації сервера показуються біля полів.
 */
export function CrudPage<T extends { id: number }>({ config }: { config: CrudConfig<T> }) {
  const { data, error, loading, reload } = useApi(() => api.get<T[]>(config.endpoint), [config.endpoint]);
  const [query, setQuery] = useState('');
  const [editing, setEditing] = useState<{ row: T | null; values: FormValues } | null>(null);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState<T | null>(null);
  const [notice, setNotice] = useState<{ tone: 'success' | 'error'; text: string } | null>(null);

  const rows = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q || !config.searchText) return data ?? [];
    return (data ?? []).filter((r) => config.searchText!(r).toLowerCase().includes(q));
  }, [data, query, config]);

  const openCreate = () => {
    setErrors({});
    setFormError(null);
    setEditing({ row: null, values: {} });
  };
  const openEdit = (row: T) => {
    setErrors({});
    setFormError(null);
    setEditing({ row, values: config.toForm(row) });
  };

  const save = async () => {
    if (!editing) return;
    setSaving(true);
    setErrors({});
    setFormError(null);
    try {
      const body = config.toRequest(editing.values);
      if (editing.row) await api.put(`${config.endpoint}/${editing.row.id}`, body);
      else await api.post(config.endpoint, body);
      setNotice({ tone: 'success', text: editing.row ? 'Зміни збережено' : `${config.entityName}: запис створено` });
      setEditing(null);
      reload();
    } catch (err) {
      if (err instanceof ApiError) {
        setErrors(err.fieldErrors);
        setFormError(err.message);
      } else {
        setFormError('Не вдалося зберегти');
      }
    } finally {
      setSaving(false);
    }
  };

  const remove = async () => {
    if (!deleting) return;
    setSaving(true);
    try {
      await api.delete(`${config.endpoint}/${deleting.id}`);
      setNotice({ tone: 'success', text: 'Запис видалено' });
      reload();
    } catch (err) {
      setNotice({ tone: 'error', text: err instanceof Error ? err.message : 'Не вдалося видалити' });
    } finally {
      setSaving(false);
      setDeleting(null);
    }
  };

  return (
    <>
      <PageHeader
        title={config.title}
        subtitle={config.subtitle}
        actions={
          <button type="button" className="btn btn-primary" onClick={openCreate}>
            + Додати
          </button>
        }
      />
      {notice && (
        <div className={`alert alert-${notice.tone}`}>
          <span>{notice.text}</span>
          <button type="button" className="btn btn-small btn-ghost" onClick={() => setNotice(null)}>
            ×
          </button>
        </div>
      )}
      <Card>
        {config.searchText && (
          <div className="filters">
            <input type="search" placeholder="Пошук…" value={query} onChange={(e) => setQuery(e.target.value)} />
            <span className="muted small">Знайдено: {rows.length}</span>
          </div>
        )}
        {loading && !data && <Loader />}
        {error && <ErrorBox error={error} onRetry={reload} />}
        {data && rows.length === 0 && <EmptyState>Записів немає</EmptyState>}
        {rows.length > 0 && (
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  {config.columns.map((c) => (
                    <th key={c.title} className={c.numeric ? 'num' : ''}>
                      {c.title}
                    </th>
                  ))}
                  <th />
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => (
                  <tr key={row.id}>
                    {config.columns.map((c) => (
                      <td key={c.title} className={c.numeric ? 'num' : ''}>
                        {c.render(row)}
                      </td>
                    ))}
                    <td className="actions-cell">
                      <button type="button" className="btn btn-small" onClick={() => openEdit(row)}>
                        Редагувати
                      </button>{' '}
                      <button type="button" className="btn btn-small btn-danger" onClick={() => setDeleting(row)}>
                        Видалити
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      {editing && (
        <Modal
          title={editing.row ? `Редагування: ${config.entityName.toLowerCase()}` : `Новий запис: ${config.entityName.toLowerCase()}`}
          onClose={() => setEditing(null)}
          footer={
            <>
              <button type="button" className="btn" onClick={() => setEditing(null)}>
                Скасувати
              </button>
              <button type="button" className="btn btn-primary" onClick={save} disabled={saving}>
                {saving ? 'Збереження…' : 'Зберегти'}
              </button>
            </>
          }
        >
          {formError && <div className="alert alert-error">{formError}</div>}
          <FormFields
            fields={config.fields}
            values={editing.values}
            errors={errors}
            onChange={(name, value) => setEditing((e) => (e ? { ...e, values: { ...e.values, [name]: value } } : e))}
          />
        </Modal>
      )}
      {deleting && (
        <ConfirmDialog
          text={`Видалити ${config.deleteLabel ? config.deleteLabel(deleting) : 'запис'}? Якщо на запис посилаються інші дані, сервер не дозволить видалення.`}
          onConfirm={remove}
          onCancel={() => setDeleting(null)}
          busy={saving}
        />
      )}
    </>
  );
}
