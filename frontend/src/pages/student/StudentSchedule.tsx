import { Card, EmptyState, ErrorBox, Loader, PageHeader } from '../../components/ui';
import { ScheduleGrid } from '../../components/ScheduleGrid';
import { useStudentRecord } from './useStudentRecord';

export function StudentSchedule() {
  const { data, error, loading, reload } = useStudentRecord();
  if (loading) return <Loader />;
  if (error || !data) return <ErrorBox error={error ?? 'Немає даних'} onRetry={reload} />;

  return (
    <>
      <PageHeader title="Розклад занять" subtitle={`Група ${data.student.groupName}`} />
      <Card>
        {data.schedule.length === 0 ? (
          <EmptyState>
            {data.student.status === 'ACTIVE'
              ? 'Розклад для групи ще не сформовано.'
              : `Розклад не відображається: статус «${data.student.statusTitle}».`}
          </EmptyState>
        ) : (
          <ScheduleGrid
            lessons={data.schedule.map((l) => ({
              id: l.id,
              dayOfWeek: l.dayOfWeek,
              pairNumber: l.pairNumber,
              weekType: l.weekType,
              lessonType: l.lessonType,
              subject: l.subject,
              classroom: l.classroom,
              extra: l.teacher,
            }))}
          />
        )}
      </Card>
    </>
  );
}
