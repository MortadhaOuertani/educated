package tn.tekup.educated.pedagogie.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgressEntryRepository extends JpaRepository<ProgressEntry, UUID> {
}
