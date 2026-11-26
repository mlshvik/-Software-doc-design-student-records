package ua.lpnu.studentrecords.pattern.observer;

import org.springframework.stereotype.Component;
import ua.lpnu.studentrecords.domain.entity.Grade;
import ua.lpnu.studentrecords.domain.entity.Notification;
import ua.lpnu.studentrecords.domain.entity.Subject;
import ua.lpnu.studentrecords.domain.enums.GradeAttempt;
import ua.lpnu.studentrecords.pattern.scale.GradingScale;
import ua.lpnu.studentrecords.pattern.scale.ScaleGrade;
import ua.lpnu.studentrecords.repository.NotificationRepository;

/**
 * Створює сповіщення студенту про нову оцінку (якщо в студента є обліковий запис).
 * <p>
 * <b>Шаблон Observer (роль ConcreteObserver).</b>
 */
@Component
public class NotificationObserver implements GradeEventListener {

    private final NotificationRepository notificationRepository;

    public NotificationObserver(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void onGradeRecorded(GradeRecordedEvent event) {
        if (event.student().getUser() == null) {
            return;
        }
        notificationRepository.save(new Notification(event.student().getUser(), buildMessage(event)));
    }

    String buildMessage(GradeRecordedEvent event) {
        Grade grade = event.grade();
        Subject subject = event.assignment().getSubject();
        ScaleGrade scale = GradingScale.INSTANCE.convert(grade.getScore(), subject.getControlType());
        StringBuilder message = new StringBuilder()
                .append("Нова оцінка з дисципліни «").append(subject.getName()).append("»")
                .append(attemptSuffix(grade.getAttempt()))
                .append(": ").append(grade.getScore()).append(" балів – ")
                .append(scale.national()).append(" (").append(scale.ects()).append(").");
        if (!scale.passed()) {
            message.append(" Відкрито академічну заборгованість.");
        }
        return message.toString();
    }

    private static String attemptSuffix(GradeAttempt attempt) {
        return switch (attempt) {
            case REGULAR -> "";
            case RETAKE_TEACHER -> " (перескладання викладачу)";
            case RETAKE_COMMISSION -> " (перескладання комісії)";
        };
    }
}
