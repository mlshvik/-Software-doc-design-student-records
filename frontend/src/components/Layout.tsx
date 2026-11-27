import { useEffect, useState, type ReactNode } from 'react';
import { api } from '../api/client';
import type { Role } from '../api/types';
import { useAuth } from '../auth/AuthContext';
import { Link, useRouter } from '../router';
import { initials } from '../utils/format';

export interface NavItem {
  to: string;
  label: string;
  icon: string;
}

/** Пункти бічного меню для кожної ролі. */
export const NAVIGATION: Record<Role, NavItem[]> = {
  STUDENT: [
    { to: '/student', label: 'Мій кабінет', icon: '🏠' },
    { to: '/student/grades', label: 'Оцінки', icon: '📘' },
    { to: '/student/schedule', label: 'Розклад', icon: '🗓' },
    { to: '/student/debts', label: 'Заборгованості', icon: '⚠' },
    { to: '/notifications', label: 'Сповіщення', icon: '🔔' },
  ],
  TEACHER: [
    { to: '/teacher', label: 'Мої дисципліни', icon: '📚' },
    { to: '/teacher/schedule', label: 'Розклад', icon: '🗓' },
    { to: '/teacher/statistics', label: 'Статистика', icon: '📊' },
    { to: '/notifications', label: 'Сповіщення', icon: '🔔' },
  ],
  ADMIN: [{ to: '/admin', label: 'Огляд', icon: '🏛' }],
};

const ROLE_TITLES: Record<Role, string> = {
  ADMIN: 'Адміністратор',
  TEACHER: 'Викладач',
  STUDENT: 'Студент',
};

export function Layout({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth();
  const { path } = useRouter();
  const [unread, setUnread] = useState(0);
  const [menuOpen, setMenuOpen] = useState(false);

  useEffect(() => {
    if (!user) return;
    api.get<{ count: number }>('/notifications/me/unread-count')
      .then((r) => setUnread(r.count))
      .catch(() => setUnread(0));
  }, [user, path]);

  useEffect(() => setMenuOpen(false), [path]);

  if (!user) return <>{children}</>;
  const items = NAVIGATION[user.role];
  const isActive = (to: string) => path === to || (to.split('/').length > 2 && path.startsWith(`${to}/`));

  return (
    <div className="app">
      <aside className={`sidebar ${menuOpen ? 'open' : ''}`}>
        <div className="brand">
          <img src="/favicon.svg" alt="" width={32} height={32} />
          <div>
            <div className="brand-title">Облік студентів</div>
            <div className="brand-sub">НУ «Львівська політехніка»</div>
          </div>
        </div>
        <nav>
          {items.map((item) => (
            <Link key={item.to} to={item.to} className={`nav-item ${isActive(item.to) ? 'active' : ''}`}>
              <span className="nav-icon" aria-hidden="true">{item.icon}</span>
              {item.label}
              {item.to === '/notifications' && unread > 0 && <span className="nav-count">{unread}</span>}
            </Link>
          ))}
        </nav>
      </aside>
      <div className="main">
        <header className="topbar">
          <button type="button" className="menu-toggle" onClick={() => setMenuOpen((v) => !v)} aria-label="Меню">
            ☰
          </button>
          <div className="topbar-spacer" />
          <div className="user-chip">
            <span className="avatar">{initials(user.fullName)}</span>
            <div>
              <div className="user-name">{user.fullName}</div>
              <div className="user-role">{ROLE_TITLES[user.role]}</div>
            </div>
          </div>
          <button type="button" className="btn btn-ghost" onClick={logout}>
            Вийти
          </button>
        </header>
        <main className="content">{children}</main>
      </div>
    </div>
  );
}
