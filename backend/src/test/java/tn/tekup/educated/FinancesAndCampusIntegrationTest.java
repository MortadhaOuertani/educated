package tn.tekup.educated;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import tn.tekup.educated.shared.tenant.TenantContext;
import tn.tekup.educated.support.IntegrationTest;
import tn.tekup.educated.vieetablissement.domain.SurveyResponse;
import tn.tekup.educated.vieetablissement.domain.SurveyResponseRepository;

class FinancesAndCampusIntegrationTest extends IntegrationTest {

    @Autowired
    SurveyResponseRepository surveyResponses;

    @Test
    void feesAreSplitIntoInstallmentsAndPaymentsPayTheOldestFirst() throws Exception {
        String yearId = currentYearId();
        String levelId = demoStudyLevelId();
        String fee = """
                {"academicYearId": "%s", "studyLevelId": "%s", "feeType": "%s", "amount": %s,
                 "installmentsCount": %s}
                """;
        mvc.perform(post("/api/finances/fee-schedules").with(as("superadmin")).contentType(MediaType.APPLICATION_JSON)
                        .content(fee.formatted(yearId, levelId, "TUITION", "6000", "10")))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/finances/fee-schedules").with(as("superadmin")).contentType(MediaType.APPLICATION_JSON)
                        .content(fee.formatted(yearId, levelId, "REGISTRATION", "300", "1")))
                .andExpect(status().isCreated());

