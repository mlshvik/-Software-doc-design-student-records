import { useEffect } from 'react';
import type { Role } from './api/types';
import { homePath, useAuth } from './auth/AuthContext';
import { Layout } from './components/Layout';
import { Loader } from './components/ui';
import { LoginPage } from './pages/LoginPage';
import { NotFoundPage } from './pages/NotFoundPage';
import { NotificationsPage } from './pages/NotificationsPage';
import { AdminHome } from './pages/admin/AdminHome';
import { DebtsPage } from './pages/admin/DebtsPage';
import {
  AssignmentsPage,
  FacultiesPage,
  GroupsPage,
  SpecialtiesPage,
  SubjectsPage,
  TeachersPage,
} from './pages/admin/ReferencePages';
import { ReportsPage } from './pages/admin/ReportsPage';
import { ScheduleBuilderPage } from './pages/admin/ScheduleBuilderPage';
import { SettingsPage } from './pages/admin/SettingsPage';
import { StudentCardPage } from './pages/admin/StudentCardPage';
import { StudentsPage } from './pages/admin/StudentsPage';
import { StudentDebts } from './pages/student/StudentDebts';
import { StudentGrades } from './pages/student/StudentGrades';
import { StudentHome } from './pages/student/StudentHome';
import { StudentSchedule } from './pages/student/StudentSchedule';
import { GradeSheetPage } from './pages/teacher/GradeSheetPage';
import { TeacherHome } from './pages/teacher/TeacherHome';
import { TeacherSchedule } from './pages/teacher/TeacherSchedule';
import { TeacherStatistics } from './pages/teacher/TeacherStatistics';
import { Routes, useRouter, type RouteDef } from './router';

/** Маршрути, доступні кожній ролі. */
const ROUTES: Record<Role, RouteDef[]> = {
  STUDENT: [
    { path: '/student', render: () => <StudentHome /> },
    { path: '/student/grades', render: () => <StudentGrades /> },
    { path: '/student/schedule', render: () => <StudentSchedule /> },
    { path: '/student/debts', render: () => <StudentDebts /> },
    { path: '/notifications', render: () => <NotificationsPage /> },
  ],
  TEACHER: [
    { path: '/teacher', render: () => <TeacherHome /> },
    { path: '/teacher/sheet/:id', render: (p) => <GradeSheetPage assignmentId={Number(p.id)} /> },
    { path: '/teacher/schedule', render: () => <TeacherSchedule /> },
    { path: '/teacher/statistics', render: () => <TeacherStatistics /> },
    { path: '/notifications', render: () => <NotificationsPage /> },
  ],
  ADMIN: [
    { path: '/admin', render: () => <AdminHome /> },
    { path: '/admin/students', render: () => <StudentsPage /> },
    { path: '/admin/students/:id', render: (p) => <StudentCardPage studentId={Number(p.id)} /> },
    { path: '/admin/teachers', render: () => <TeachersPage /> },
    { path: '/admin/groups', render: () => <GroupsPage /> },
    { path: '/admin/subjects', render: () => <SubjectsPage /> },
    { path: '/admin/assignments', render: () => <AssignmentsPage /> },
    { path: '/admin/schedule', render: () => <ScheduleBuilderPage /> },
    { path: '/admin/debts', render: () => <DebtsPage /> },
    { path: '/admin/reports', render: () => <ReportsPage /> },
    { path: '/admin/specialties', render: () => <SpecialtiesPage /> },
    { path: '/admin/faculties', render: () => <FacultiesPage /> },
    { path: '/admin/settings', render: () => <SettingsPage /> },
    { path: '/notifications', render: () => <NotificationsPage /> },
  ],
};

function Redirect({ to }: { to: string }): null {
  const { navigate } = useRouter();
  useEffect(() => navigate(to, { replace: true }), [navigate, to]);
  return null;
}

export function App() {
  const { user, loading } = useAuth();
  const { path } = useRouter();

  if (loading) return <Loader text="Перевірка сесії…" />;
  if (!user) {
    return path === '/login' ? <LoginPage /> : <Redirect to="/login" />;
  }
  if (path === '/' || path === '/login') return <Redirect to={homePath(user)} />;

  return (
    <Layout>
      <Routes routes={ROUTES[user.role]} fallback={<NotFoundPage />} />
    </Layout>
  );
}
