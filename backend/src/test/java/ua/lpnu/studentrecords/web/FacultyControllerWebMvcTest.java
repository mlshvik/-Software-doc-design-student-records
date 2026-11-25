package ua.lpnu.studentrecords.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ua.lpnu.studentrecords.domain.exception.ConflictException;
import ua.lpnu.studentrecords.domain.exception.NotFoundException;
import ua.lpnu.studentrecords.service.FacultyService;
import ua.lpnu.studentrecords.web.controller.FacultyController;
import ua.lpnu.studentrecords.web.dto.FacultyRequest;
import ua.lpnu.studentrecords.web.dto.FacultyResponse;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Зріз веб-шару (@WebMvcTest): контролер, валідація запиту і формат помилок ProblemDetail.
 * Фільтри безпеки вимкнено – перевірки ролей покриває {@link SecuredApiTest}.
 */
@WebMvcTest(FacultyController.class)
@AutoConfigureMockMvc(addFilters = false)
class FacultyControllerWebMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private FacultyService facultyService;

    @Test
    void listsFaculties() throws Exception {
        when(facultyService.findAll()).thenReturn(List.of(
                new FacultyResponse(1L, "Інститут комп'ютерних наук та інформаційних технологій", "ІКНІ")));

        mvc.perform(get("/api/faculties"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].shortName").value("ІКНІ"));
    }

    @Test
    void createsFaculty() throws Exception {
        when(facultyService.create(any(FacultyRequest.class)))
                .thenReturn(new FacultyResponse(5L, "Інститут права", "ІПП"));

        mvc.perform(post("/api/faculties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Інститут права\",\"shortName\":\"ІПП\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void invalidRequestReturnsValidationProblem() throws Exception {
        mvc.perform(post("/api/faculties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"shortName\":\"ІПП\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("validation"))
                .andExpect(jsonPath("$.errors.name").value("Вкажіть назву факультету"));
        verifyNoInteractions(facultyService);
    }

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        mvc.perform(post("/api/faculties").contentType(MediaType.APPLICATION_JSON).content("{name:"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("malformed-request"));
    }

    @Test
    void missingFacultyReturnsNotFound() throws Exception {
        when(facultyService.get(99L)).thenThrow(new NotFoundException("Факультет", 99L));

        mvc.perform(get("/api/faculties/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("not-found"))
                .andExpect(jsonPath("$.detail").value("Факультет з ідентифікатором 99 не знайдено"));
    }

    @Test
    void duplicateFacultyReturnsConflict() throws Exception {
        when(facultyService.create(any(FacultyRequest.class)))
                .thenThrow(new ConflictException("Факультет з назвою «ІКНІ» уже існує"));

        mvc.perform(post("/api/faculties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"ІКНІ\",\"shortName\":\"ІКНІ\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("conflict"));
    }

    @Test
    void wrongPathVariableTypeReturnsBadRequest() throws Exception {
        mvc.perform(get("/api/faculties/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("malformed-request"));
    }
}
