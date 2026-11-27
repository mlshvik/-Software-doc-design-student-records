import { useEffect, useState } from 'react';
import { ApiError, api } from '../../api/client';
import type { Assignment, DayOfWeek, Group, LessonType, ScheduleConflict, ScheduleEntry, ScheduleEntryRequest, WeekType } from '../../api/types';
import { ConfirmDialog } from '../../components/Modal';
import { ScheduleGrid } from '../../components/ScheduleGrid';
import { Card, EmptyState, ErrorBox, Loader, PageHeader } from '../../components/ui';
import { DAYS, DAY_TITLES, LESSON_TITLES, PAIR_TIMES, WEEK_TITLES } from '../../utils/format';
import { useApi } from '../../utils/useApi';
import { assignmentLabel } from './lookups';

interface Draft {
  assignmentId: string;
  dayOfWeek: DayOfWeek;
  pairNumber: string;
  weekType: WeekType;
  classroom: string;
  lessonType: LessonType;
}

const EMPTY: Draft = { assignmentId: '', dayOfWeek: 'MONDAY', pairNumber: '1', weekType: 'ALL', classroom: '', lessonType: 'LECTURE' };

/**
 * Конструктор розкладу групи. Під час заповнення форми бекенд перевіряє конфлікти
 * (зайнятий викладач, група або аудиторія з урахуванням чисельника/знаменника).
 */
