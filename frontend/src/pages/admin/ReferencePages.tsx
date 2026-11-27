import type { Assignment, Faculty, Group, Specialty, Subject, Teacher } from '../../api/types';
import { CrudPage, type CrudConfig } from '../../components/CrudPage';
import { num, str } from '../../components/FormFields';
import { Badge } from '../../components/ui';
import { CONTROL_TITLES, formatNumber } from '../../utils/format';
import {
  controlTypeOptions,
  facultyOptions,
  groupOptions,
  specialtyOptions,
  subjectOptions,
  teacherOptions,
} from './lookups';

const faculties: CrudConfig<Faculty> = {
  title: 'Факультети (інститути)',
  endpoint: '/faculties',
  entityName: 'Факультет',
  columns: [
    { title: 'Скорочено', render: (f) => <strong>{f.shortName}</strong> },
    { title: 'Назва', render: (f) => f.name },
  ],
  fields: [
    { name: 'name', label: 'Назва', required: true, wide: true },
    { name: 'shortName', label: 'Скорочена назва', required: true },
  ],
  toForm: (f) => ({ name: f.name, shortName: f.shortName }),
  toRequest: (v) => ({ name: str(v.name), shortName: str(v.shortName) }),
  searchText: (f) => `${f.name} ${f.shortName}`,
  deleteLabel: (f) => `факультет «${f.shortName}»`,
};

const specialties: CrudConfig<Specialty> = {
  title: 'Спеціальності',
  endpoint: '/specialties',
  entityName: 'Спеціальність',
  columns: [
    { title: 'Код', render: (s) => <strong>{s.code}</strong> },
    { title: 'Назва', render: (s) => s.name },
    { title: 'Факультет', render: (s) => s.facultyShortName },
  ],
  fields: [
    { name: 'code', label: 'Код', required: true, placeholder: '121' },
    { name: 'facultyId', label: 'Факультет', type: 'select', required: true, options: facultyOptions },
    { name: 'name', label: 'Назва', required: true, wide: true },
  ],
  toForm: (s) => ({ code: s.code, name: s.name, facultyId: String(s.facultyId) }),
  toRequest: (v) => ({ code: str(v.code), name: str(v.name), facultyId: num(v.facultyId) }),
  searchText: (s) => `${s.code} ${s.name} ${s.facultyShortName}`,
  deleteLabel: (s) => `спеціальність ${s.code}`,
};

const groups: CrudConfig<Group> = {
  title: 'Академічні групи',
  endpoint: '/groups',
  entityName: 'Група',
  columns: [
    { title: 'Група', render: (g) => <span className="group-tag">{g.name}</span> },
    { title: 'Курс', render: (g) => g.course, numeric: true },
    { title: 'Рік вступу', render: (g) => g.admissionYear, numeric: true },
    { title: 'Спеціальність', render: (g) => `${g.specialtyCode} ${g.specialtyName}` },
    { title: 'Факультет', render: (g) => g.facultyShortName },
  ],
  fields: [
    { name: 'name', label: 'Назва групи', required: true, placeholder: 'ІР-43' },
    { name: 'specialtyId', label: 'Спеціальність', type: 'select', required: true, options: specialtyOptions },
    { name: 'course', label: 'Курс', type: 'number', required: true },
    { name: 'admissionYear', label: 'Рік вступу', type: 'number', required: true },
  ],
  toForm: (g) => ({ name: g.name, specialtyId: String(g.specialtyId), course: String(g.course), admissionYear: String(g.admissionYear) }),
  toRequest: (v) => ({ name: str(v.name), specialtyId: num(v.specialtyId), course: num(v.course), admissionYear: num(v.admissionYear) }),
  searchText: (g) => `${g.name} ${g.specialtyName} ${g.facultyShortName}`,
  deleteLabel: (g) => `групу ${g.name}`,
};

const subjects: CrudConfig<Subject> = {
  title: 'Дисципліни',
  endpoint: '/subjects',
  entityName: 'Дисципліна',
  columns: [
    { title: 'Назва', render: (s) => s.name },
    { title: 'Кредити ЄКТС', render: (s) => formatNumber(s.credits), numeric: true },
    { title: 'Годин', render: (s) => s.hours, numeric: true },
    { title: 'Контроль', render: (s) => <Badge tone="info">{CONTROL_TITLES[s.controlType]}</Badge> },
  ],
  fields: [
    { name: 'name', label: 'Назва', required: true, wide: true },
    { name: 'credits', label: 'Кредити ЄКТС', type: 'number', step: '0.5', required: true },
    { name: 'hours', label: 'Годин', type: 'number', required: true },
    { name: 'controlType', label: 'Форма контролю', type: 'select', required: true, options: controlTypeOptions },
  ],
  toForm: (s) => ({ name: s.name, credits: String(s.credits), hours: String(s.hours), controlType: s.controlType }),
  toRequest: (v) => ({ name: str(v.name), credits: num(v.credits), hours: num(v.hours), controlType: str(v.controlType) }),
  searchText: (s) => s.name,
  deleteLabel: (s) => `дисципліну «${s.name}»`,
};

