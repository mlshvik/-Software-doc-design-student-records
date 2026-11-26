package ua.lpnu.studentrecords.domain.exception;

import ua.lpnu.studentrecords.domain.enums.StudentStatus;

/**
 * Недопустимий перехід між статусами студента (шаблон State),
 * наприклад, з «випускник» у «академічна відпустка».
 */
public class InvalidStatusTransitionException extends BusinessException {

    private final StudentStatus from;
    private final StudentStatus to;

    public InvalidStatusTransitionException(StudentStatus from, StudentStatus to) {
        super("Недопустимий перехід статусу студента: " + from + " → " + to);
        this.from = from;
        this.to = to;
    }

    public StudentStatus getFrom() {
        return from;
    }

    public StudentStatus getTo() {
        return to;
    }
}
