package tn.tekup.educated.pedagogie.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyPlanRepository extends JpaRepository<StudyPlan, UUID> {

    Optional<StudyPlan> findByCode(String code);

    List<StudyPlan> findAllByOrderByCode();
}
