import { api } from '../../api/client';
import type { ScheduleEntry } from '../../api/types';
import { Card, EmptyState, ErrorBox, Loader, PageHeader } from '../../components/ui';
import { ScheduleGrid } from '../../components/ScheduleGrid';
import { useApi } from '../../utils/useApi';

export function TeacherSchedule() {
  const { data, error, loading, reload } = useApi(() => api.get<ScheduleEntry[]>('/schedule/me'));
  return (
    <>
      <PageHeader title="Мій розклад" subtitle={data ? `Занять на тиждень: ${data.length}` : undefined} />
      {loading && <Loader />}
      {error && <ErrorBox error={error} onRetry={reload} />}
      {data && (
        <Card>
          {data.length === 0 ? (
            <EmptyState>Занять у розкладі немає.</EmptyState>
          ) : (
            <ScheduleGrid
              lessons={data.map((e) => ({
                id: e.id,
                dayOfWeek: e.dayOfWeek,
                pairNumber: e.pairNumber,
                weekType: e.weekType,
                lessonType: e.lessonType,
                subject: e.subjectName,
                classroom: e.classroom,
                extra: `група ${e.groupName}`,
              }))}
            />
          )}
        </Card>
      )}
    </>
  );
}
