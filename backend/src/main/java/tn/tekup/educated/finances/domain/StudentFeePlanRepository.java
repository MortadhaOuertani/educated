package tn.tekup.educated.finances.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentFeePlanRepository extends JpaRepository<StudentFeePlan, UUID> {
}
