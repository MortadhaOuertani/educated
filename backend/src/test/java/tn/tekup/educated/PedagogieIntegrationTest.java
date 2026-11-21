package tn.tekup.educated;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import tn.tekup.educated.support.IntegrationTest;

class PedagogieIntegrationTest extends IntegrationTest {

    @Test
    void staffBuildsAStudyPlanAndAssignsATeacher() throws Exception {
        String subject = body(mvc.perform(post("/api/pedagogie/subjects").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"code": "T-ALGO", "name": {"fr": "Algorithmique"}, "defaultCoefficient": 2,
                                 "defaultCredits": 4}
                                """))
                .andExpect(status().isCreated()));
        String subjectId = read(subject, "$.id");
        String levelId = demoStudyLevelId();

        String plan = """
                {"code": "%s", "studyLevelId": "%s", "semesterNumber": 2, "units": [{"code": "UE-1",
                 "name": {"fr": "Fondamentaux"}, "subjects": [{"subjectId": "%s", "lectureHours": 21,
                 "assessments": [{"assessmentType": "EXAM", "weightPercent": %s},
                                 {"assessmentType": "TEST", "weightPercent": 30}]}]}]}
                """;
        mvc.perform(post("/api/pedagogie/study-plans").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content(plan.formatted("T-PLAN", levelId, subjectId, 60)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("studyPlan.weights"));
        String created = body(mvc.perform(post("/api/pedagogie/study-plans").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content(plan.formatted("T-PLAN", levelId, subjectId, 70)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.units[0].subjects[0].coefficient").value(2))
                .andExpect(jsonPath("$.units[0].subjects[0].assessments.length()").value(2)));
        String planSubjectId = read(created, "$.units[0].subjects[0].id");

        String teacher = body(mvc.perform(post("/api/pedagogie/teachers").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"person": {"firstName": "Sami", "lastName": "Trabelsi"}, "employeeNumber": "T-900",
                                 "gradeCode": "MA", "employmentType": "PERMANENT", "statutoryHours": 10}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.grade.fr").value("Maître assistant")));
        String teacherId = read(teacher, "$.id");

        String assignment = """
                {"studyPlanSubjectId": "%s", "schoolClassId": "%s", "sessionType": "LECTURE", "plannedHours": 21,
                 "teacherId": "%s"}
                """.formatted(planSubjectId, demoClassId(), teacherId);
        mvc.perform(post("/api/pedagogie/assignments").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content(assignment))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subjectCode").value("T-ALGO"))
                .andExpect(jsonPath("$.teacherName").value("TRABELSI Sami"));
        mvc.perform(post("/api/pedagogie/assignments").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content(assignment))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("assignment.duplicate"));

        mvc.perform(get("/api/pedagogie/teachers/" + teacherId + "/workload?academicYearId=" + currentYearId())
                        .with(as("superadmin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedHours").value(21))
                .andExpect(jsonPath("$.overloaded").value(true));
    }

    @Test
    void teacherSharesMaterialWritesTheJournalAndStudentsSeeIt() throws Exception {
        mvc.perform(get("/api/teacher/profile").with(as("teacher.demo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeNumber").value("DEMO-T-001"));
        mvc.perform(get("/api/teacher/periods").with(as("teacher.demo")))
                .andExpect(jsonPath("$[0].code").value("S1"));
        String assignments = body(mvc.perform(get("/api/teacher/assignments").with(as("teacher.demo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].subjectCode", hasItem("GL-ARCHI"))));
        String assignmentId = read(assignments, "$[0].id");

        when(fileStorage.store(any(), anyLong(), any())).thenReturn(UUID.randomUUID());
        var file = new MockMultipartFile("file", "chapitre1.pdf", "application/pdf", "pdf".getBytes());
        mvc.perform(multipart("/api/teacher/assignments/" + assignmentId + "/materials").file(file)
                        .with(as("teacher.demo")).param("title", "Chapitre 1").param("materialType", "COURSE"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.visibleToStudents").value(true));

        String sessionId = UUID.randomUUID().toString();
        String journal = "{\"sessionOccurrenceId\": \"" + sessionId + "\", \"content\": \"Introduction, MVC\"}";
        mvc.perform(post("/api/teacher/assignments/" + assignmentId + "/journal").with(as("teacher.demo"))
                        .contentType(MediaType.APPLICATION_JSON).content(journal))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/teacher/assignments/" + assignmentId + "/journal").with(as("teacher.demo"))
                        .contentType(MediaType.APPLICATION_JSON).content(journal))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("journal.duplicate"));

        mvc.perform(post("/api/teacher/assignments/" + assignmentId + "/homework").with(as("teacher.demo"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"title": "Exercices MVC", "assignedDate": "2026-10-05", "dueDate": "2026-10-12"}
                                """))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/student/subjects").with(as("student.demo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].subjectCode", hasItem("GL-JEE")));
        mvc.perform(get("/api/student/materials").with(as("student.demo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", hasItem("Chapitre 1")));
        mvc.perform(get("/api/student/homework").with(as("student.demo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", hasItem("Exercices MVC")));
    }

    @Test
    void makeUpSessionIsRequestedByTheTeacherAndDecidedOnce() throws Exception {
        String assignmentId = read(body(mvc.perform(get("/api/teacher/assignments").with(as("teacher.demo")))),
                "$[0].id");
        mvc.perform(post("/api/teacher/make-up-requests").with(as("teacher.demo"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"teachingAssignmentId": "%s", "originalOccurrenceId": "%s",
                                 "requestedDate": "2026-11-28", "startTime": "10:00", "endTime": "09:00"}
                                """.formatted(assignmentId, UUID.randomUUID())))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("makeUp.time"));
        String request = body(mvc.perform(post("/api/teacher/make-up-requests").with(as("teacher.demo"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"teachingAssignmentId": "%s", "originalOccurrenceId": "%s",
                                 "requestedDate": "2026-11-28", "startTime": "10:00", "endTime": "11:30",
                                 "reason": "Jour férié"}
                                """.formatted(assignmentId, UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REQUESTED")));
        String requestId = read(request, "$.id");

        mvc.perform(patch("/api/pedagogie/make-up-requests/" + requestId).with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"CONFIRMED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
        mvc.perform(get("/api/teacher/sessions?from=2026-11-28&to=2026-11-28").with(as("teacher.demo")))
                .andExpect(jsonPath("$[0].makeUp").value(true))
                .andExpect(jsonPath("$[0].startTime").value("10:00:00"));
        mvc.perform(patch("/api/pedagogie/make-up-requests/" + requestId).with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"REJECTED\"}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("makeUp.alreadyDecided"));
    }

    @Test
    void portalsOnlyReachTheirOwnData() throws Exception {
        mvc.perform(get("/api/teacher/assignments").with(as("student.demo"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/student/subjects").with(as("teacher.demo"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/pedagogie/subjects").with(as("teacher.demo"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/teacher/assignments/" + UUID.randomUUID() + "/journal").with(as("teacher.demo")))
                .andExpect(status().isNotFound());
    }
}
