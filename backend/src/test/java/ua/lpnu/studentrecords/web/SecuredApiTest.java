package ua.lpnu.studentrecords.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ua.lpnu.studentrecords.domain.entity.AppUser;
import ua.lpnu.studentrecords.domain.entity.Faculty;
import ua.lpnu.studentrecords.domain.entity.Grade;
import ua.lpnu.studentrecords.domain.entity.Specialty;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.entity.StudyGroup;
import ua.lpnu.studentrecords.domain.entity.Subject;
import ua.lpnu.studentrecords.domain.entity.Teacher;
import ua.lpnu.studentrecords.domain.entity.TeachingAssignment;
import ua.lpnu.studentrecords.domain.enums.ControlType;
import ua.lpnu.studentrecords.domain.enums.GradeAttempt;
import ua.lpnu.studentrecords.domain.enums.Role;
import ua.lpnu.studentrecords.repository.AppUserRepository;
import ua.lpnu.studentrecords.repository.FacultyRepository;
import ua.lpnu.studentrecords.repository.GradeRepository;
import ua.lpnu.studentrecords.repository.SpecialtyRepository;
import ua.lpnu.studentrecords.repository.StudentRepository;
import ua.lpnu.studentrecords.repository.StudyGroupRepository;
import ua.lpnu.studentrecords.repository.SubjectRepository;
import ua.lpnu.studentrecords.repository.TeacherRepository;
import ua.lpnu.studentrecords.repository.TeachingAssignmentRepository;
import ua.lpnu.studentrecords.security.AuthenticatedUser;
import ua.lpnu.studentrecords.security.JwtService;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тести REST API з повним контекстом Spring (профіль test, H2): JWT-автентифікація, ролі,
 * доступ лише до власних даних і реакція шаблонів (Chain, Observer, State, Template Method) на запити.
 * Кожен тест виконується в транзакції, яка відкочується, тому тести не впливають один на одного.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecuredApiTest {

    private static final String PASSWORD = "secret123";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AppUserRepository userRepository;
    @Autowired
    private FacultyRepository facultyRepository;
    @Autowired
    private SpecialtyRepository specialtyRepository;
    @Autowired
    private StudyGroupRepository groupRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private TeachingAssignmentRepository assignmentRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private GradeRepository gradeRepository;

    private AppUser admin;
    private AppUser teacherUser;
    private AppUser otherTeacherUser;
    private AppUser studentUser;
    private Teacher teacher;
    private TeachingAssignment assignment;
    private Student student;
    private Student otherStudent;

    @BeforeEach
    void setUp() {
        String hash = passwordEncoder.encode(PASSWORD);
        admin = userRepository.save(new AppUser("test-admin", hash, Role.ADMIN));
        teacherUser = userRepository.save(new AppUser("test-teacher", hash, Role.TEACHER));
        otherTeacherUser = userRepository.save(new AppUser("test-teacher-2", hash, Role.TEACHER));
        studentUser = userRepository.save(new AppUser("test-student", hash, Role.STUDENT));

        Faculty faculty = facultyRepository.save(new Faculty("Тестовий інститут", "ТІ"));
        Specialty specialty = specialtyRepository.save(new Specialty("121", "Інженерія програмного забезпечення", faculty));
        StudyGroup group = groupRepository.save(new StudyGroup("ТС-41", specialty, 4, 2023));
        Subject subject = subjectRepository.save(new Subject("Бази даних", new BigDecimal("5.0"), 150, ControlType.EXAM));

        teacher = new Teacher("Коваленко", "Олена", "Іванівна", "доцент", "Кафедра ПЗ");
        teacher.setUser(teacherUser);
        teacherRepository.save(teacher);
        Teacher otherTeacher = new Teacher("Мельник", "Андрій", "Петрович", "старший викладач", "Кафедра ПЗ");
        otherTeacher.setUser(otherTeacherUser);
        teacherRepository.save(otherTeacher);

        assignment = assignmentRepository.save(new TeachingAssignment(subject, teacher, group, 7, "2026/2027"));

        student = new Student("Маліш", "Вікторія", "Олегівна", "TS0001", group);
        student.setUser(studentUser);
        studentRepository.save(student);
        otherStudent = studentRepository.save(new Student("Іваненко", "Петро", null, "TS0002", group));
    }

    private String bearer(AppUser user) {
        return "Bearer " + jwtService.issue(AuthenticatedUser.from(user)).token();
    }

    private String gradeJson(Long studentId, int score) {
        return "{\"studentId\":" + studentId + ",\"assignmentId\":" + assignment.getId() + ",\"score\":" + score + "}";
    }

    // ---------- автентифікація ----------

    @Test
    void loginReturnsTokenAndProfile() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test-student\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("STUDENT"))
                .andExpect(jsonPath("$.user.fullName").value("Маліш Вікторія Олегівна"))
                .andExpect(jsonPath("$.user.studentId").value(student.getId().intValue()));
    }

    @Test
    void wrongPasswordIsUnauthorized() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test-student\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("unauthorized"))
                .andExpect(jsonPath("$.detail").value("Неправильний логін або пароль"));
    }

    @Test
    void requestWithoutTokenIsUnauthorized() throws Exception {
        mvc.perform(get("/api/students"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("unauthorized"));
        mvc.perform(get("/api/students").header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiDocumentationIsPublic() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value(containsString("Система обліку студентів")));
    }

    @Test
    void meDescribesTeacher() throws Exception {
        mvc.perform(get("/api/me").header(HttpHeaders.AUTHORIZATION, bearer(teacherUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("TEACHER"))
                .andExpect(jsonPath("$.teacherId").value(teacher.getId().intValue()));
    }

    // ---------- ролі та власні дані ----------

    @Test
    void studentSeesOnlyOwnData() throws Exception {
        String token = bearer(studentUser);
        mvc.perform(get("/api/students/{id}/record", student.getId()).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.student.fullName").value("Маліш Вікторія Олегівна"))
                .andExpect(jsonPath("$.student.groupName").value("ТС-41"));
        mvc.perform(get("/api/students/{id}/record", otherStudent.getId()).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("forbidden"));
        mvc.perform(get("/api/students").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/grades").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON).content(gradeJson(student.getId(), 100)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminSearchesStudents() throws Exception {
        mvc.perform(get("/api/students").param("q", "маліш").header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].recordBookNumber").value("TS0001"))
                .andExpect(jsonPath("$[0].statusTitle").isNotEmpty());
    }

    // ---------- оцінки: Chain of Responsibility + Observer ----------

    @Test
    void failedGradeOpensDebtAndRetakeClosesIt() throws Exception {
        String teacherToken = bearer(teacherUser);
        mvc.perform(post("/api/grades").header(HttpHeaders.AUTHORIZATION, teacherToken)
                        .contentType(MediaType.APPLICATION_JSON).content(gradeJson(student.getId(), 45)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.attempt").value("REGULAR"))
                .andExpect(jsonPath("$.ects").value("FX"))
                .andExpect(jsonPath("$.passed").value(false));

        mvc.perform(get("/api/debts").param("status", "OPEN").header(HttpHeaders.AUTHORIZATION, teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].subjectName").value("Бази даних"));
        mvc.perform(get("/api/notifications/me").header(HttpHeaders.AUTHORIZATION, bearer(studentUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].message").value(containsString("Бази даних")));

        mvc.perform(post("/api/grades").header(HttpHeaders.AUTHORIZATION, teacherToken)
                        .contentType(MediaType.APPLICATION_JSON).content(gradeJson(student.getId(), 80)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.attempt").value("RETAKE_TEACHER"))
                .andExpect(jsonPath("$.national").value("добре"));

        mvc.perform(get("/api/debts").param("status", "OPEN").header(HttpHeaders.AUTHORIZATION, teacherToken))
                .andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/assignments/{id}/sheet", assignment.getId()).header(HttpHeaders.AUTHORIZATION, teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows", hasSize(2)));
    }

    @Test
    void teacherCannotGradeForeignDiscipline() throws Exception {
        mvc.perform(post("/api/grades").header(HttpHeaders.AUTHORIZATION, bearer(otherTeacherUser))
                        .contentType(MediaType.APPLICATION_JSON).content(gradeJson(student.getId(), 90)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("grade-validation"))
                .andExpect(jsonPath("$.rule").value("TeacherOwnsAssignmentHandler"));
        mvc.perform(get("/api/assignments/{id}/sheet", assignment.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherTeacherUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    void scoreOutOfRangeIsRejectedByValidation() throws Exception {
        mvc.perform(post("/api/grades").header(HttpHeaders.AUTHORIZATION, bearer(teacherUser))
                        .contentType(MediaType.APPLICATION_JSON).content(gradeJson(student.getId(), 101)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.score").exists());
    }

    // ---------- статус студента: State ----------

    @Test
    void invalidStatusTransitionIsConflict() throws Exception {
        String token = bearer(admin);
        mvc.perform(patch("/api/students/{id}/status", student.getId()).header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"GRADUATED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("GRADUATED"));
        mvc.perform(patch("/api/students/{id}/status", student.getId()).header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("invalid-status-transition"));
    }

    // ---------- довідники ----------

    @Test
    void onlyAdminManagesFaculties() throws Exception {
        String body = "{\"name\":\"Інститут права\",\"shortName\":\"ІПП\"}";
        mvc.perform(post("/api/faculties").header(HttpHeaders.AUTHORIZATION, bearer(teacherUser))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/faculties").header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortName").value("ІПП"));
        mvc.perform(post("/api/faculties").header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/faculties").header(HttpHeaders.AUTHORIZATION, bearer(studentUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // ---------- звіти: Factory Method + Template Method + Builder ----------

    @Test
    void reportsAreGeneratedByType() throws Exception {
        gradeRepository.save(new Grade(student, assignment, 90, GradeAttempt.REGULAR, teacher));
        String token = bearer(admin);

        mvc.perform(get("/api/reports/group-performance").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("GROUP_PERFORMANCE"))
                .andExpect(jsonPath("$.columns[0].key").value("group"))
                .andExpect(jsonPath("$.rows[0].group").value("ТС-41"))
                .andExpect(jsonPath("$.rows[0].averageScore").value(90.0));
        mvc.perform(get("/api/reports/teacher-statistics").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows", hasSize(1)));
        mvc.perform(get("/api/reports").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(jsonPath("$", hasSize(3)));
        mvc.perform(get("/api/reports/salary").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/reports/academic-debts").header(HttpHeaders.AUTHORIZATION, bearer(studentUser)))
                .andExpect(status().isForbidden());
    }

    // ---------- налаштування: Strategy ----------

    @Test
    void gpaStrategySettingIsValidated() throws Exception {
        String token = bearer(admin);
        mvc.perform(put("/api/settings/gpa.strategy").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"value\":\"simple\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value("SIMPLE"));
        mvc.perform(put("/api/settings/gpa.strategy").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"value\":\"MEDIAN\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/students/{id}/gpa", student.getId()).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.strategy").value("SIMPLE"));
    }
}
