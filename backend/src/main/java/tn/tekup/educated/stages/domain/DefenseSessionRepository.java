package tn.tekup.educated.stages.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DefenseSessionRepository extends JpaRepository<DefenseSession, UUID> {

    List<DefenseSession> findAllByOrderByStartDateDesc();
}
