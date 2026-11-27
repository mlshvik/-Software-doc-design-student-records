import { useState, type FormEvent } from 'react';
import { ApiError } from '../api/client';
import { homePath, useAuth } from '../auth/AuthContext';
import { useRouter } from '../router';

const DEMO_ACCOUNTS = [
  { role: 'Адміністратор', username: 'admin', password: 'admin123' },
  { role: 'Викладач', username: 'melnyk', password: 'teacher123' },
  { role: 'Студент', username: 'ir23101', password: 'student123' },
];

export function LoginPage() {
  const { login } = useAuth();
  const { navigate } = useRouter();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const user = await login(username.trim(), password);
      navigate(homePath(user), { replace: true });
    } catch (err) {
      setError(err instanceof ApiError && err.status === 401 ? 'Невірний логін або пароль' : (err as Error).message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-brand">
          <img src="/favicon.svg" alt="" width={48} height={48} />
          <div>
            <h1>Облік студентів</h1>
            <p className="muted">Система обліку студентів у ВНЗ</p>
          </div>
        </div>
        <form onSubmit={submit} className="form">
          <label className="field">
            <span>Логін</span>
            <input value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" autoFocus required />
          </label>
          <label className="field">
            <span>Пароль</span>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
              required
            />
          </label>
          {error && <div className="alert alert-error">{error}</div>}
          <button type="submit" className="btn btn-primary btn-block" disabled={busy}>
            {busy ? 'Вхід…' : 'Увійти'}
          </button>
        </form>
        <div className="demo">
          <div className="muted small">Демонстраційні облікові записи:</div>
          {DEMO_ACCOUNTS.map((a) => (
            <button
              key={a.username}
              type="button"
              className="demo-account"
              onClick={() => {
                setUsername(a.username);
                setPassword(a.password);
              }}
            >
              <strong>{a.role}</strong> · {a.username}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
