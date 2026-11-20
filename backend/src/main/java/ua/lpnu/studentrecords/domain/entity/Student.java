package ua.lpnu.studentrecords.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import ua.lpnu.studentrecords.domain.enums.StudentStatus;

import java.time.LocalDate;

/**
 * Студент закладу вищої освіти.
 */
@Entity
@Table(name = "student")
public class Student extends Person {

    /** Номер залікової книжки. */
    @Column(name = "record_book_number", nullable = false, unique = true, length = 20)
    private String recordBookNumber;

    @Column(length = 20)
    private String phone;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private StudyGroup group;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudentStatus status = StudentStatus.ACTIVE;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private AppUser user;

    protected Student() {
    }

    public Student(String lastName, String firstName, String middleName,
                   String recordBookNumber, StudyGroup group) {
        setLastName(lastName);
        setFirstName(firstName);
        setMiddleName(middleName);
        this.recordBookNumber = recordBookNumber;
        this.group = group;
    }

    public String getRecordBookNumber() { return recordBookNumber; }
    public void setRecordBookNumber(String recordBookNumber) { this.recordBookNumber = recordBookNumber; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public StudyGroup getGroup() { return group; }
    public void setGroup(StudyGroup group) { this.group = group; }

    public StudentStatus getStatus() { return status; }

    /**
     * Встановлює статус напряму (використовується при створенні та в шарі доступу до даних).
     * Бізнес-переходи між статусами виконуються через шаблон State.
     */
    public void setStatus(StudentStatus status) { this.status = status; }

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
}
