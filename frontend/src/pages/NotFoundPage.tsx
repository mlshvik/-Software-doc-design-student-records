import { homePath, useAuth } from '../auth/AuthContext';
import { Link } from '../router';

export function NotFoundPage() {
  const { user } = useAuth();
  return (
    <div className="not-found">
      <h1>404</h1>
      <p>Сторінку не знайдено або вона недоступна для вашої ролі.</p>
      <Link to={user ? homePath(user) : '/login'} className="btn btn-primary">
        На головну
      </Link>
    </div>
  );
}
