package tn.tekup.educated.examens.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassDecisionRepository extends JpaRepository<ClassDecision, UUID> {
}
