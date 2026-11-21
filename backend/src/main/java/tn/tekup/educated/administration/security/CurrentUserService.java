package tn.tekup.educated.administration.security;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import tn.tekup.educated.administration.domain.DisplayDensity;
import tn.tekup.educated.administration.domain.ThemeMode;
import tn.tekup.educated.administration.domain.UserAccount;
import tn.tekup.educated.administration.domain.UserAccountRepository;
import tn.tekup.educated.administration.domain.UserRole;
import tn.tekup.educated.administration.domain.UserRoleRepository;
import tn.tekup.educated.administration.service.RoleAssignments;
import tn.tekup.educated.administration.setup.SystemRoles;
import tn.tekup.educated.shared.EducatedProperties;
import tn.tekup.educated.shared.security.CurrentUser;
import tn.tekup.educated.shared.security.CurrentUserHolder;
import tn.tekup.educated.shared.security.CurrentUserProvider;
import tn.tekup.educated.shared.security.EstablishmentDirectory;
import tn.tekup.educated.shared.security.TenantAccess;
import tn.tekup.educated.shared.tenant.TenantContext;

/**
 * Turns the Keycloak token into an application user. On first sign-in the account is created, or an
 * account prepared in advance with the same username is linked to the Keycloak user.
 */
@Service
class CurrentUserService implements CurrentUserProvider, TenantAccess {

    private final UserAccountRepository accounts;
    private final UserRoleRepository userRoles;
    private final RoleAssignments roleAssignments;
    private final EstablishmentDirectory establishments;
    private final String superAdminUsername;
    private final TransactionTemplate tx;

    CurrentUserService(UserAccountRepository accounts, UserRoleRepository userRoles, RoleAssignments roleAssignments,
            EstablishmentDirectory establishments, EducatedProperties properties,
            PlatformTransactionManager transactionManager) {
        this.accounts = accounts;
        this.userRoles = userRoles;
        this.roleAssignments = roleAssignments;
        this.establishments = establishments;
        this.superAdminUsername = properties.bootstrap().superAdminUsername();
        this.tx = new TransactionTemplate(transactionManager);
        this.tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public Optional<CurrentUser> current() {
        Optional<CurrentUser> alreadyResolved = CurrentUserHolder.get();
        if (alreadyResolved.isPresent()) {
            return alreadyResolved;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return Optional.empty();
        }
        UserAccount account = TenantContext.callAsRoot(() -> tx.execute(status -> findOrCreate(jwt)));
        if (account.getUsername().equalsIgnoreCase(superAdminUsername)) {
            grantSuperAdminIfMissing(account);
        }
        if (!account.isActive()) {
            return Optional.empty();
        }
        boolean administrative = rolesValidToday(account.getId()).stream()
                .anyMatch(userRole -> userRole.getRole().isAdministrative());
        return Optional.of(new CurrentUser(account.getId(), account.getKeycloakId(), account.getUsername(),
                account.getEmail(), jwt.getClaimAsString("given_name"), jwt.getClaimAsString("family_name"),
                account.getPersonType(), account.getPersonId(), administrative));
    }

    @Override
    public Set<UUID> accessibleEstablishments(CurrentUser user) {
        if (user.administrative()) {
            return new LinkedHashSet<>(establishments.allEstablishmentIds());
        }
        Set<UUID> result = new LinkedHashSet<>();
        rolesValidToday(user.userId()).forEach(userRole -> result.add(userRole.getEstablishmentId()));
        return result;
    }

    @Override
    public Optional<UUID> defaultEstablishment(CurrentUser user) {
        return accessibleEstablishments(user).stream().findFirst();
    }

    /** Roles in every establishment (root tenant), valid today. */
    private List<UserRole> rolesValidToday(UUID userAccountId) {
        LocalDate today = LocalDate.now();
        return TenantContext.callAsRoot(() -> tx.execute(status -> userRoles.findAllByUserAccount_Id(userAccountId)
                .stream()
                .filter(userRole -> RoleAssignments.isValidOn(userRole, today))
                .toList()));
    }

    private UserAccount findOrCreate(Jwt jwt) {
        UUID keycloakId = UUID.fromString(jwt.getSubject());
        Optional<UserAccount> existing = accounts.findByKeycloakId(keycloakId);
        if (existing.isPresent()) {
            return existing.get();
        }
        String username = jwt.getClaimAsString("preferred_username");
        UserAccount account = accounts.findByUsernameIgnoreCase(username)
                .filter(prepared -> prepared.getKeycloakId() == null)
                .orElseGet(() -> newAccount(username, jwt.getClaimAsString("email")));
        account.setKeycloakId(keycloakId);
        account.setLastLoginAt(Instant.now());
        return accounts.save(account);
    }

    private static UserAccount newAccount(String username, String email) {
        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setEmail(email);
        account.setActive(true);
        account.setTheme(ThemeMode.SYSTEM);
        account.setDisplayDensity(DisplayDensity.NORMAL);
        account.setPreferredLocale("fr");
        return account;
    }

    /** The configured super admin gets the Super Admin role in the first establishment, in its own transaction. */
    private void grantSuperAdminIfMissing(UserAccount account) {
        boolean alreadyAdmin = rolesValidToday(account.getId()).stream().anyMatch(r -> r.getRole().isAdministrative());
        if (alreadyAdmin) {
            return;
        }
        establishments.allEstablishmentIds().stream().findFirst().ifPresent(establishmentId ->
                TenantContext.runAs(establishmentId, () -> tx.executeWithoutResult(status ->
                        roleAssignments.assign(accounts.getReferenceById(account.getId()), SystemRoles.SUPER_ADMIN))));
    }
}
