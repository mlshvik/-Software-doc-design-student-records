package ua.lpnu.studentrecords.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ua.lpnu.studentrecords.domain.entity.StudyGroup;
import ua.lpnu.studentrecords.domain.enums.DebtStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Перевіряє міграції Flyway на реальній PostgreSQL: схема створюється, Hibernate
 * успішно валідує відповідність сутностей таблицям ({@code ddl-auto: validate}),
 * демонстраційні дані завантажуються.
 * <p>
 * Запускається лише коли доступна БД (у CI змінна {@code CI_POSTGRES=true}).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIfEnvironmentVariable(named = "CI_POSTGRES", matches = "true")
class DatabaseMigrationTest {

    @Autowired
    private StudyGroupRepository groupRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeachingAssignmentRepository assignmentRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private AcademicDebtRepository debtRepository;

    @Autowired
    private ScheduleEntryRepository scheduleRepository;

    @Autowired
    private AppUserRepository userRepository;

    @Test
    void seedDataIsLoaded() {
        StudyGroup group = groupRepository.findByName("ІР-43").orElseThrow();
        assertThat(group.getCourse()).isEqualTo(4);
        assertThat(studentRepository.findByGroupIdOrderByLastNameAscFirstNameAsc(group.getId())).hasSize(10);
        assertThat(userRepository.findByUsername("admin")).isPresent();
    }

    @Test
    void customQueriesWork() {
        StudyGroup group = groupRepository.findByName("ІР-43").orElseThrow();
        assertThat(assignmentRepository.findByGroupId(group.getId())).isNotEmpty();
        assertThat(gradeRepository.findByGroupId(group.getId())).isNotEmpty();
        assertThat(scheduleRepository.findByGroupId(group.getId())).isNotEmpty();
        assertThat(debtRepository.findByStatus(DebtStatus.OPEN)).isNotEmpty();
    }
}
