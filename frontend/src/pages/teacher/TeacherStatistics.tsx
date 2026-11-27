import { api } from '../../api/client';
import type { Report } from '../../api/types';
import { useAuth } from '../../auth/AuthContext';
import { Card, ErrorBox, Loader, PageHeader } from '../../components/ui';
import { ReportView } from '../../components/ReportView';
import { useApi } from '../../utils/useApi';

export function TeacherStatistics() {
  const { user } = useAuth();
  const { data, error, loading, reload } = useApi(() => api.get<Report>('/reports/teacher-statistics'));
  const lastName = user?.fullName.split(' ')[0] ?? '';

  return (
    <>
      <PageHeader title="Статистика оцінювання" subtitle="Ваш рядок підсвічено; для порівняння показано показники колег" />
      {loading && <Loader />}
      {error && <ErrorBox error={error} onRetry={reload} />}
      {data && (
        <Card title={data.title}>
          <ReportView
            report={data}
            chartKey="averageScore"
            highlight={(row) => String(row.teacher ?? '').startsWith(lastName)}
          />
        </Card>
      )}
    </>
  );
}
