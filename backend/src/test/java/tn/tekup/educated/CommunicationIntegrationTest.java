package tn.tekup.educated;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import tn.tekup.educated.support.IntegrationTest;

class CommunicationIntegrationTest extends IntegrationTest {

    @Test
    void eventsOfOtherModulesBecomeNotifications() throws Exception {
        String types = body(mvc.perform(get("/api/scolarite/document-types").with(as("student.demo"))));
        List<String> ids = read(types, "$[?(@.code == 'ATTESTATION_PRESENCE')].id");
        String request = read(body(mvc.perform(multipart("/api/student/document-requests").with(as("student.demo"))
                .param("documentTypeId", ids.getFirst()))), "$.id");
        mvc.perform(patch("/api/scolarite/document-requests/" + request + "/status").with(as("scolarite.agent"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"IN_PROGRESS\"}"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/me/notifications").with(as("student.demo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("DOCUMENT"))
                .andExpect(jsonPath("$[0].body").value("Votre demande est maintenant : en traitement."));
        mvc.perform(post("/api/me/notifications/read-all").with(as("student.demo"))).andExpect(status().isNoContent());
        mvc.perform(get("/api/me/notifications/unread-count").with(as("student.demo")))
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void messagesReachTheChosenClassAndRespectPreferences() throws Exception {
        mvc.perform(put("/api/me/notification-preferences").with(as("student.demo"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"MESSAGE\", \"inApp\": false, \"push\": false, \"email\": true, \"sms\": false}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/communication/messages").with(as("scolarite.agent"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"subject": "Réunion de classe", "body": "Jeudi à 14h", "audience": "STUDENTS",
                                 "category": "SCOLARITE", "schoolClassId": "%s"}
                                """.formatted(demoClassId())))
                .andExpect(status().isCreated());

        String inbox = body(mvc.perform(get("/api/me/messages").with(as("student.demo")))
                .andExpect(jsonPath("$[*].subject", hasItem("Réunion de classe"))));
        mvc.perform(post("/api/me/messages/" + read(inbox, "$[0].id") + "/read").with(as("student.demo")))
                .andExpect(jsonPath("$.readAt").isNotEmpty());
        mvc.perform(get("/api/me/notifications").with(as("student.demo")))
                .andExpect(jsonPath("$[*].title", not(hasItem("Réunion de classe"))));
        mvc.perform(get("/api/me/messages").with(as("teacher.demo")))
                .andExpect(jsonPath("$[*].subject", not(hasItem("Réunion de classe"))));
        mvc.perform(post("/api/communication/messages").with(as("student.demo"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subject\": \"x\", \"audience\": \"ALL\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void newsAreShownToTheirAudience() throws Exception {
        String news = read(body(mvc.perform(post("/api/communication/news").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Conseil pédagogique\", \"audience\": \"TEACHERS\"}"))
                .andExpect(status().isCreated())), "$.id");
        mvc.perform(get("/api/me/news").with(as("teacher.demo")))
                .andExpect(jsonPath("$[*].title", not(hasItem("Conseil pédagogique"))));
        mvc.perform(post("/api/communication/news/" + news + "/publish").with(as("superadmin")))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
        mvc.perform(get("/api/me/news").with(as("teacher.demo")))
                .andExpect(jsonPath("$[*].title", hasItem("Conseil pédagogique")));
        mvc.perform(get("/api/me/news").with(as("student.demo")))
                .andExpect(jsonPath("$[*].title", not(hasItem("Conseil pédagogique"))));
        mvc.perform(get("/api/me/notifications").with(as("teacher.demo")))
                .andExpect(jsonPath("$[*].title", hasItem("Conseil pédagogique")));
    }

    @Test
    void onlyParticipantsReadADiscussion() throws Exception {
        String studentUserId = signIn("student.demo");
        String conversation = read(body(mvc.perform(post("/api/me/conversations").with(as("teacher.demo"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"userId\": \"" + studentUserId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("TEACHER_STUDENT"))), "$.id");
        mvc.perform(post("/api/me/conversations/" + conversation + "/messages").with(as("teacher.demo"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\": \"Bonjour, votre rapport ?\"}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/me/conversations").with(as("student.demo")))
                .andExpect(jsonPath("$[*].id", hasItem(conversation)));
        mvc.perform(get("/api/me/conversations/" + conversation + "/messages").with(as("student.demo")))
                .andExpect(jsonPath("$[0].content").value("Bonjour, votre rapport ?"));
        mvc.perform(get("/api/me/conversations/" + conversation + "/messages").with(as("scolarite.agent")))
                .andExpect(status().isNotFound());
    }
}
