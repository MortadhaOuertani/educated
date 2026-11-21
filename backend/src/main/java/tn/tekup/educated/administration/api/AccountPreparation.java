package tn.tekup.educated.administration.api;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.administration.domain.DisplayDensity;
import tn.tekup.educated.administration.domain.ThemeMode;
import tn.tekup.educated.administration.domain.UserAccount;
import tn.tekup.educated.administration.domain.UserAccountRepository;
import tn.tekup.educated.administration.service.RoleAssignments;
import tn.tekup.educated.shared.domain.PersonType;

/**
 * Prepares an account before its owner first signs in: links it to a person and gives a role in the current
 * establishment. When the person signs in with Keycloak, the account is matched by username.
 */
@Service
public class AccountPreparation {

    private final UserAccountRepository accounts;
    private final RoleAssignments roleAssignments;

    AccountPreparation(UserAccountRepository accounts, RoleAssignments roleAssignments) {
        this.accounts = accounts;
        this.roleAssignments = roleAssignments;
    }

    @Transactional
    public UUID prepare(String username, String email, PersonType personType, UUID personId, String roleName) {
        UserAccount account = accounts.findByUsernameIgnoreCase(username).orElseGet(() -> {
            UserAccount created = new UserAccount();
            created.setUsername(username);
            created.setActive(true);
            created.setTheme(ThemeMode.SYSTEM);
            created.setDisplayDensity(DisplayDensity.NORMAL);
            created.setPreferredLocale("fr");
            return created;
        });
        account.setEmail(email);
        account.setPersonType(personType);
        account.setPersonId(personId);
        account = accounts.save(account);
        roleAssignments.assign(account, roleName);
        return account.getId();
    }
}
