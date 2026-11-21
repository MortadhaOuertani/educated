package tn.tekup.educated;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import tn.tekup.educated.support.IntegrationTest;

class StagesIntegrationTest extends IntegrationTest {

    @Test
    void internshipGoesFromDeclarationToPublishedDefense() throws Exception {
        String options = body(mvc.perform(get("/api/student/internship-options").with(as("student.demo")))
                .andExpect(status().isOk()));
        List<String> pfe = read(options, "$.natures[?(@.code == 'PFE')].id");

        String declaration = """
                {"natureId": "%s", "newCompany": {"name": "Vermeg", "sector": "Logiciel"},
                 "supervisor": {"firstName": "Leila", "lastName": "Haddad", "email": "leila@test.local"},
                 "title": "Plateforme de gestion scolaire", "startDate": "2027-02-01", "endDate": "%s"}
                """;
        mvc.perform(post("/api/student/internships").with(as("student.demo"))
                        .contentType(MediaType.APPLICATION_JSON).content(declaration.formatted(pfe.getFirst(), "2027-03-01")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("internship.duration"));
        String internship = body(mvc.perform(post("/api/student/internships").with(as("student.demo"))
                        .contentType(MediaType.APPLICATION_JSON).content(declaration.formatted(pfe.getFirst(), "2027-06-30")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"))
                .andExpect(jsonPath("$.companyName").value("Vermeg"))
                .andExpect(jsonPath("$.referenceNumber").value(org.hamcrest.Matchers.startsWith("STG-2026-"))));
        String internshipId = read(internship, "$.id");

        String teacherId = read(body(mvc.perform(get("/api/teacher/profile").with(as("teacher.demo")))), "$.id");
        mvc.perform(post("/api/stages/internships/" + internshipId + "/approve").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"academicSupervisorId\": \"" + teacherId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.academicSupervisorName").value("TEACHER Demo"));

        LocalDate today = LocalDate.now();
        String session = body(mvc.perform(post("/api/stages/defense-sessions").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"name": "Session PFE", "academicYearId": "%s", "startDate": "%s", "endDate": "%s",
                                 "requestsOpenFrom": "%s", "requestsOpenTo": "%s"}
                                """.formatted(currentYearId(), today.plusDays(10), today.plusDays(20),
                                today.minusDays(1), today.plusDays(5))))
                .andExpect(status().isCreated()));
        String sessionId = read(session, "$.id");

        String defenseRequest = "{\"defenseSessionId\": \"" + sessionId + "\"}";
        mvc.perform(post("/api/student/internships/" + internshipId + "/defense-request").with(as("student.demo"))
                        .contentType(MediaType.APPLICATION_JSON).content(defenseRequest))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("internship.reportMissing"));
        when(fileStorage.store(any(), anyLong(), any())).thenReturn(UUID.randomUUID());
        mvc.perform(multipart("/api/student/internships/" + internshipId + "/documents").with(as("student.demo"))
                        .file(new MockMultipartFile("file", "rapport.pdf", "application/pdf", "pdf".getBytes()))
                        .param("documentType", "REPORT"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documents[0].documentType").value("REPORT"));
        mvc.perform(post("/api/student/internships/" + internshipId + "/defense-request").with(as("student.demo"))
                        .contentType(MediaType.APPLICATION_JSON).content(defenseRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DEFENSE_REQUESTED"));

        mvc.perform(post("/api/teacher/internships/" + internshipId + "/authorize-defense").with(as("teacher.demo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AUTHORIZED_BY_SUPERVISOR"));
        mvc.perform(post("/api/stages/internships/" + internshipId + "/authorize-defense").with(as("superadmin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AUTHORIZED_TO_DEFEND"));

        String jury = """
                {"defenseSessionId": "%s", "code": "%s", "juryDate": "%s", "members": [
                 {"teacherId": "%s", "role": "PRESIDENT"}, {"externalName": "Leila Haddad", "role": "%s"}]}
                """;
        mvc.perform(post("/api/stages/juries").with(as("superadmin")).contentType(MediaType.APPLICATION_JSON)
                        .content(jury.formatted(sessionId, "J-01", today.plusDays(12), teacherId, "PRESIDENT")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("jury.president"));
        String juryId = read(body(mvc.perform(post("/api/stages/juries").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jury.formatted(sessionId, "J-01", today.plusDays(12), teacherId, "SUPERVISOR")))
                .andExpect(status().isCreated())), "$.id");

        String defenseId = read(body(mvc.perform(post("/api/stages/defenses").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"internshipId": "%s", "juryId": "%s", "startTime": "09:00", "endTime": "10:00"}
                                """.formatted(internshipId, juryId)))
                .andExpect(status().isCreated())), "$.id");
        mvc.perform(patch("/api/stages/defenses/" + defenseId + "/grade").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"grade\": 15.5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mention").value("GOOD"));

        mvc.perform(get("/api/student/internships").with(as("student.demo")))
                .andExpect(jsonPath("$[0].status").value("DEFENDED"))
                .andExpect(jsonPath("$[0].defense.grade").doesNotExist());
        mvc.perform(post("/api/stages/defenses/" + defenseId + "/publish").with(as("superadmin")))
                .andExpect(status().isOk());
        mvc.perform(get("/api/student/internships").with(as("student.demo")))
                .andExpect(jsonPath("$[0].status").value("VALIDATED"))
                .andExpect(jsonPath("$[0].defense.grade").value(15.5))
                .andExpect(jsonPath("$[0].history.length()").value(7));
        mvc.perform(get("/api/teacher/juries").with(as("teacher.demo")))
                .andExpect(jsonPath("$[0].defenses[0].mention").value("GOOD"));
    }

    @Test
    void onlyTheInternshipOfficeApprovesInternships() throws Exception {
        mvc.perform(get("/api/stages/internships").with(as("teacher.demo"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/stages/internships").with(as("student.demo"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/teacher/internships").with(as("student.demo"))).andExpect(status().isForbidden());
    }
}
