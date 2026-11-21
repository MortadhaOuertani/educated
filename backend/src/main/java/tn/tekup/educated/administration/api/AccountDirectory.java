package tn.tekup.educated.administration.api;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.administration.domain.UserAccount;
import tn.tekup.educated.administration.domain.UserAccountRepository;
import tn.tekup.educated.administration.domain.UserRoleRepository;
import tn.tekup.educated.shared.domain.PersonType;

/** Finds the accounts behind students, teachers and staff, so other modules can notify them. */
@Service
@Transactional(readOnly = true)
public class AccountDirectory {

    public record AccountInfo(UUID userId, String username, PersonType personType, UUID personId) {
    }

    private final UserAccountRepository accounts;
    private final UserRoleRepository userRoles;

    AccountDirectory(UserAccountRepository accounts, UserRoleRepository userRoles) {
        this.accounts = accounts;
        this.userRoles = userRoles;
    }

    /** Account ids of the given students or teachers (people without an account are skipped). */
    public List<UUID> userIdsOf(PersonType type, Collection<UUID> personIds) {
        if (personIds.isEmpty()) {
            return List.of();
        }
        return accounts.findAllByPersonTypeAndPersonIdIn(type, personIds).stream().map(UserAccount::getId).toList();
    }

    /** Everyone of that kind with a role in the current establishment. STAFF includes unlinked admin accounts. */
    public List<UUID> usersOfEstablishment(PersonType type) {
        return type == PersonType.STAFF ? userRoles.findStaffUserIds() : userRoles.findUserIdsByPersonType(type);
    }

    public Optional<AccountInfo> findAccount(UUID userId) {
        return accounts.findById(userId)
                .map(a -> new AccountInfo(a.getId(), a.getUsername(), a.getPersonType(), a.getPersonId()));
    }
}
