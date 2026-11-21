package tn.tekup.educated.finances.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeeScheduleRepository extends JpaRepository<FeeSchedule, UUID> {
}
