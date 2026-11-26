package ua.lpnu.studentrecords.pattern.observer;

/**
 * Спостерігач за виставленням оцінок.
 * <p>
 * <b>Шаблон Observer (роль Observer).</b> Після збереження оцінки треба відкрити або закрити
 * академічну заборгованість, повідомити студента, перерахувати середній бал. Замість того щоб
 * {@code GradeService} знав про всі ці дії, кожна з них – окремий спостерігач, підписаний на
 * {@link GradeEventPublisher}. Нова реакція на оцінку = новий клас, що реалізує цей інтерфейс.
 */
public interface GradeEventListener {

    void onGradeRecorded(GradeRecordedEvent event);
}
