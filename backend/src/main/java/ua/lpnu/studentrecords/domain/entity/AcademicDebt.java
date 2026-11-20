package ua.lpnu.studentrecords.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import ua.lpnu.studentrecords.domain.enums.DebtStatus;

import java.time.LocalDateTime;

/**
 * Академічна заборгованість студента з дисципліни.
 * Відкривається автоматично при незадовільній оцінці і закривається
 * після успішного перескладання (шаблон Observer).
 */
@Entity
@Table(name = "academic_debt")
public class AcademicDebt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private TeachingAssignment assignment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DebtStatus status = DebtStatus.OPEN;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    protected AcademicDebt() {
    }

    public AcademicDebt(Student student, TeachingAssignment assignment) {
        this.student = student;
        this.assignment = assignment;
        this.openedAt = LocalDateTime.now();
    }

    /** Закриває заборгованість після успішного перескладання. */
    public void close() {
        this.status = DebtStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
    }

    public boolean isOpen() {
        return status == DebtStatus.OPEN;
    }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public TeachingAssignment getAssignment() { return assignment; }
    public void setAssignment(TeachingAssignment assignment) { this.assignment = assignment; }

    public DebtStatus getStatus() { return status; }
    public void setStatus(DebtStatus status) { this.status = status; }

    public LocalDateTime getOpenedAt() { return openedAt; }
    public void setOpenedAt(LocalDateTime openedAt) { this.openedAt = openedAt; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
}
