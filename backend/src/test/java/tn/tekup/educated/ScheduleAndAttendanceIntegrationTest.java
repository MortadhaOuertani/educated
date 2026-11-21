package tn.tekup.educated;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import tn.tekup.educated.support.IntegrationTest;

class ScheduleAndAttendanceIntegrationTest extends IntegrationTest {

    @Test
    void demoTimetableCreatesSessionsAndSlotsCannotClash() throws Exception {
        mvc.perform(get("/api/teacher/sessions?from=2026-11-16&to=2026-11-22").with(as("teacher.demo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].roomCode").value("B-213"));
        mvc.perform(get("/api/student/timetable?from=2026-11-16&to=2026-11-16").with(as("student.demo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].subjectCode").value("GL-ARCHI"))
                .andExpect(jsonPath("$[0].startTime").value("08:30:00"));

        String periodId = read(body(mvc.perform(get("/api/parametrage/academic-years").with(as("superadmin")))),
                "$[0].periods[0].id");
        String timetable = body(mvc.perform(post("/api/emploidutemps/timetables").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"schoolClassId": "%s", "academicPeriodId": "%s"}
                                """.formatted(demoClassId(), periodId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.versionNumber").value(2)));
        String timetableId = read(timetable, "$.id");
        String assignmentId = read(body(mvc.perform(get("/api/teacher/assignments").with(as("teacher.demo")))),
                "$[0].id");
        String slot = """
                {"teachingAssignmentId": "%s", "dayOfWeek": "TUESDAY", "timeSlotCode": "S2"}
                """.formatted(assignmentId);
        mvc.perform(post("/api/emploidutemps/timetables/" + timetableId + "/slots").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content(slot))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startTime").value("10:15:00"));
        mvc.perform(post("/api/emploidutemps/timetables/" + timetableId + "/slots").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content(slot))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("slot.conflict"));

        String sessionId = read(body(mvc.perform(get("/api/teacher/sessions?from=2026-12-02&to=2026-12-02")
                .with(as("teacher.demo")))), "$[0].id");
        mvc.perform(post("/api/emploidutemps/sessions/" + sessionId + "/cancel").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"reason\": \"Grève\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        mvc.perform(post("/api/emploidutemps/sessions/" + sessionId + "/cancel").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"reason\": \"Grève\"}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("session.notScheduled"));
    }

    @Test
    void teacherTakesTheCallAndTheStudentJustifiesTheAbsence() throws Exception {
        String sessionId = read(body(mvc.perform(get("/api/teacher/sessions?from=2026-11-16&to=2026-11-16")
                .with(as("teacher.demo")))), "$[0].id");
        String studentId = read(body(mvc.perform(get("/api/student/profile").with(as("student.demo")))),
                "$.student.id");
        String call = "/api/teacher/sessions/" + sessionId + "/attendance";

        mvc.perform(get(call).with(as("teacher.demo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"));
        mvc.perform(put(call).with(as("teacher.demo")).contentType(MediaType.APPLICATION_JSON).content("""
                        {"records": [{"studentId": "%s", "status": "ABSENT"}]}
                        """.formatted(studentId)))
                .andExpect(status().isOk());
        mvc.perform(post(call + "/confirm").with(as("teacher.demo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.editable").value(true));
        mvc.perform(post(call + "/confirm").with(as("teacher.demo")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("attendance.alreadyConfirmed"));

        String futureId = read(body(mvc.perform(get("/api/teacher/sessions?from=2027-01-11&to=2027-01-11")
                .with(as("teacher.demo")))), "$[0].id");
        mvc.perform(get("/api/teacher/sessions/" + futureId + "/attendance").with(as("teacher.demo")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("attendance.future"));

        mvc.perform(get("/api/student/absences").with(as("student.demo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.absences[0].status").value("ABSENT"))
                .andExpect(jsonPath("$.absences[0].justified").value(false))
                .andExpect(jsonPath("$.statistics[0].level").value("CRITICAL"));

        String request = body(mvc.perform(multipart("/api/student/absence-requests").with(as("student.demo"))
                        .param("fromDateTime", "2026-11-16T08:00:00").param("toDateTime", "2026-11-16T12:00:00")
                        .param("reason", "Certificat médical"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING")));
        mvc.perform(patch("/api/assiduite/absence-requests/" + read(request, "$.id")).with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"APPROVED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
        mvc.perform(get("/api/student/absences").with(as("student.demo")))
                .andExpect(jsonPath("$.absences[0].justified").value(true))
                .andExpect(jsonPath("$.statistics[0].level").value("OK"));

        String teacherId = read(body(mvc.perform(get("/api/teacher/profile").with(as("teacher.demo")))), "$.id");
        String hours = body(mvc.perform(get("/api/assiduite/teacher-hours?teacherId=" + teacherId
                        + "&from=2026-11-16&to=2026-11-16").with(as("superadmin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hours").value(1.5))
                .andExpect(jsonPath("$[0].validated").value(false)));
        String hoursId = read(hours, "$[0].id");
        mvc.perform(post("/api/assiduite/teacher-hours/" + hoursId + "/validate").with(as("superadmin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validated").value(true));
        mvc.perform(post("/api/assiduite/teacher-hours/" + hoursId + "/validate").with(as("superadmin")))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void studentsCannotTakeCallsAndStaffNeedRights() throws Exception {
        String sessionId = read(body(mvc.perform(get("/api/teacher/sessions?from=2026-11-18&to=2026-11-18")
                .with(as("teacher.demo")))), "$[0].id");
        mvc.perform(get("/api/teacher/sessions/" + sessionId + "/attendance").with(as("student.demo")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/emploidutemps/timetables?schoolClassId=" + demoClassId()).with(as("teacher.demo")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/assiduite/absence-requests").with(as("scolarite.agent")))
                .andExpect(status().isOk());
    }
}
