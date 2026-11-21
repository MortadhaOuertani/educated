package tn.tekup.educated.shared.audit;

import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import tn.tekup.educated.shared.security.CurrentUser;
import tn.tekup.educated.shared.security.CurrentUserHolder;

/** Fills createdAt/updatedAt and createdByUserId/updatedByUserId on every entity. */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    @Bean
    AuditorAware<UUID> auditorAware() {
        return () -> CurrentUserHolder.get().map(CurrentUser::userId);
    }
}
