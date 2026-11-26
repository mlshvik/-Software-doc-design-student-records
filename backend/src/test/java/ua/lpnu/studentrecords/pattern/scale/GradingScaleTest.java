package ua.lpnu.studentrecords.pattern.scale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import ua.lpnu.studentrecords.domain.enums.ControlType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тести шаблону Singleton – шкали оцінювання.
 */
class GradingScaleTest {

    private final GradingScale scale = GradingScale.INSTANCE;

    @Test
    void hasExactlyOneInstance() {
        assertEquals(1, GradingScale.values().length);
        assertSame(GradingScale.INSTANCE, GradingScale.valueOf("INSTANCE"));
    }

    @ParameterizedTest(name = "{0} балів → {1}")
    @CsvSource({
            "100, A", "90, A", "89, B", "82, B", "81, C", "74, C",
            "73, D", "64, D", "63, E", "60, E", "59, FX", "35, FX", "34, F", "0, F"
    })
    void convertsScoreToEcts(int score, String expectedEcts) {
        assertEquals(expectedEcts, scale.toEcts(score));
    }

    @ParameterizedTest(name = "{0} балів → {1}")
    @CsvSource({
            "95, відмінно", "90, відмінно", "89, добре", "74, добре",
            "73, задовільно", "60, задовільно", "59, незадовільно", "10, незадовільно"
    })
    void convertsExamScoreToNationalScale(int score, String expected) {
        assertEquals(expected, scale.toNational(score, ControlType.EXAM));
        assertEquals(expected, scale.toNational(score, ControlType.DIFF_CREDIT));
    }

    @Test
    void creditUsesPassFailScale() {
        assertEquals("зараховано", scale.toNational(60, ControlType.CREDIT));
        assertEquals("зараховано", scale.toNational(100, ControlType.CREDIT));
        assertEquals("не зараховано", scale.toNational(59, ControlType.CREDIT));
    }

    @Test
    void passingScoreStartsAtSixty() {
        assertTrue(scale.isPassing(60));
        assertFalse(scale.isPassing(59));
    }

    @Test
    void convertsToFivePointScale() {
        assertEquals(5, scale.toFivePoint(92));
        assertEquals(4, scale.toFivePoint(75));
        assertEquals(3, scale.toFivePoint(60));
        assertEquals(2, scale.toFivePoint(30));
    }

    @Test
    void convertReturnsAllScales() {
        ScaleGrade grade = scale.convert(85, ControlType.EXAM);
        assertEquals(new ScaleGrade(85, "добре", "B", true), grade);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 101, 250})
    void rejectsScoreOutOfRange(int score) {
        assertThrows(IllegalArgumentException.class, () -> scale.toEcts(score));
        assertFalse(scale.isValidScore(score));
    }

    @Test
    void nullScoreIsInvalid() {
        assertFalse(scale.isValidScore(null));
    }
}