        String studentId = read(body(mvc.perform(get("/api/student/profile").with(as("student.demo")))), "$.student.id");
        mvc.perform(post("/api/finances/students/" + studentId + "/fee-plan").with(as("superadmin")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(6300.0))
                .andExpect(jsonPath("$.installments.length()").value(10))
                .andExpect(jsonPath("$.installments[0].amount").value(630.0))
                .andExpect(jsonPath("$.installments[0].dueDate").value("2026-09-15"));
        mvc.perform(post("/api/finances/students/" + studentId + "/fee-plan").with(as("superadmin")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("feePlan.exists"));

        String payment = "{\"studentId\": \"" + studentId + "\", \"amount\": %s, \"method\": \"CASH\"}";
        mvc.perform(post("/api/finances/payments").with(as("superadmin")).contentType(MediaType.APPLICATION_JSON)
                        .content(payment.formatted("999999")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("payment.tooMuch"));
        mvc.perform(post("/api/finances/payments").with(as("superadmin")).contentType(MediaType.APPLICATION_JSON)
                        .content(payment.formatted("1000")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.receiptNumber").value(org.hamcrest.Matchers.startsWith("REC-")));
        mvc.perform(get("/api/student/payments").with(as("student.demo")))
                .andExpect(jsonPath("$.plans[0].paidAmount").value(1000.0))
                .andExpect(jsonPath("$.plans[0].installments[0].status").value("PAID"))
                .andExpect(jsonPath("$.plans[0].installments[1].status").value("PARTIALLY_PAID"))
                .andExpect(jsonPath("$.payments[0].amount").value(1000.0));

        String overdue = body(mvc.perform(get("/api/finances/installments/overdue").with(as("superadmin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].matricule", hasItem("DEMO-0001"))));
        List<String> overdueIds = read(overdue, "$[?(@.matricule == 'DEMO-0001')].id");
        mvc.perform(post("/api/finances/installments/" + overdueIds.getFirst() + "/reminders").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"channel\": \"SMS\", \"message\": \"Rappel\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.level").value(1));

        // A student enrolled after the fees were set gets a plan automatically.
        String newStudent = read(body(mvc.perform(post("/api/scolarite/students").with(as("superadmin"))
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"matricule": "F-2001", "person": {"firstName": "Sara", "lastName": "Mabrouk"}}
                        """))), "$.id");
        mvc.perform(post("/api/scolarite/students/" + newStudent + "/enrollments").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"schoolClassId": "%s", "enrollmentType": "NEW", "enrollmentDate": "2026-09-01"}
                                """.formatted(demoClassId())))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/finances/students/" + newStudent + "/account").with(as("superadmin")))
                .andExpect(jsonPath("$.plans[0].totalAmount").value(6300.0));
    }

    @Test
    void validatedTeacherHoursArePaidOnce() throws Exception {
        String sessionId = read(body(mvc.perform(get("/api/teacher/sessions?from=2026-11-02&to=2026-11-02")
                .with(as("teacher.demo")))), "$[0].id");
        mvc.perform(get("/api/teacher/sessions/" + sessionId + "/attendance").with(as("teacher.demo")));
        mvc.perform(post("/api/teacher/sessions/" + sessionId + "/attendance/confirm").with(as("teacher.demo")))
                .andExpect(status().isOk());
        String teacherId = read(body(mvc.perform(get("/api/teacher/profile").with(as("teacher.demo")))), "$.id");
        String hoursId = read(body(mvc.perform(get("/api/assiduite/teacher-hours?teacherId=" + teacherId
                + "&from=2026-11-02&to=2026-11-02").with(as("superadmin")))), "$[0].id");

        String request = "{\"teacherId\": \"" + teacherId + "\", \"from\": \"2026-11-02\", \"to\": \"2026-11-02\"}";
        mvc.perform(post("/api/finances/teacher-payments").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("teacherPayment.nothingToPay"));
        mvc.perform(post("/api/assiduite/teacher-hours/" + hoursId + "/validate").with(as("superadmin")))
                .andExpect(status().isOk());

        String paymentId = read(body(mvc.perform(post("/api/finances/teacher-payments").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalHours").value(1.5))
                .andExpect(jsonPath("$.totalAmount").value(67.5))
                .andExpect(jsonPath("$.status").value("PENDING"))), "$.id");
        mvc.perform(post("/api/finances/teacher-payments/" + paymentId + "/pay").with(as("superadmin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
        mvc.perform(post("/api/finances/teacher-payments").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isUnprocessableContent());
        mvc.perform(post("/api/finances/teacher-payments/" + paymentId + "/cancel").with(as("superadmin")))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        mvc.perform(post("/api/finances/teacher-payments").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated());
    }

    @Test
    void anonymousSurveyAnswersKeepNoAuthor() throws Exception {
        String survey = body(mvc.perform(post("/api/vie-etablissement/surveys").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"title": "Satisfaction cantine", "audience": "STUDENTS", "anonymous": true,
                                 "questions": [{"text": "Votre note ?", "questionType": "RATING", "required": true}]}
                                """))
                .andExpect(status().isCreated()));
        String surveyId = read(survey, "$.id");
        String questionId = read(survey, "$.questions[0].id");

        mvc.perform(get("/api/me/surveys").with(as("student.demo")))
                .andExpect(jsonPath("$[*].id", hasItem(surveyId)));
        mvc.perform(get("/api/me/surveys").with(as("teacher.demo")))
                .andExpect(jsonPath("$[*].id", not(hasItem(surveyId))));
        mvc.perform(post("/api/me/surveys/" + surveyId + "/responses").with(as("student.demo"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"answers\": {}}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("survey.required"));
        mvc.perform(post("/api/me/surveys/" + surveyId + "/responses").with(as("student.demo"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answers\": {\"" + questionId + "\": \"4\"}}"))
                .andExpect(status().isCreated());

        String establishmentId = read(body(mvc.perform(get("/api/me").with(as("superadmin")))), "$.establishmentId");
        List<SurveyResponse> saved = TenantContext.callAs(UUID.fromString(establishmentId),
                () -> surveyResponses.findAllBySurvey_Id(UUID.fromString(surveyId)));
        assertThat(saved).hasSize(1);
        assertThat(saved.getFirst().getRespondentUserId()).isNull();
        assertThat(saved.getFirst().getCreatedByUserId()).isNull();
    }

    @Test
    void canteenInfirmaryAndEntryTickets() throws Exception {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String menu = "{\"menuDate\": \"%s\", \"mealType\": \"LUNCH\", \"items\": \"Couscous\", \"price\": 2.5}";
        String menuId = read(body(mvc.perform(post("/api/vie-etablissement/canteen-menus").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content(menu.formatted(tomorrow)))
                .andExpect(status().isCreated())), "$.id");
        String todayMenu = read(body(mvc.perform(post("/api/vie-etablissement/canteen-menus").with(as("superadmin"))
                .contentType(MediaType.APPLICATION_JSON).content(menu.formatted(LocalDate.now())))), "$.id");

        String reservation = read(body(mvc.perform(post("/api/me/meal-reservations").with(as("student.demo"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"menuId\": \"" + menuId + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RESERVED"))), "$.id");
        mvc.perform(post("/api/me/meal-reservations").with(as("student.demo"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"menuId\": \"" + menuId + "\"}"))
                .andExpect(jsonPath("$.code").value("canteen.alreadyReserved"));
        mvc.perform(post("/api/me/meal-reservations").with(as("student.demo"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"menuId\": \"" + todayMenu + "\"}"))
                .andExpect(jsonPath("$.code").value("canteen.tooLate"));
        mvc.perform(get("/api/vie-etablissement/canteen-menus?from=" + tomorrow + "&to=" + tomorrow)
                        .with(as("superadmin")))
                .andExpect(jsonPath("$[0].reserved").value(1));
        mvc.perform(delete("/api/me/meal-reservations/" + reservation).with(as("student.demo")))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        String studentId = read(body(mvc.perform(get("/api/student/profile").with(as("student.demo")))), "$.student.id");
        mvc.perform(post("/api/vie-etablissement/infirmary-visits").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\": \"" + studentId + "\", \"reason\": \"Maux de tête\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/vie-etablissement/entry-tickets").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\": \"" + studentId + "\", \"reason\": \"LATE_ARRIVAL\"}"))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/student/entry-tickets").with(as("student.demo")))
                .andExpect(jsonPath("$[0].reason").value("LATE_ARRIVAL"));
        mvc.perform(get("/api/vie-etablissement/infirmary-visits").with(as("student.demo")))
                .andExpect(status().isForbidden());
    }
}
