package tn.tekup.educated.stages.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanySupervisorRepository extends JpaRepository<CompanySupervisor, UUID> {
}
