import { api } from '../../api/client';
import type { Assignment, Faculty, Group, Specialty, Subject, Teacher } from '../../api/types';
import type { Option } from '../../components/FormFields';
import { CONTROL_TITLES, DAYS, DAY_TITLES, LESSON_TITLES, STATUS_TITLES, WEEK_TITLES } from '../../utils/format';

// Завантажувачі варіантів для випадних списків (стабільні посилання – не створюються при кожному рендері).

export const facultyOptions = (): Promise<Option[]> =>
  api.get<Faculty[]>('/faculties').then((list) => list.map((f) => ({ value: String(f.id), label: `${f.shortName} – ${f.name}` })));

export const specialtyOptions = (): Promise<Option[]> =>
  api.get<Specialty[]>('/specialties').then((list) => list.map((s) => ({ value: String(s.id), label: `${s.code} ${s.name} (${s.facultyShortName})` })));

export const groupOptions = (): Promise<Option[]> =>
  api.get<Group[]>('/groups').then((list) => list.map((g) => ({ value: String(g.id), label: `${g.name} (${g.course} курс)` })));

export const subjectOptions = (): Promise<Option[]> =>
  api.get<Subject[]>('/subjects').then((list) => list.map((s) => ({ value: String(s.id), label: s.name })));

export const teacherOptions = (): Promise<Option[]> =>
  api.get<Teacher[]>('/teachers').then((list) => list.map((t) => ({ value: String(t.id), label: t.fullName })));

export const assignmentLabel = (a: Assignment) => `${a.subjectName} – ${a.teacherName} (${a.semester} сем., ${a.academicYear})`;

export const controlTypeOptions: Option[] = Object.entries(CONTROL_TITLES).map(([value, label]) => ({ value, label }));
export const statusOptions: Option[] = Object.entries(STATUS_TITLES).map(([value, label]) => ({ value, label }));
export const dayOptions: Option[] = DAYS.map((d) => ({ value: d, label: DAY_TITLES[d] }));
export const weekOptions: Option[] = Object.entries(WEEK_TITLES).map(([value, label]) => ({ value, label }));
export const lessonOptions: Option[] = Object.entries(LESSON_TITLES).map(([value, label]) => ({ value, label }));
export const courseOptions: Option[] = [1, 2, 3, 4, 5, 6].map((c) => ({ value: String(c), label: `${c} курс` }));
