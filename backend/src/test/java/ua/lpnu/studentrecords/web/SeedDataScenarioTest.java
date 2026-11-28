package ua.lpnu.studentrecords.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Наскрізні (end-to-end) сценарії REST API на реальній PostgreSQL з демонстраційними даними
 * (міграції Flyway V1 + V2), так, як систему використовують через веб-інтерфейс:
 * вхід демо-користувачів, виставлення оцінки з перескладанням, кабінет студента, звіти, розклад.
 * <p>
 * Кожен тест виконується в транзакції з відкатом. Запускається, коли доступна БД
 * (у CI: {@code CI_POSTGRES=true}).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@EnabledIfEnvironmentVariable(named = "CI_POSTGRES", matches = "true")
class SeedDataScenarioTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Test
    void demoAccountsCanLogIn() throws Exception {
        assertThat(loginResponse("admin", "admin123").at("/user/role").asText()).isEqualTo("ADMIN");
        assertThat(loginResponse("melnyk", "teacher123").at("/user/role").asText()).isEqualTo("TEACHER");
        JsonNode student = loginResponse("ir23101", "student123");
        assertThat(student.at("/user/role").asText()).isEqualTo("STUDENT");
        assertThat(student.at("/user/studentId").asLong()).isEqualTo(1L);
    }

    @Test
    void teacherGradesStudentWithRetakeOnSeedData() throws Exception {
        String teacher = token("melnyk", "teacher123");
        long assignmentId = currentAssignment(teacher, "ІР-43");

        perform(auth(get("/api/assignments/{id}/sheet", assignmentId), teacher))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows", hasSize(10)));

        // незадовільна оцінка → відкривається заборгованість
        perform(auth(post("/api/grades"), teacher).contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("studentId", 1, "assignmentId", assignmentId, "score", 45))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.attempt").value("REGULAR"))
                .andExpect(jsonPath("$.ects").value("FX"));
        assertThat(openDebtExists(teacher, assignmentId)).isTrue();

        // перескладання викладачу → заборгованість закривається
        perform(auth(post("/api/grades"), teacher).contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("studentId", 1, "assignmentId", assignmentId, "score", 75))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.attempt").value("RETAKE_TEACHER"))
                .andExpect(jsonPath("$.passed").value(true));
        assertThat(openDebtExists(teacher, assignmentId)).isFalse();

        // студент отримав сповіщення про обидві оцінки
        String student = token("ir23101", "student123");
        JsonNode notifications = read(perform(auth(get("/api/notifications/me"), student)).andExpect(status().isOk()));
        assertThat(notifications.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void studentRecordIsAssembledByFacade() throws Exception {
        String student = token("ir23101", "student123");
        JsonNode record = read(perform(auth(get("/api/students/{id}/record", 1), student)).andExpect(status().isOk()));
        assertThat(record.at("/student/groupName").asText()).isEqualTo("ІР-43");
        assertThat(record.get("grades").size()).isGreaterThan(0);
        assertThat(record.get("schedule").size()).isGreaterThan(0);
        assertThat(record.at("/gpa/strategy").asText()).isEqualTo("CREDIT_WEIGHTED");
        assertThat(record.at("/gpa/value").decimalValue()).isBetween(
                java.math.BigDecimal.ZERO, java.math.BigDecimal.valueOf(100));
    }

    @Test
    void reportsAreGeneratedFromSeedData() throws Exception {
        String admin = token("admin", "admin123");
        perform(auth(get("/api/reports/group-performance"), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows", hasSize(6)));
        perform(auth(get("/api/reports/teacher-statistics"), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows", hasSize(8)));
        JsonNode debts = read(perform(auth(get("/api/reports/academic-debts"), admin)).andExpect(status().isOk()));
        assertThat(debts.get("rows").size()).isGreaterThan(0);
    }

    @Test
    void seedScheduleHasNoConflicts() throws Exception {
        String admin = token("admin", "admin123");
        JsonNode groups = read(perform(auth(get("/api/groups"), admin)).andExpect(status().isOk()));
        for (JsonNode group : groups) {
            JsonNode entries = read(perform(auth(get("/api/schedule").param("groupId", group.get("id").asText()), admin))
                    .andExpect(status().isOk()));
            for (JsonNode entry : entries) {
                Map<String, Object> request = new LinkedHashMap<>();
                request.put("assignmentId", entry.get("assignmentId").asLong());
                request.put("dayOfWeek", entry.get("dayOfWeek").asText());
                request.put("pairNumber", entry.get("pairNumber").asInt());
                request.put("weekType", entry.get("weekType").asText());
                request.put("classroom", entry.get("classroom").asText());
                request.put("lessonType", entry.get("lessonType").asText());
                JsonNode conflicts = read(perform(auth(post("/api/schedule/check"), admin)
                        .param("excludeId", entry.get("id").asText())
                        .contentType(MediaType.APPLICATION_JSON).content(body(request)))
                        .andExpect(status().isOk()));
                assertThat(conflicts.size())
                        .as("конфлікти для заняття %s групи %s", entry.get("id"), group.get("name"))
                        .isZero();
            }
        }
    }

    // ---------- допоміжні методи ----------

    private JsonNode loginResponse(String username, String password) throws Exception {
        return read(perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("username", username, "password", password))))
                .andExpect(status().isOk()));
    }

    private String token(String username, String password) throws Exception {
        return loginResponse(username, password).get("token").asText();
    }

    private long currentAssignment(String teacherToken, String groupName) throws Exception {
        JsonNode list = read(perform(auth(get("/api/assignments"), teacherToken)).andExpect(status().isOk()));
        return StreamSupport.stream(list.spliterator(), false)
                .filter(a -> groupName.equals(a.get("groupName").asText())
                        && "2026/2027".equals(a.get("academicYear").asText()))
                .findFirst()
                .orElseThrow()
                .get("id").asLong();
    }

    private boolean openDebtExists(String token, long assignmentId) throws Exception {
        JsonNode debts = read(perform(auth(get("/api/debts"), token)
                .param("status", "OPEN").param("studentId", "1")).andExpect(status().isOk()));
        return StreamSupport.stream(debts.spliterator(), false)
                .anyMatch(d -> d.get("assignmentId").asLong() == assignmentId);
    }

    private ResultActions perform(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request);
    }

    private static MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request, String token) {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    private String body(Object value) throws Exception {
        return json.writeValueAsString(value);
    }

    private JsonNode read(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }
}
