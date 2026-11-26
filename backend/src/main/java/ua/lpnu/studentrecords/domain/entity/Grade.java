package ua.lpnu.studentrecords.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import ua.lpnu.studentrecords.domain.enums.GradeAttempt;

import java.time.LocalDateTime;

/**
 * Оцінка студента з дисципліни (за 100-бальною шкалою).
 * Для кожної дисципліни можливі основна спроба та два перескладання.
 */
@Entity
@Table(name = "grade",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_grade_student_assignment_attempt",
                columnNames = {"student_id", "assignment_id", "attempt"}))
public class Grade extends BaseEntity {

    /** Мінімальний прохідний бал. */
    public static final int PASSING_SCORE = 60;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private TeachingAssignment assignment;

    @Column(nullable = false)
    private Integer score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GradeAttempt attempt = GradeAttempt.REGULAR;

    @Column(name = "graded_at", nullable = false)
    private LocalDateTime gradedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "graded_by_id")
    private Teacher gradedBy;

    protected Grade() {
    }

    public Grade(Student student, TeachingAssignment assignment, Integer score,
                 GradeAttempt attempt, Teacher gradedBy) {
        this.student = student;
        this.assignment = assignment;
        this.score = score;
        this.attempt = attempt;
        this.gradedBy = gradedBy;
        this.gradedAt = LocalDateTime.now();
    }

    /** Чи є оцінка позитивною (не менше 60 балів). */
    public boolean isPassed() {
        return score != null && score >= PASSING_SCORE;
    }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public TeachingAssignment getAssignment() { return assignment; }
    public void setAssignment(TeachingAssignment assignment) { this.assignment = assignment; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public GradeAttempt getAttempt() { return attempt; }
    public void setAttempt(GradeAttempt attempt) { this.attempt = attempt; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }

    public Teacher getGradedBy() { return gradedBy; }
    public void setGradedBy(Teacher gradedBy) { this.gradedBy = gradedBy; }
}
