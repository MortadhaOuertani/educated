package tn.tekup.educated.shared.audit;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tn.tekup.educated.shared.security.CurrentUser;
import tn.tekup.educated.shared.security.CurrentUserHolder;
import tn.tekup.educated.shared.tenant.TenantContext;

/** Writes {@link AuditLog} entries in the caller's transaction, so they exist only if the action commits. */
@Component
public class AuditLogger {

    private final AuditLogRepository repository;

    AuditLogger(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void record(String moduleName, String action, String entityType, UUID entityId, String changesJson) {
        String ip = null;
        String agent = null;
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            ip = request.getRemoteAddr();
            agent = request.getHeader("User-Agent");
        }
        UUID actor = CurrentUserHolder.get().map(CurrentUser::userId).orElse(null);
        UUID establishment = TenantContext.currentEstablishment().orElse(null);
        repository.save(new AuditLog(establishment, moduleName, action, entityType, entityId, changesJson, actor, ip,
                agent == null ? null : agent.substring(0, Math.min(agent.length(), 512))));
    }
}
