package tn.tekup.educated;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import tn.tekup.educated.support.IntegrationTest;

class ExamensIntegrationTest extends IntegrationTest {

    @Test
    void examCalendarIsPublishedToStudentsAndInvigilators() throws Exception {
        String archi = archiAssignment();
        String planSubjectId = read(archi, "$.studyPlanSubjectId");
        String teacherId = read(body(mvc.perform(get("/api/teacher/profile").with(as("teacher.demo")))), "$.id");
        String roomId = read(body(mvc.perform(get("/api/parametrage/rooms").with(as("superadmin")))), "$[0].id");

        String sessionId = read(body(mvc.perform(post("/api/examens/exam-sessions").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"name": "Examens S1", "sessionType": "MAIN", "academicPeriodId": "%s",
                                 "startDate": "2027-01-04", "endDate": "2027-01-15"}
                                """.formatted(firstPeriodId())))
                .andExpect(status().isCreated())), "$.id");
        String exam = """
                {"studyPlanSubjectId": "%s", "assessmentType": "EXAM", "examDate": "2027-01-05",
                 "startTime": "09:00", "endTime": "%s", "schoolClassIds": ["%s"],
                 "rooms": [{"roomId": "%s", "seatCount": 30}],
                 "invigilators": [{"teacherId": "%s", "roomId": "%s", "role": "HEAD_INVIGILATOR"}]}
                """;
        mvc.perform(post("/api/examens/exam-sessions/" + sessionId + "/exams").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exam.formatted(planSubjectId, "10:30", demoClassId(), roomId, teacherId, roomId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.durationMinutes").value(90))
                .andExpect(jsonPath("$.roomCodes[0]").value("B-213"));
        mvc.perform(post("/api/examens/exam-sessions/" + sessionId + "/exams").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exam.formatted(planSubjectId, "11:00", demoClassId(), roomId, teacherId, roomId)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("exam.roomBusy"));

        mvc.perform(get("/api/student/exams").with(as("student.demo")))
                .andExpect(jsonPath("$[*].examDate", org.hamcrest.Matchers.not(hasItem("2027-01-05"))));
        mvc.perform(post("/api/examens/exam-sessions/" + sessionId + "/publish").with(as("superadmin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.published").value(true));
        mvc.perform(get("/api/student/exams").with(as("student.demo")))
                .andExpect(jsonPath("$[*].examDate", hasItem("2027-01-05")));
        mvc.perform(get("/api/teacher/invigilations").with(as("teacher.demo")))
                .andExpect(jsonPath("$[*].role", hasItem("HEAD_INVIGILATOR")));
    }

    @Test
    void teacherGradesStaffPublishAndReportCardsAreComputed() throws Exception {
        String assignmentId = read(archiAssignment(), "$.id");
        String periodId = firstPeriodId();
        String studentId = read(body(mvc.perform(get("/api/student/profile").with(as("student.demo")))),
                "$.student.id");

        mvc.perform(post("/api/teacher/grade-grids").with(as("teacher.demo")).contentType(MediaType.APPLICATION_JSON)
                        .content(openGrid(assignmentId, periodId, "PROJECT")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("grades.assessmentType"));

        String testGrid = gradeEveryone(assignmentId, periodId, "TEST", studentId, "14");
        String examGrid = gradeEveryone(assignmentId, periodId, "EXAM", studentId, "12");

        mvc.perform(get("/api/student/grades").with(as("student.demo")))
                .andExpect(jsonPath("$[*].subjectCode", org.hamcrest.Matchers.not(hasItem("GL-ARCHI"))));
        for (String grid : List.of(testGrid, examGrid)) {
            mvc.perform(post("/api/examens/grade-grids/" + grid + "/publish").with(as("superadmin")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PUBLISHED"));
        }
        mvc.perform(get("/api/student/grades").with(as("student.demo")))
                .andExpect(jsonPath("$[?(@.subjectCode == 'GL-ARCHI' && @.assessmentType == 'TEST')].score",
                        hasItem(14.0)));

        String classPeriod = "{\"schoolClassId\": \"" + demoClassId() + "\", \"academicPeriodId\": \"" + periodId + "\"}";
        mvc.perform(post("/api/examens/report-cards/generate").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content(classPeriod))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.studentId == '" + studentId + "')].average", hasItem(12.8)))
                .andExpect(jsonPath("$[?(@.studentId == '" + studentId + "')].appreciation", hasItem("Assez bien")))
                .andExpect(jsonPath("$[?(@.studentId == '" + studentId + "')].creditsEarned", hasItem(4)));
        mvc.perform(get("/api/student/report-cards").with(as("student.demo")))
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(post("/api/examens/report-cards/publish").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content(classPeriod))
                .andExpect(status().isOk());
        mvc.perform(get("/api/student/report-cards").with(as("student.demo")))
                .andExpect(jsonPath("$[0].average").value(12.8))
                .andExpect(jsonPath("$[0].subjects[0].subjectCode").value("GL-ARCHI"))
                .andExpect(jsonPath("$[0].subjects[0].validated").value(true));
    }

    /** Opens the grid, checks the rules, gives the demo student the score and 10 to the others, and confirms. */
    private String gradeEveryone(String assignmentId, String periodId, String type, String studentId, String score)
            throws Exception {
        String grid = body(mvc.perform(post("/api/teacher/grade-grids").with(as("teacher.demo"))
                        .contentType(MediaType.APPLICATION_JSON).content(openGrid(assignmentId, periodId, type)))
                .andExpect(status().isOk()));
        String gridId = read(grid, "$.id");
        mvc.perform(post("/api/teacher/grade-grids/" + gridId + "/confirm").with(as("teacher.demo")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("grades.incomplete"));
        mvc.perform(put("/api/teacher/grade-grids/" + gridId + "/grades").with(as("teacher.demo"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grades\": [{\"studentId\": \"" + studentId + "\", \"score\": 25}]}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("grades.score"));

        List<String> students = read(grid, "$.rows[*].studentId");
        String grades = students.stream()
                .map(id -> "{\"studentId\": \"" + id + "\", \"score\": " + (id.equals(studentId) ? score : "10") + "}")
                .collect(Collectors.joining(","));
        mvc.perform(put("/api/teacher/grade-grids/" + gridId + "/grades").with(as("teacher.demo"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"grades\": [" + grades + "]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        mvc.perform(post("/api/teacher/grade-grids/" + gridId + "/confirm").with(as("teacher.demo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
        return gridId;
    }

    private static String openGrid(String assignmentId, String periodId, String type) {
        return """
                {"teachingAssignmentId": "%s", "academicPeriodId": "%s", "assessmentType": "%s"}
                """.formatted(assignmentId, periodId, type);
    }

    private String archiAssignment() throws Exception {
        String assignments = body(mvc.perform(get("/api/teacher/assignments").with(as("teacher.demo"))));
        List<Object> archi = read(assignments, "$[?(@.subjectCode == 'GL-ARCHI')]");
        return com.jayway.jsonpath.JsonPath.parse(archi.getFirst()).jsonString();
    }

    private String firstPeriodId() throws Exception {
        return read(body(mvc.perform(get("/api/parametrage/academic-years").with(as("superadmin")))),
                "$[0].periods[0].id");
    }
}
