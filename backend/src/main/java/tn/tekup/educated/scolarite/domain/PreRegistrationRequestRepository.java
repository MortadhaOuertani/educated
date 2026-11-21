package tn.tekup.educated.scolarite.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreRegistrationRequestRepository extends JpaRepository<PreRegistrationRequest, UUID> {
}
