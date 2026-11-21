package tn.tekup.educated.administration.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.tekup.educated.shared.domain.PersonType;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

    Optional<UserAccount> findByKeycloakId(UUID keycloakId);

    Optional<UserAccount> findByUsernameIgnoreCase(String username);

    Page<UserAccount> findAllByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email,
            Pageable pageable);

    List<UserAccount> findAllByPersonTypeAndPersonIdIn(PersonType personType, Collection<UUID> personIds);
}
