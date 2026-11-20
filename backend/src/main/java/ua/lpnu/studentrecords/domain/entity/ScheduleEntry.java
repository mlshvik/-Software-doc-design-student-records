package ua.lpnu.studentrecords.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import ua.lpnu.studentrecords.domain.enums.LessonType;
import ua.lpnu.studentrecords.domain.enums.WeekType;

import java.time.DayOfWeek;

/**
 * Заняття в розкладі: призначення викладача + день тижня, пара, тип тижня, аудиторія.
 */
@Entity
@Table(name = "schedule_entry")
public class ScheduleEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private TeachingAssignment assignment;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    /** Номер пари (1–8). */
    @Column(name = "pair_number", nullable = false)
    private Integer pairNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "week_type", nullable = false, length = 5)
    private WeekType weekType = WeekType.ALL;

    @Column(nullable = false, length = 20)
    private String classroom;

    @Enumerated(EnumType.STRING)
    @Column(name = "lesson_type", nullable = false, length = 10)
    private LessonType lessonType;

    protected ScheduleEntry() {
    }

    public ScheduleEntry(TeachingAssignment assignment, DayOfWeek dayOfWeek, Integer pairNumber,
                         WeekType weekType, String classroom, LessonType lessonType) {
        this.assignment = assignment;
        this.dayOfWeek = dayOfWeek;
        this.pairNumber = pairNumber;
        this.weekType = weekType;
        this.classroom = classroom;
        this.lessonType = lessonType;
    }

    /** Чи збігається час цього заняття з іншим (той самий день, пара і тип тижня перетинаються). */
    public boolean overlapsInTime(DayOfWeek day, int pair, WeekType week) {
        return dayOfWeek == day && pairNumber == pair && weekType.overlaps(week);
    }

    public TeachingAssignment getAssignment() { return assignment; }
    public void setAssignment(TeachingAssignment assignment) { this.assignment = assignment; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public Integer getPairNumber() { return pairNumber; }
    public void setPairNumber(Integer pairNumber) { this.pairNumber = pairNumber; }

    public WeekType getWeekType() { return weekType; }
    public void setWeekType(WeekType weekType) { this.weekType = weekType; }

    public String getClassroom() { return classroom; }
    public void setClassroom(String classroom) { this.classroom = classroom; }

    public LessonType getLessonType() { return lessonType; }
    public void setLessonType(LessonType lessonType) { this.lessonType = lessonType; }
}
