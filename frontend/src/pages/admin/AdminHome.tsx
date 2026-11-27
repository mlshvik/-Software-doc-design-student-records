import { Card, PageHeader } from '../../components/ui';

/** Тимчасова сторінка адміністратора: адмін-панель реалізується на наступному етапі. */
export function AdminHome() {
  return (
    <>
      <PageHeader title="Адміністрування" subtitle="Панель адміністратора (деканату)" />
      <Card>
        <p>
          Адмін-панель (довідники, студенти, викладачі, призначення, розклад, звіти, налаштування) – наступний етап
          розробки. Поки що всі функції доступні через REST API:{' '}
          <a href="/swagger-ui/index.html" className="link">Swagger UI</a>.
        </p>
      </Card>
    </>
  );
}
