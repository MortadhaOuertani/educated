package tn.tekup.educated.support;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tn.tekup.educated.shared.files.FileStorage;

/** Starts the whole application on embedded PostgreSQL. Requests carry a fake Keycloak token. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(EmbeddedPostgresConfig.class)
public abstract class IntegrationTest {

    @Autowired
    protected MockMvc mvc;

    /** Uploads are not sent to SeaweedFS in tests; stub {@code store} where a test uploads a file. */
    @MockitoBean
    protected FileStorage fileStorage;

    /** A token like the one Keycloak issues for this username. */
    protected static JwtRequestPostProcessor as(String username) {
        return jwt().jwt(token -> token
                .subject(UUID.nameUUIDFromBytes(username.getBytes(StandardCharsets.UTF_8)).toString())
                .claim("preferred_username", username)
                .claim("email", username + "@test.local")
                .claim("given_name", username)
                .claim("family_name", "Test"));
    }

    protected static String body(ResultActions result) throws Exception {
        return result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    protected static <T> T read(String json, String path) {
        return JsonPath.read(json, path);
    }

    /** Id of the current academic year (2026-2027 in the demo data). */
    protected String currentYearId() throws Exception {
        return read(body(mvc.perform(get("/api/parametrage/academic-years").with(as("superadmin")))), "$[0].id");
    }

    /** The demo class ING-5-S-GLSI-B: the demo student is enrolled in it and the demo teacher teaches it. */
    protected String demoClassId() throws Exception {
        return read(currentYearClasses(), "$[0].id");
    }

    protected String demoStudyLevelId() throws Exception {
        return read(currentYearClasses(), "$[0].studyLevelId");
    }

    private String currentYearClasses() throws Exception {
        return body(mvc.perform(get("/api/parametrage/school-classes?academicYearId=" + currentYearId())
                .with(as("superadmin"))));
    }

    /** Signs the user in once so their account exists, and returns the account id. */
    protected String signIn(String username) throws Exception {
        return read(body(mvc.perform(get("/api/me").with(as(username)))), "$.userId");
    }
}
