import { api } from '../../api/client';
import type { StudentRecord } from '../../api/types';
import { useAuth } from '../../auth/AuthContext';
import { useApi } from '../../utils/useApi';

/** Картка поточного студента (оцінки, середній бал, борги, розклад) – одним запитом до фасаду. */
export function useStudentRecord() {
  const { user } = useAuth();
  const studentId = user?.studentId;
  return useApi<StudentRecord>(() => {
    if (!studentId) return Promise.reject(new Error('Обліковий запис не пов\'язаний зі студентом'));
    return api.get<StudentRecord>(`/students/${studentId}/record`);
  }, [studentId]);
}
