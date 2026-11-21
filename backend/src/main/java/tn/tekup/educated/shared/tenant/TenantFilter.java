package tn.tekup.educated.shared.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import tn.tekup.educated.shared.security.CurrentUser;
import tn.tekup.educated.shared.security.CurrentUserHolder;
import tn.tekup.educated.shared.security.CurrentUserProvider;
import tn.tekup.educated.shared.security.TenantAccess;

/**
 * Runs after the bearer token is validated. Resolves the application user, then the establishment:
 * the {@value #HEADER} header chosen in the UI (checked against the user's access), otherwise the user's
 * default establishment. Without one, the request runs with {@link TenantContext#NONE} and sees no data.
 */
public class TenantFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Establishment-Id";

    private final CurrentUserProvider currentUserProvider;
    private final TenantAccess tenantAccess;

    public TenantFilter(CurrentUserProvider currentUserProvider, TenantAccess tenantAccess) {
        this.currentUserProvider = currentUserProvider;
        this.tenantAccess = tenantAccess;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            chain.doFilter(request, response);
            return;
        }
        try {
            Optional<CurrentUser> user = currentUserProvider.current();
            if (user.isEmpty()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Account is disabled");
                return;
            }
            CurrentUserHolder.set(user.get());
            UUID establishment;
            String requested = request.getHeader(HEADER);
            if (requested != null && !requested.isBlank()) {
                try {
                    establishment = UUID.fromString(requested.trim());
                } catch (IllegalArgumentException invalid) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid " + HEADER + " header");
                    return;
                }
                if (!tenantAccess.canAccess(user.get(), establishment)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "No access to this establishment");
                    return;
                }
            } else {
                establishment = tenantAccess.defaultEstablishment(user.get()).orElse(TenantContext.NONE);
            }
            TenantContext.set(establishment);
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            CurrentUserHolder.clear();
        }
    }
}
