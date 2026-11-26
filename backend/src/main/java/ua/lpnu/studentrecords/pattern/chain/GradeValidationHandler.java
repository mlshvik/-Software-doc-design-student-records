package ua.lpnu.studentrecords.pattern.chain;

import ua.lpnu.studentrecords.domain.exception.GradeValidationException;

/**
 * Обробник у ланцюжку перевірок перед збереженням оцінки.
 * <p>
 * <b>Шаблон Chain of Responsibility (роль Handler).</b> Кожне правило (діапазон балів,
 * статус студента, право викладача, порядок перескладань) – окремий обробник, що знає лише
 * про наступну ланку. Запит проходить ланцюжком, доки якийсь обробник його не відхилить
 * (виняток {@link GradeValidationException}) або ланцюжок не закінчиться. Правила можна
 * додавати, прибирати й переставляти в {@link GradeValidationChainBuilder}, не змінюючи
 * {@code GradeService}.
 */
public abstract class GradeValidationHandler {

    private GradeValidationHandler next;

    /**
     * Задає наступну ланку.
     *
     * @return переданий обробник – для зручного побудування ланцюжка {@code a.setNext(b).setNext(c)}
     */
    public GradeValidationHandler setNext(GradeValidationHandler next) {
        this.next = next;
        return next;
    }

    public GradeValidationHandler getNext() {
        return next;
    }

    /** Виконує перевірку цієї ланки і передає запит далі. */
    public final void handle(GradeValidationContext context) {
        check(context);
        if (next != null) {
            next.handle(context);
        }
    }

    /**
     * Перевірка конкретного правила.
     *
     * @throws GradeValidationException якщо правило порушено
     */
    protected abstract void check(GradeValidationContext context);

    /** Відхиляє запит з повідомленням для користувача. */
    protected void reject(String message) {
        throw new GradeValidationException(getClass().getSimpleName(), message);
    }
}
