package ua.lpnu.studentrecords.pattern.facade;

import ua.lpnu.studentrecords.domain.enums.ControlType;
import ua.lpnu.studentrecords.domain.enums.DebtStatus;
import ua.lpnu.studentrecords.domain.enums.GradeAttempt;
import ua.lpnu.studentrecords.domain.enums.LessonType;
import ua.lpnu.studentrecords.domain.enums.StudentStatus;
import ua.lpnu.studentrecords.domain.enums.WeekType;
import ua.lpnu.studentrecords.service.GpaResult;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Зведені дані для кабінету студента, які повертає {@link StudentRecordFacade}.
 *
 * @param student   загальні відомості про студента
 * @param grades    усі оцінки (включно з перескладаннями)
 * @param gpa       середній бал
 * @param debts     академічні заборгованості
 * @param openDebts кількість відкритих заборгованостей
 * @param schedule  розклад групи
 */
public record StudentRecordDto(StudentInfo student,
                               List<GradeItem> grades,
                               GpaResult gpa,
                               List<DebtItem> debts,
                               long openDebts,
                               List<ScheduleItem> schedule) {

    public record StudentInfo(Long id,
                              String fullName,
                              String recordBookNumber,
                              String groupName,
                              Integer course,
                              String specialty,
                              String faculty,
                              StudentStatus status,
                              String statusTitle) {
    }

    /**
     * @param finalResult чи є ця оцінка підсумковою з дисципліни (найкраща спроба)
     */
    public record GradeItem(Long id,
                            Long assignmentId,
                            String subject,
                            ControlType controlType,
                            BigDecimal credits,
                            Integer semester,
                            String academicYear,
                            String teacher,
                            Integer score,
                            GradeAttempt attempt,
                            String national,
                            String ects,
                            boolean passed,
                            boolean finalResult,
                            LocalDateTime gradedAt) {
    }

    public record DebtItem(Long id,
                           Long assignmentId,
                           String subject,
                           Integer semester,
                           String academicYear,
                           String teacher,
                           DebtStatus status,
                           LocalDateTime openedAt,
                           LocalDateTime closedAt) {
    }

    public record ScheduleItem(Long id,
                               DayOfWeek dayOfWeek,
                               Integer pairNumber,
                               WeekType weekType,
                               String subject,
                               String teacher,
                               String classroom,
                               LessonType lessonType) {
    }
}
