import { api } from '../api/client';
import type { Notification } from '../api/types';
import { Card, EmptyState, ErrorBox, Loader, PageHeader } from '../components/ui';
import { formatDateTime } from '../utils/format';
import { useApi } from '../utils/useApi';

export function NotificationsPage() {
  const { data, error, loading, reload, setData } = useApi(() => api.get<Notification[]>('/notifications/me'));

  const markRead = async (id: number) => {
    await api.patch(`/notifications/${id}/read`);
    setData((data ?? []).map((n) => (n.id === id ? { ...n, read: true } : n)));
  };

  const markAll = async () => {
    await api.post('/notifications/me/read-all');
    reload();
  };

  const unread = (data ?? []).filter((n) => !n.read).length;
  return (
    <>
      <PageHeader
        title="Сповіщення"
        subtitle={unread ? `Непрочитаних: ${unread}` : 'Усі сповіщення прочитано'}
        actions={
          unread > 0 && (
            <button type="button" className="btn" onClick={markAll}>
              Позначити всі як прочитані
            </button>
          )
        }
      />
      {loading && <Loader />}
      {error && <ErrorBox error={error} onRetry={reload} />}
      {data && (
        <Card>
          {data.length === 0 ? (
            <EmptyState>Сповіщень поки немає. Тут з'являться нові оцінки та зміни заборгованостей.</EmptyState>
          ) : (
            <ul className="notifications">
              {data.map((n) => (
                <li key={n.id} className={n.read ? 'read' : 'unread'}>
                  <div>
                    <div>{n.message}</div>
                    <div className="muted small">{formatDateTime(n.createdAt)}</div>
                  </div>
                  {!n.read && (
                    <button type="button" className="btn btn-small" onClick={() => markRead(n.id)}>
                      Прочитано
                    </button>
                  )}
                </li>
              ))}
            </ul>
          )}
        </Card>
      )}
    </>
  );
}
