// Типи даних REST API бекенду (відповідають DTO у пакеті web.dto).

export type Role = 'ADMIN' | 'TEACHER' | 'STUDENT';
export type StudentStatus = 'ACTIVE' | 'ACADEMIC_LEAVE' | 'EXPELLED' | 'GRADUATED';
export type ControlType = 'EXAM' | 'CREDIT' | 'DIFF_CREDIT';
export type GradeAttempt = 'REGULAR' | 'RETAKE_TEACHER' | 'RETAKE_COMMISSION';
export type DebtStatus = 'OPEN' | 'CLOSED';
export type WeekType = 'ALL' | 'ODD' | 'EVEN';
export type LessonType = 'LECTURE' | 'PRACTICE' | 'LAB';
export type DayOfWeek = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';
export type GpaStrategyType = 'SIMPLE' | 'CREDIT_WEIGHTED';

export interface CurrentUser {
  id: number;
  username: string;
  role: Role;
  fullName: string;
  studentId: number | null;
  teacherId: number | null;
}

export interface LoginResponse {
  token: string;
  tokenType: string;
  expiresAt: string;
  user: CurrentUser;
}

export interface GpaResult {
  value: number;
  ects: string | null;
  strategy: GpaStrategyType;
  subjectsCounted: number;
  totalCredits: number;
}

export interface StudentRecord {
  student: {
    id: number;
    fullName: string;
    recordBookNumber: string;
    groupName: string;
    course: number;
    specialty: string;
    faculty: string;
    status: StudentStatus;
    statusTitle: string;
  };
  grades: RecordGrade[];
  gpa: GpaResult;
  debts: RecordDebt[];
  openDebts: number;
  schedule: RecordScheduleItem[];
}

export interface RecordGrade {
  id: number;
  assignmentId: number;
  subject: string;
  controlType: ControlType;
  credits: number;
  semester: number;
  academicYear: string;
  teacher: string;
  score: number;
  attempt: GradeAttempt;
  national: string;
  ects: string;
  passed: boolean;
  finalResult: boolean;
  gradedAt: string;
}

export interface RecordDebt {
  id: number;
  assignmentId: number;
  subject: string;
  semester: number;
  academicYear: string;
  teacher: string;
  status: DebtStatus;
  openedAt: string;
  closedAt: string | null;
}

export interface RecordScheduleItem {
  id: number;
  dayOfWeek: DayOfWeek;
  pairNumber: number;
  weekType: WeekType;
  subject: string;
  teacher: string;
  classroom: string;
  lessonType: LessonType;
}

export interface ScheduleEntry {
  id: number;
  assignmentId: number;
  dayOfWeek: DayOfWeek;
  pairNumber: number;
  weekType: WeekType;
  classroom: string;
  lessonType: LessonType;
  subjectName: string;
  teacherId: number;
  teacherName: string;
  groupId: number;
  groupName: string;
}

export interface ScheduleEntryRequest {
  assignmentId: number;
  dayOfWeek: DayOfWeek;
  pairNumber: number;
  weekType: WeekType;
  classroom: string;
  lessonType: LessonType;
}

export interface ScheduleConflict {
  type: string;
  existingEntryId: number;
  message: string;
}

export interface Assignment {
  id: number;
  subjectId: number;
  subjectName: string;
  controlType: ControlType;
  credits: number;
  teacherId: number;
  teacherName: string;
  groupId: number;
  groupName: string;
  course: number;
  semester: number;
  academicYear: string;
}

export interface Grade {
  id: number;
  studentId: number;
  studentName: string;
  assignmentId: number;
  subjectName: string;
  score: number;
  attempt: GradeAttempt;
  national: string;
  ects: string;
  passed: boolean;
  gradedAt: string;
  gradedBy: string;
}

export interface GradeSheetRow {
  studentId: number;
  studentName: string;
  recordBookNumber: string;
  status: StudentStatus;
  canBeGraded: boolean;
  grades: Grade[];
  finalScore: number | null;
  finalEcts: string | null;
  nextAttempt: GradeAttempt | null;
}

export interface GradeSheet {
  assignment: Assignment;
  rows: GradeSheetRow[];
}

export interface Notification {
  id: number;
  message: string;
  createdAt: string;
  read: boolean;
}

export interface ReportColumn {
  key: string;
  title: string;
  numeric: boolean;
}

export interface Report {
  type: string;
  title: string;
  generatedAt: string;
  parameters: Record<string, unknown>;
  columns: ReportColumn[];
  rows: Record<string, unknown>[];
  summary: Record<string, unknown>;
}

export interface ReportTypeInfo {
  type: string;
  slug: string;
  title: string;
}

export interface Faculty {
  id: number;
  name: string;
  shortName: string;
}

export interface Specialty {
  id: number;
  code: string;
  name: string;
  facultyId: number;
  facultyShortName: string;
}

export interface Group {
  id: number;
  name: string;
  course: number;
  admissionYear: number;
  specialtyId: number;
  specialtyCode: string;
  specialtyName: string;
  facultyId: number;
  facultyShortName: string;
}

export interface Subject {
  id: number;
  name: string;
  credits: number;
  hours: number;
  controlType: ControlType;
}

export interface Teacher {
  id: number;
  lastName: string;
  firstName: string;
  middleName: string | null;
  fullName: string;
  shortName: string;
  position: string | null;
  academicDegree: string | null;
  department: string | null;
  email: string | null;
  username: string | null;
}

export interface Student {
  id: number;
  lastName: string;
  firstName: string;
  middleName: string | null;
  fullName: string;
  recordBookNumber: string;
  email: string | null;
  phone: string | null;
  birthDate: string | null;
  groupId: number;
  groupName: string;
  course: number;
  status: StudentStatus;
  statusTitle: string;
  username: string | null;
}

export interface Debt {
  id: number;
  studentId: number;
  studentName: string;
  groupName: string;
  assignmentId: number;
  subjectName: string;
  teacherName: string;
  semester: number;
  academicYear: string;
  status: DebtStatus;
  openedAt: string;
  closedAt: string | null;
}

export interface Setting {
  key: string;
  value: string;
  description: string | null;
}

/** Відповідь з помилкою у форматі RFC 7807 (Problem Details). */
export interface ProblemDetail {
  status?: number;
  title?: string;
  detail?: string;
  errors?: Record<string, string>;
  conflicts?: ScheduleConflict[];
}
