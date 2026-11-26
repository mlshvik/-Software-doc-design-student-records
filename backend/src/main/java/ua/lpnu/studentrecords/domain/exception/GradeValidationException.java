package ua.lpnu.studentrecords.domain.exception;

/**
 * Оцінку не можна виставити: її відхилив один з обробників
 * ланцюжка валідації (шаблон Chain of Responsibility).
 */
public class GradeValidationException extends BusinessException {

    private final String rule;

    public GradeValidationException(String rule, String message) {
        super(message);
        this.rule = rule;
    }

    /** Назва правила (обробника), яке відхилило оцінку. */
    public String getRule() {
        return rule;
    }
}
