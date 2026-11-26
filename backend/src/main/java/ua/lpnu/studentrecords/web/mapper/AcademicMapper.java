package ua.lpnu.studentrecords.web.mapper;

import ua.lpnu.studentrecords.domain.entity.AcademicDebt;
import ua.lpnu.studentrecords.domain.entity.Grade;
import ua.lpnu.studentrecords.domain.entity.Notification;
import ua.lpnu.studentrecords.domain.entity.ScheduleEntry;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.entity.StudyGroup;
import ua.lpnu.studentrecords.domain.entity.Subject;
import ua.lpnu.studentrecords.domain.entity.SystemSetting;
import ua.lpnu.studentrecords.domain.entity.Teacher;
import ua.lpnu.studentrecords.domain.entity.TeachingAssignment;
import ua.lpnu.studentrecords.pattern.scale.GradingScale;
import ua.lpnu.studentrecords.pattern.scale.ScaleGrade;
import ua.lpnu.studentrecords.service.schedule.ScheduleConflict;
import ua.lpnu.studentrecords.web.dto.AssignmentResponse;
import ua.lpnu.studentrecords.web.dto.DebtResponse;
import ua.lpnu.studentrecords.web.dto.GradeResponse;
import ua.lpnu.studentrecords.web.dto.NotificationResponse;
import ua.lpnu.studentrecords.web.dto.ScheduleConflictResponse;
import ua.lpnu.studentrecords.web.dto.ScheduleEntryResponse;
import ua.lpnu.studentrecords.web.dto.SettingResponse;

/**
 * Перетворення сутностей навчального процесу (призначення, оцінки, борги, розклад,
 * сповіщення, налаштування) у DTO.
 * <p>
 * Оцінка доповнюється національною шкалою та ЄКТС через Singleton {@link GradingScale}.
 */
public final class AcademicMapper {

    private AcademicMapper() {
    }

    public static AssignmentResponse toResponse(TeachingAssignment assignment) {
        Subject subject = assignment.getSubject();
        Teacher teacher = assignment.getTeacher();
        StudyGroup group = assignment.getGroup();
        return new AssignmentResponse(
                assignment.getId(),
                subject.getId(),
                subject.getName(),
                subject.getControlType(),
                subject.getCredits(),
                teacher.getId(),
                teacher.getShortName(),
                group.getId(),
                group.getName(),
                group.getCourse(),
                assignment.getSemester(),
                assignment.getAcademicYear());
    }

    public static GradeResponse toResponse(Grade grade) {
        TeachingAssignment assignment = grade.getAssignment();
        Subject subject = assignment.getSubject();
        Student student = grade.getStudent();
        ScaleGrade scale = GradingScale.INSTANCE.convert(grade.getScore(), subject.getControlType());
        Teacher gradedBy = grade.getGradedBy();
        return new GradeResponse(
                grade.getId(),
                student.getId(),
                student.getFullName(),
                assignment.getId(),
                subject.getName(),
                grade.getScore(),
                grade.getAttempt(),
                scale.national(),
                scale.ects(),
                scale.passed(),
                grade.getGradedAt(),
                gradedBy == null ? null : gradedBy.getShortName());
    }

    public static DebtResponse toResponse(AcademicDebt debt) {
        Student student = debt.getStudent();
        TeachingAssignment assignment = debt.getAssignment();
        return new DebtResponse(
                debt.getId(),
                student.getId(),
                student.getFullName(),
                student.getGroup().getName(),
                assignment.getId(),
                assignment.getSubject().getName(),
                assignment.getTeacher().getShortName(),
                assignment.getSemester(),
                assignment.getAcademicYear(),
                debt.getStatus(),
                debt.getOpenedAt(),
                debt.getClosedAt());
    }

    public static ScheduleEntryResponse toResponse(ScheduleEntry entry) {
        TeachingAssignment assignment = entry.getAssignment();
        Teacher teacher = assignment.getTeacher();
        StudyGroup group = assignment.getGroup();
        return new ScheduleEntryResponse(
                entry.getId(),
                assignment.getId(),
                entry.getDayOfWeek(),
                entry.getPairNumber(),
                entry.getWeekType(),
                entry.getClassroom(),
                entry.getLessonType(),
                assignment.getSubject().getName(),
                teacher.getId(),
                teacher.getShortName(),
                group.getId(),
                group.getName());
    }

    public static ScheduleConflictResponse toResponse(ScheduleConflict conflict) {
        return new ScheduleConflictResponse(conflict.type(), conflict.existing().getId(), conflict.message());
    }

    public static NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getMessage(),
                notification.getCreatedAt(), notification.isRead());
    }

    public static SettingResponse toResponse(SystemSetting setting) {
        return new SettingResponse(setting.getKey(), setting.getValue(), setting.getDescription());
    }
}
