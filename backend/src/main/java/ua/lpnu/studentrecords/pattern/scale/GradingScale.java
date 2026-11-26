package ua.lpnu.studentrecords.pattern.scale;

import ua.lpnu.studentrecords.domain.enums.ControlType;

/**
 * Шкала оцінювання: переведення 100-бальної оцінки в національну шкалу та шкалу ЄКТС.
 * <p>
 * <b>Шаблон Singleton.</b> Правила переведення однакові для всієї системи і не мають
 * змінного стану, тому існує рівно один екземпляр шкали – {@link #INSTANCE}.
 * Реалізація через {@code enum} (за Дж. Блохом) гарантує єдиність екземпляра JVM,
 * потокобезпечну ліниву ініціалізацію та захист від створення копій через
 * рефлексію чи серіалізацію.
 *
 * <pre>
 * 90–100 – відмінно (A)      64–73 – задовільно (D)
 * 82–89  – добре (B)         60–63 – задовільно (E)
 * 74–81  – добре (C)         35–59 – незадовільно (FX)
 *                             0–34 – незадовільно (F)
 * </pre>
 * Для заліку ({@link ControlType#CREDIT}) національна оцінка – «зараховано» / «не зараховано».
 */
public enum GradingScale {

    /** Єдиний екземпляр шкали. */
    INSTANCE;

    public static final int MIN_SCORE = 0;
    public static final int MAX_SCORE = 100;
    public static final int PASSING_SCORE = 60;

    /** Чи належить бал допустимому діапазону 0..100. */
    public boolean isValidScore(Integer score) {
        return score != null && score >= MIN_SCORE && score <= MAX_SCORE;
    }

    /** Чи є бал позитивним (не менше 60). */
    public boolean isPassing(int score) {
        requireValid(score);
        return score >= PASSING_SCORE;
    }

    /** Оцінка за шкалою ЄКТС. */
    public String toEcts(int score) {
        requireValid(score);
        if (score >= 90) return "A";
        if (score >= 82) return "B";
        if (score >= 74) return "C";
        if (score >= 64) return "D";
        if (score >= 60) return "E";
        if (score >= 35) return "FX";
        return "F";
    }

    /** Оцінка за національною шкалою з урахуванням форми контролю. */
    public String toNational(int score, ControlType controlType) {
        requireValid(score);
        if (controlType == ControlType.CREDIT) {
            return score >= PASSING_SCORE ? "зараховано" : "не зараховано";
        }
        if (score >= 90) return "відмінно";
        if (score >= 74) return "добре";
        if (score >= 60) return "задовільно";
        return "незадовільно";
    }

    /** Оцінка за чотирибальною шкалою (5, 4, 3, 2) – для іспитів і диференційованих заліків. */
    public int toFivePoint(int score) {
        requireValid(score);
        if (score >= 90) return 5;
        if (score >= 74) return 4;
        if (score >= 60) return 3;
        return 2;
    }

    /** Переведення балу в усі шкали одночасно. */
    public ScaleGrade convert(int score, ControlType controlType) {
        return new ScaleGrade(score, toNational(score, controlType), toEcts(score), isPassing(score));
    }

    private void requireValid(int score) {
        if (score < MIN_SCORE || score > MAX_SCORE) {
            throw new IllegalArgumentException("Бал має бути в межах " + MIN_SCORE + ".." + MAX_SCORE + ": " + score);
        }
    }
}