const teachers: CrudConfig<Teacher> = {
  title: 'Викладачі',
  endpoint: '/teachers',
  entityName: 'Викладач',
  columns: [
    { title: 'ПІБ', render: (t) => <strong>{t.fullName}</strong> },
    { title: 'Посада', render: (t) => t.position ?? '—' },
    { title: 'Ступінь', render: (t) => t.academicDegree ?? '—' },
    { title: 'Кафедра', render: (t) => t.department ?? '—' },
    { title: 'Логін', render: (t) => t.username ?? <span className="muted">немає</span> },
  ],
  fields: [
    { name: 'lastName', label: 'Прізвище', required: true },
    { name: 'firstName', label: "Ім'я", required: true },
    { name: 'middleName', label: 'По батькові' },
    { name: 'email', label: 'E-mail', type: 'email' },
    { name: 'position', label: 'Посада', placeholder: 'доцент' },
    { name: 'academicDegree', label: 'Науковий ступінь', placeholder: 'к.т.н.' },
    { name: 'department', label: 'Кафедра', wide: true },
  ],
  toForm: (t) => ({
    lastName: t.lastName,
    firstName: t.firstName,
    middleName: t.middleName ?? '',
    email: t.email ?? '',
    position: t.position ?? '',
    academicDegree: t.academicDegree ?? '',
    department: t.department ?? '',
  }),
  toRequest: (v) => ({
    lastName: str(v.lastName),
    firstName: str(v.firstName),
    middleName: str(v.middleName),
    email: str(v.email),
    position: str(v.position),
    academicDegree: str(v.academicDegree),
    department: str(v.department),
  }),
  searchText: (t) => `${t.fullName} ${t.department ?? ''}`,
  deleteLabel: (t) => `викладача ${t.fullName}`,
};

const assignments: CrudConfig<Assignment> = {
  title: 'Призначення викладачів',
  subtitle: 'Яку дисципліну, хто і в якій групі веде в семестрі',
  endpoint: '/assignments',
  entityName: 'Призначення',
  columns: [
    { title: 'Рік', render: (a) => a.academicYear },
    { title: 'Сем.', render: (a) => a.semester, numeric: true },
    { title: 'Група', render: (a) => <span className="group-tag">{a.groupName}</span> },
    { title: 'Дисципліна', render: (a) => a.subjectName },
    { title: 'Контроль', render: (a) => CONTROL_TITLES[a.controlType] },
    { title: 'Викладач', render: (a) => a.teacherName },
  ],
  fields: [
    { name: 'subjectId', label: 'Дисципліна', type: 'select', required: true, options: subjectOptions, wide: true },
    { name: 'teacherId', label: 'Викладач', type: 'select', required: true, options: teacherOptions },
    { name: 'groupId', label: 'Група', type: 'select', required: true, options: groupOptions },
    { name: 'semester', label: 'Семестр', type: 'number', required: true },
    { name: 'academicYear', label: 'Навчальний рік', required: true, placeholder: '2026/2027' },
  ],
  toForm: (a) => ({
    subjectId: String(a.subjectId),
    teacherId: String(a.teacherId),
    groupId: String(a.groupId),
    semester: String(a.semester),
    academicYear: a.academicYear,
  }),
  toRequest: (v) => ({
    subjectId: num(v.subjectId),
    teacherId: num(v.teacherId),
    groupId: num(v.groupId),
    semester: num(v.semester),
    academicYear: str(v.academicYear),
  }),
  searchText: (a) => `${a.subjectName} ${a.teacherName} ${a.groupName} ${a.academicYear}`,
  deleteLabel: (a) => `призначення «${a.subjectName}» для ${a.groupName}`,
};

export const FacultiesPage = () => <CrudPage config={faculties} />;
export const SpecialtiesPage = () => <CrudPage config={specialties} />;
export const GroupsPage = () => <CrudPage config={groups} />;
export const SubjectsPage = () => <CrudPage config={subjects} />;
export const TeachersPage = () => <CrudPage config={teachers} />;
export const AssignmentsPage = () => <CrudPage config={assignments} />;
