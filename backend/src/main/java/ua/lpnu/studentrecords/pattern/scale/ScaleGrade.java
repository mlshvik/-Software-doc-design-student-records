package ua.lpnu.studentrecords.pattern.scale;

/**
 * Результат переведення 100-бальної оцінки в інші шкали.
 *
 * @param score    бал за 100-бальною шкалою
 * @param national оцінка за національною шкалою («відмінно», «зараховано» тощо)
 * @param ects     оцінка за шкалою ЄКТС (A–F, FX)
 * @param passed   чи є оцінка позитивною
 */
public record ScaleGrade(int score, String national, String ects, boolean passed) {
}
