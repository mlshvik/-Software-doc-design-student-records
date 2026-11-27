import { useEffect, useState } from 'react';
import { ApiError, api } from '../../api/client';
import type { Group, Student, StudentStatus } from '../../api/types';
import { FormFields, num, str, type FieldDef, type FormValues } from '../../components/FormFields';
import { ConfirmDialog, Modal } from '../../components/Modal';
import { Badge, Card, EmptyState, ErrorBox, Loader, PageHeader } from '../../components/ui';
import { Link } from '../../router';
import { STATUS_TITLES } from '../../utils/format';
import { useApi } from '../../utils/useApi';
import { groupOptions } from './lookups';

/** Дозволені переходи статусів – дзеркало шаблону State на бекенді (рішення приймає сервер). */
const TRANSITIONS: Record<StudentStatus, StudentStatus[]> = {
  ACTIVE: ['ACADEMIC_LEAVE', 'EXPELLED', 'GRADUATED'],
  ACADEMIC_LEAVE: ['ACTIVE', 'EXPELLED'],
  EXPELLED: ['ACTIVE'],
  GRADUATED: [],
};

const STATUS_TONE: Record<StudentStatus, 'good' | 'warn' | 'bad' | 'neutral'> = {
  ACTIVE: 'good',
  ACADEMIC_LEAVE: 'warn',
  EXPELLED: 'bad',
  GRADUATED: 'neutral',
};

const FIELDS: FieldDef[] = [
  { name: 'lastName', label: 'Прізвище', required: true },
  { name: 'firstName', label: "Ім'я", required: true },
  { name: 'middleName', label: 'По батькові' },
  { name: 'recordBookNumber', label: 'Номер залікової книжки', required: true },
  { name: 'groupId', label: 'Група', type: 'select', required: true, options: groupOptions },
  { name: 'birthDate', label: 'Дата народження', type: 'date' },
  { name: 'email', label: 'E-mail', type: 'email' },
  { name: 'phone', label: 'Телефон', type: 'tel', placeholder: '+380…' },
];

const toForm = (s: Student): FormValues => ({
  lastName: s.lastName,
  firstName: s.firstName,
  middleName: s.middleName ?? '',
  recordBookNumber: s.recordBookNumber,
  groupId: String(s.groupId),
  birthDate: s.birthDate ?? '',
  email: s.email ?? '',
  phone: s.phone ?? '',
});

const toRequest = (v: FormValues) => ({
  lastName: str(v.lastName),
  firstName: str(v.firstName),
  middleName: str(v.middleName),
  recordBookNumber: str(v.recordBookNumber),
  groupId: num(v.groupId),
  birthDate: str(v.birthDate),
  email: str(v.email),
  phone: str(v.phone),
});