export function ScheduleBuilderPage() {
  const [groups, setGroups] = useState<Group[]>([]);
  const [groupId, setGroupId] = useState<string>('');
  const [assignments, setAssignments] = useState<Assignment[]>([]);
  const [draft, setDraft] = useState<Draft>(EMPTY);
  const [conflicts, setConflicts] = useState<ScheduleConflict[] | null>(null);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState<{ tone: 'success' | 'error'; text: string } | null>(null);
  const [deleting, setDeleting] = useState<number | null>(null);

  useEffect(() => {
    api.get<Group[]>('/groups').then((list) => {
      setGroups(list);
      if (list.length) setGroupId((g) => g || String(list[0].id));
    }).catch(() => setGroups([]));
  }, []);

  useEffect(() => {
    if (!groupId) return;
    setDraft(EMPTY);
    api.get<Assignment[]>('/assignments', { groupId }).then(setAssignments).catch(() => setAssignments([]));
  }, [groupId]);

  const { data: entries, error, loading, reload } = useApi(
    () => (groupId ? api.get<ScheduleEntry[]>('/schedule', { groupId }) : Promise.resolve([] as ScheduleEntry[])),
    [groupId],
  );

  const request = (): ScheduleEntryRequest | null => {
    if (!draft.assignmentId || !draft.classroom.trim()) return null;
    return {
      assignmentId: Number(draft.assignmentId),
      dayOfWeek: draft.dayOfWeek,
      pairNumber: Number(draft.pairNumber),
      weekType: draft.weekType,
      classroom: draft.classroom.trim(),
      lessonType: draft.lessonType,
    };
  };

  // Перевірка конфліктів «на льоту» (із затримкою після зміни полів).
  useEffect(() => {
    const body = request();
    if (!body) {
      setConflicts(null);
      return;
    }
    const t = window.setTimeout(() => {
      api.post<ScheduleConflict[]>('/schedule/check', body).then(setConflicts).catch(() => setConflicts(null));
    }, 300);
    return () => window.clearTimeout(t);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [draft]);

  const save = async () => {
    const body = request();
    if (!body) return;
    setSaving(true);
    setMessage(null);
    try {
      await api.post('/schedule', body);
      setMessage({ tone: 'success', text: 'Заняття додано до розкладу' });
      setDraft((d) => ({ ...d, classroom: '' }));
      reload();
    } catch (err) {
      if (err instanceof ApiError && err.problem.conflicts) setConflicts(err.problem.conflicts);
      setMessage({ tone: 'error', text: err instanceof Error ? err.message : 'Не вдалося зберегти' });
    } finally {
      setSaving(false);
    }
  };

  const remove = async () => {
    if (deleting === null) return;
    try {
      await api.delete(`/schedule/${deleting}`);
      setMessage({ tone: 'success', text: 'Заняття видалено' });
      reload();
    } catch (err) {
      setMessage({ tone: 'error', text: err instanceof Error ? err.message : 'Не вдалося видалити' });
    } finally {
      setDeleting(null);
    }
  };

  const set = <K extends keyof Draft>(key: K, value: Draft[K]) => setDraft((d) => ({ ...d, [key]: value }));
  const ready = request() !== null;

  return (
    <>
      <PageHeader
        title="Конструктор розкладу"
        subtitle="Оберіть групу, додайте заняття; конфлікти перевіряються автоматично"
        actions={
          <select className="btn" value={groupId} onChange={(e) => setGroupId(e.target.value)} aria-label="Група">
            {groups.map((g) => (
              <option key={g.id} value={g.id}>Група {g.name}</option>
            ))}
          </select>
        }
      />
      {message && <div className={`alert alert-${message.tone}`}>{message.text}</div>}
      <Card title="Нове заняття">
        <div className="form-grid form-grid-3">
          <label className="field">
            <span>Дисципліна і викладач</span>
            <select value={draft.assignmentId} onChange={(e) => set('assignmentId', e.target.value)}>
              <option value="">— оберіть —</option>
              {assignments.map((a) => (
                <option key={a.id} value={a.id}>{assignmentLabel(a)}</option>
              ))}
            </select>
          </label>
          <label className="field">
            <span>День тижня</span>
            <select value={draft.dayOfWeek} onChange={(e) => set('dayOfWeek', e.target.value as DayOfWeek)}>
              {DAYS.map((d) => <option key={d} value={d}>{DAY_TITLES[d]}</option>)}
            </select>
          </label>
          <label className="field">
            <span>Пара</span>
            <select value={draft.pairNumber} onChange={(e) => set('pairNumber', e.target.value)}>
              {[1, 2, 3, 4, 5, 6, 7, 8].map((p) => <option key={p} value={p}>{p} ({PAIR_TIMES[p]})</option>)}
            </select>
          </label>
          <label className="field">
            <span>Тиждень</span>
            <select value={draft.weekType} onChange={(e) => set('weekType', e.target.value as WeekType)}>
              {(Object.keys(WEEK_TITLES) as WeekType[]).map((w) => <option key={w} value={w}>{WEEK_TITLES[w]}</option>)}
            </select>
          </label>
          <label className="field">
            <span>Тип заняття</span>
            <select value={draft.lessonType} onChange={(e) => set('lessonType', e.target.value as LessonType)}>
              {(Object.keys(LESSON_TITLES) as LessonType[]).map((l) => <option key={l} value={l}>{LESSON_TITLES[l]}</option>)}
            </select>
          </label>
          <label className="field">
            <span>Аудиторія</span>
            <input value={draft.classroom} placeholder="213" onChange={(e) => set('classroom', e.target.value)} />
          </label>
        </div>
        <div className="builder-footer">
          {conflicts && conflicts.length > 0 && (
            <ul className="conflicts">
              {conflicts.map((c, i) => <li key={i}>{c.message}</li>)}
            </ul>
          )}
          {conflicts && conflicts.length === 0 && <div className="ok-text">✓ Конфліктів немає</div>}
          <button type="button" className="btn btn-primary" disabled={!ready || saving || (conflicts?.length ?? 0) > 0} onClick={save}>
            {saving ? 'Збереження…' : 'Додати заняття'}
          </button>
        </div>
      </Card>
      <Card title="Розклад групи">
        {loading && !entries && <Loader />}
        {error && <ErrorBox error={error} onRetry={reload} />}
        {entries && entries.length === 0 && <EmptyState>Занять ще немає – додайте перше вище.</EmptyState>}
        {entries && entries.length > 0 && (
          <ScheduleGrid
            onDelete={(id) => setDeleting(id)}
            lessons={entries.map((e) => ({
              id: e.id,
              dayOfWeek: e.dayOfWeek,
              pairNumber: e.pairNumber,
              weekType: e.weekType,
              lessonType: e.lessonType,
              subject: e.subjectName,
              classroom: e.classroom,
              extra: e.teacherName,
            }))}
          />
        )}
      </Card>
      {deleting !== null && (
        <ConfirmDialog text="Видалити заняття з розкладу?" onConfirm={remove} onCancel={() => setDeleting(null)} />
      )}
    </>
  );
}
