package ua.lpnu.studentrecords.repository;

import ua.lpnu.studentrecords.domain.enums.StudentStatus;

/**
 * Критерії пошуку студентів. Усі поля необов'язкові: {@code null} означає «без фільтра».
 *
 * @param facultyId   факультет
 * @param specialtyId спеціальність
 * @param groupId     група
 * @param course      курс
 * @param status      статус
 * @param query       фрагмент ПІБ або номера залікової книжки
 */
public record StudentSearchCriteria(Long facultyId,
                                    Long specialtyId,
                                    Long groupId,
                                    Integer course,
                                    StudentStatus status,
                                    String query) {

    public static StudentSearchCriteria empty() {
        return new StudentSearchCriteria(null, null, null, null, null, null);
    }
}
