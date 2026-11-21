package tn.tekup.educated;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import tn.tekup.educated.support.IntegrationTest;

class AccessControlIntegrationTest extends IntegrationTest {

    @Test
    void superAdminIsSetUpOnFirstSignInAndSeesEverything() throws Exception {
        mvc.perform(get("/api/me").with(as("superadmin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.administrative").value(true))
                .andExpect(jsonPath("$.establishmentId").isNotEmpty());

        mvc.perform(get("/api/parametrage/academic-years").with(as("superadmin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("2026-2027"))
                .andExpect(jsonPath("$[0].current").value(true))
                .andExpect(jsonPath("$[0].periods.length()").value(2));

        mvc.perform(get("/api/parametrage/establishments").with(as("superadmin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("TEKUP"));
    }

    @Test
    void userWithoutRoleCanSignInButSeesNoData() throws Exception {
        mvc.perform(get("/api/me").with(as("nobody")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.administrative").value(false))
                .andExpect(jsonPath("$.establishmentId").doesNotExist());

        mvc.perform(get("/api/parametrage/academic-years").with(as("nobody")))
                .andExpect(status().isForbidden());
    }

    @Test
    void requestsWithoutTokenAreRejected() throws Exception {
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void scolariteAgentOnlyGetsTheRoleGrants() throws Exception {
        String agentId = signIn("agent.scolarite");
        signIn("superadmin");
        String scolariteRoleId = roleId("Scolarité");

        mvc.perform(post("/api/administration/users/" + agentId + "/roles").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"roleId\":\"" + scolariteRoleId + "\"}"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/parametrage/academic-years").with(as("agent.scolarite")))
                .andExpect(status().isOk());
        mvc.perform(post("/api/parametrage/academic-years").with(as("agent.scolarite"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"2027-2028\",\"startDate\":\"2027-09-01\",\"endDate\":\"2028-07-31\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/administration/roles").with(as("agent.scolarite")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/me").with(as("agent.scolarite")))
                .andExpect(jsonPath("$.visibleModules", hasItem("SCOLARITE")))
                .andExpect(jsonPath("$.visibleModules", not(hasItem("FINANCES"))))
                .andExpect(jsonPath("$.permissions.Student", hasItem("READ")))
                .andExpect(jsonPath("$.permissions.Student", not(hasItem("DELETE"))));
    }

    @Test
    void nobodyCanChangeTheirOwnAccess() throws Exception {
        String superAdminId = signIn("superadmin");
        mvc.perform(post("/api/administration/users/" + superAdminId + "/roles").with(as("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"roleId\":\"" + roleId("Enseignant") + "\"}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("user.selfChange"));
    }

    @Test
    void grantingAnAdminRoleNeedsASecondSuperAdmin() throws Exception {
        String candidateId = signIn("candidate.admin");
        signIn("superadmin");

        String request = body(mvc.perform(post("/api/administration/users/" + candidateId + "/roles")
                        .with(as("superadmin")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleId\":\"" + roleId("Super Admin") + "\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDING")));

        mvc.perform(post("/api/administration/role-change-requests/" + read(request, "$.id") + "/approve")
                        .with(as("superadmin")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("roleChange.sameUser"));

        mvc.perform(get("/api/me").with(as("candidate.admin")))
                .andExpect(jsonPath("$.administrative").value(false));
    }

    private String roleId(String name) throws Exception {
        String roles = body(mvc.perform(get("/api/administration/roles").with(as("superadmin"))));
        List<String> ids = read(roles, "$[?(@.name == '" + name + "')].id");
        return ids.getFirst();
    }
}
