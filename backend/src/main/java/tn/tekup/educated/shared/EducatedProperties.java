package tn.tekup.educated.shared;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Application settings under the {@code educated.*} prefix. */
@ConfigurationProperties(prefix = "educated")
public record EducatedProperties(Cors cors, Storage storage, Bootstrap bootstrap) {

    public record Cors(List<String> allowedOrigins) {
    }

    /** S3-compatible object storage (SeaweedFS in development). */
    public record Storage(String endpoint, String region, String accessKey, String secretKey, String bucket) {
    }

    /**
     * First-start data: the default establishment, the user who becomes Super Admin on first login,
     * and optional demo data for local development.
     */
    public record Bootstrap(
            String establishmentCode,
            String establishmentName,
            String superAdminUsername,
            boolean demoData) {
    }
}