export function StudentsPage() {
  const [filters, setFilters] = useState({ groupId: '', course: '', status: '', q: '' });
  const [q, setQ] = useState('');
  const [groups, setGroups] = useState<Group[]>([]);
  const { data, error, loading, reload } = useApi(
    () => api.get<Student[]>('/students', { groupId: filters.groupId, course: filters.course, status: filters.status, q: filters.q }),
    [filters.groupId, filters.course, filters.status, filters.q],
  );
  const [editing, setEditing] = useState<{ row: Student | null; values: FormValues } | null>(null);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [statusFor, setStatusFor] = useState<Student | null>(null);
  const [deleting, setDeleting] = useState<Student | null>(null);
  const [busy, setBusy] = useState(false);
  const [notice, setNotice] = useState<{ tone: 'success' | 'error'; text: string } | null>(null);

  useEffect(() => {
    api.get<Group[]>('/groups').then(setGroups).catch(() => setGroups([]));
  }, []);

  // Пошук за ПІБ – із затримкою, щоб не надсилати запит на кожну літеру.
  useEffect(() => {
    const t = window.setTimeout(() => setFilters((f) => ({ ...f, q })), 350);
    return () => window.clearTimeout(t);
  }, [q]);

  const save = async () => {
    if (!editing) return;
    setBusy(true);
    setErrors({});
    setFormError(null);
    try {
      const body = toRequest(editing.values);
      if (editing.row) await api.put(`/students/${editing.row.id}`, body);
      else await api.post('/students', body);
      setNotice({ tone: 'success', text: editing.row ? 'Дані студента оновлено' : 'Студента додано' });
      setEditing(null);
      reload();
    } catch (err) {
      if (err instanceof ApiError) {
        setErrors(err.fieldErrors);
        setFormError(err.message);
      }
    } finally {
      setBusy(false);
    }
  };

  const changeStatus = async (student: Student, status: StudentStatus) => {
    setBusy(true);
    try {
      await api.patch(`/students/${student.id}/status`, { status });
      setNotice({ tone: 'success', text: `${student.fullName}: статус змінено на «${STATUS_TITLES[status]}»` });
      setStatusFor(null);
      reload();
    } catch (err) {
      setNotice({ tone: 'error', text: err instanceof Error ? err.message : 'Не вдалося змінити статус' });
      setStatusFor(null);
    } finally {
      setBusy(false);
    }
  };

  const remove = async () => {
    if (!deleting) return;
    setBusy(true);
    try {
      await api.delete(`/students/${deleting.id}`);
      setNotice({ tone: 'success', text: 'Студента видалено' });
      reload();
    } catch (err) {
      setNotice({ tone: 'error', text: err instanceof Error ? err.message : 'Не вдалося видалити' });
    } finally {
      setBusy(false);
      setDeleting(null);
    }
  };

  return (
    <>
      <PageHeader
        title="Студенти"
        subtitle="Пошук, фільтрація, редагування, зміна статусу"
        actions={
          <button type="button" className="btn btn-primary" onClick={() => { setErrors({}); setFormError(null); setEditing({ row: null, values: {} }); }}>
            + Додати студента
          </button>
        }
      />
      {notice && (
        <div className={`alert alert-${notice.tone}`}>
          <span>{notice.text}</span>
          <button type="button" className="btn btn-small btn-ghost" onClick={() => setNotice(null)}>×</button>
        </div>
      )}
      <Card>
        <div className="filters">
          <input type="search" placeholder="Прізвище, ім'я або залікова…" value={q} onChange={(e) => setQ(e.target.value)} />
          <select value={filters.groupId} onChange={(e) => setFilters((f) => ({ ...f, groupId: e.target.value }))}>
            <option value="">Усі групи</option>
            {groups.map((g) => (
              <option key={g.id} value={g.id}>{g.name}</option>
            ))}
          </select>
          <select value={filters.course} onChange={(e) => setFilters((f) => ({ ...f, course: e.target.value }))}>
            <option value="">Усі курси</option>
            {[1, 2, 3, 4, 5, 6].map((c) => (
              <option key={c} value={c}>{c} курс</option>
            ))}
          </select>
          <select value={filters.status} onChange={(e) => setFilters((f) => ({ ...f, status: e.target.value }))}>
            <option value="">Усі статуси</option>
            {(Object.keys(STATUS_TITLES) as StudentStatus[]).map((s) => (
              <option key={s} value={s}>{STATUS_TITLES[s]}</option>
            ))}
          </select>
          {data && <span className="muted small">Знайдено: {data.length}</span>}
        </div>
        {loading && !data && <Loader />}
        {error && <ErrorBox error={error} onRetry={reload} />}
        {data && data.length === 0 && <EmptyState>Студентів за цими умовами не знайдено</EmptyState>}
        {data && data.length > 0 && (
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>ПІБ</th>
                  <th>Залікова</th>
                  <th>Група</th>
                  <th className="num">Курс</th>
                  <th>Статус</th>
                  <th>E-mail</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {data.map((s) => (
                  <tr key={s.id}>
                    <td>
                      <Link to={`/admin/students/${s.id}`} className="link">{s.fullName}</Link>
                    </td>
                    <td>{s.recordBookNumber}</td>
                    <td><span className="group-tag">{s.groupName}</span></td>
                    <td className="num">{s.course}</td>
                    <td><Badge tone={STATUS_TONE[s.status]}>{STATUS_TITLES[s.status]}</Badge></td>
                    <td className="small">{s.email ?? '—'}</td>
                    <td className="actions-cell">
                      <button type="button" className="btn btn-small" onClick={() => { setErrors({}); setFormError(null); setEditing({ row: s, values: toForm(s) }); }}>
                        Редагувати
                      </button>{' '}
                      <button type="button" className="btn btn-small" disabled={TRANSITIONS[s.status].length === 0} onClick={() => setStatusFor(s)}>
                        Статус
                      </button>{' '}
                      <button type="button" className="btn btn-small btn-danger" onClick={() => setDeleting(s)}>
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
          title={editing.row ? `Редагування: ${editing.row.fullName}` : 'Новий студент'}
          onClose={() => setEditing(null)}
          footer={
            <>
              <button type="button" className="btn" onClick={() => setEditing(null)}>Скасувати</button>
              <button type="button" className="btn btn-primary" onClick={save} disabled={busy}>{busy ? 'Збереження…' : 'Зберегти'}</button>
            </>
          }
        >
          {formError && <div className="alert alert-error">{formError}</div>}
          <FormFields
            fields={FIELDS}
            values={editing.values}
            errors={errors}
            onChange={(name, value) => setEditing((e) => (e ? { ...e, values: { ...e.values, [name]: value } } : e))}
          />
        </Modal>
      )}

      {statusFor && (
        <Modal title={`Статус: ${statusFor.fullName}`} onClose={() => setStatusFor(null)}>
          <p>
            Поточний статус: <Badge tone={STATUS_TONE[statusFor.status]}>{STATUS_TITLES[statusFor.status]}</Badge>
          </p>
          <p className="muted small">Доступні переходи визначає поточний стан студента (шаблон State).</p>
          <div className="toolbar">
            {TRANSITIONS[statusFor.status].map((target) => (
              <button key={target} type="button" className="btn" disabled={busy} onClick={() => changeStatus(statusFor, target)}>
                → {STATUS_TITLES[target]}
              </button>
            ))}
          </div>
        </Modal>
      )}

      {deleting && (
        <ConfirmDialog
          text={`Видалити студента ${deleting.fullName}? Разом зі студентом буде втрачено його оцінки та історію.`}
          onConfirm={remove}
          onCancel={() => setDeleting(null)}
          busy={busy}
        />
      )}
    </>
  );
}
