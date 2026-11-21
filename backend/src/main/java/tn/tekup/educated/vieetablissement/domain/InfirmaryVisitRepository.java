package tn.tekup.educated.vieetablissement.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InfirmaryVisitRepository extends JpaRepository<InfirmaryVisit, UUID> {

    List<InfirmaryVisit> findAllByOrderByVisitedAtDesc();

    List<InfirmaryVisit> findAllByStudentIdOrderByVisitedAtDesc(UUID studentId);
}
