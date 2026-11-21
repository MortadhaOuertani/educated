package tn.tekup.educated;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import tn.tekup.educated.support.IntegrationTest;

class ScolariteIntegrationTest extends IntegrationTest {

    private static final String STUDENT_JSON = """
            {"matricule": "%s", "person": {"firstName": "Amine", "lastName": "Ben Salem", "gender": "MALE",
             "birthDate": "2004-02-10", "email": "amine@test.local"}, "admissionDate": "2024-09-15"}
            """;

    @Test
    void staffCreatesAStudentAddsAGuardianAndEnrollsThem() throws Exception {
        String student = body(mvc.perform(post("/api/scolarite/students").with(as("scolarite.agent"))
                        .contentType(MediaType.APPLICATION_JSON).content(STUDENT_JSON.formatted("T-1001")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE")));
        String studentId = read(student, "$.id");

        mvc.perform(post("/api/scolarite/students").with(as("scolarite.agent"))
                        .contentType(MediaType.APPLICATION_JSON).content(STUDENT_JSON.formatted("T-1001")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("student.matricule"));

        mvc.perform(get("/api/scolarite/students?search=ben salem").with(as("scolarite.agent")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].matricule").value("T-1001"));

        mvc.perform(post("/api/scolarite/students/" + studentId + "/guardians").with(as("scolarite.agent"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"person": {"firstName": "Karim", "lastName": "Ben Salem", "mobile": "+21620000000"},
                                 "kinshipCode": "PERE", "primaryContact": true, "smsOptIn": true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.kinshipCode").value("PERE"))
                .andExpect(jsonPath("$.primaryContact").value(true));

        String classId = demoClassId();
        mvc.perform(post("/api/scolarite/students/" + studentId + "/enrollments").with(as("scolarite.agent"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"schoolClassId": "%s", "enrollmentType": "NEW", "enrollmentDate": "2026-09-01"}
                                """.formatted(classId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ENROLLED"))
                .andExpect(jsonPath("$.assignments[0].schoolClassId").value(classId));

        mvc.perform(post("/api/scolarite/students/" + studentId + "/enrollments").with(as("scolarite.agent"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"schoolClassId": "%s", "enrollmentType": "NEW", "enrollmentDate": "2026-09-02"}
                                """.formatted(classId)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("enrollment.duplicate"));
    }

    @Test
    void studentRequestsADocumentAndStaffProcessIt() throws Exception {
        mvc.perform(get("/api/student/profile").with(as("student.demo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.student.matricule").value("DEMO-0001"))
                .andExpect(jsonPath("$.currentClass.code").value("ING-5-S-GLSI-B"));

        String types = body(mvc.perform(get("/api/scolarite/document-types").with(as("student.demo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(11)));
        List<String> transcriptIds = read(types, "$[?(@.code == 'RELEVE_NOTES')].id");
        String transcriptId = transcriptIds.getFirst();

        String request = body(mvc.perform(multipart("/api/student/document-requests").with(as("student.demo"))
                        .param("documentTypeId", transcriptId).param("reason", "Dossier de bourse"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.matricule").value("DEMO-0001")));
        String requestId = read(request, "$.id");

        mvc.perform(get("/api/scolarite/document-requests?status=SUBMITTED").with(as("scolarite.agent")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '" + requestId + "')]").exists());

        mvc.perform(patch("/api/scolarite/document-requests/" + requestId + "/status").with(as("scolarite.agent"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"DONE\"}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("documentRequest.transition"));
        mvc.perform(patch("/api/scolarite/document-requests/" + requestId + "/status").with(as("scolarite.agent"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"IN_PROGRESS\"}"))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/scolarite/document-requests/" + requestId + "/status").with(as("scolarite.agent"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"DONE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.processedAt").isNotEmpty());

        mvc.perform(get("/api/student/document-requests").with(as("student.demo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("DONE"));
    }

    @Test
    void portalUsersCannotOpenBackOfficeScreensAndStaffCannotUseTheStudentPortal() throws Exception {
        mvc.perform(get("/api/scolarite/students").with(as("student.demo"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/scolarite/students").with(as("teacher.demo"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/student/profile").with(as("scolarite.agent"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/scolarite/students").with(as("scolarite.agent"))).andExpect(status().isOk());
    }
}
