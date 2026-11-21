package tn.tekup.educated.finances.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentFeePlanRepository extends JpaRepository<StudentFeePlan, UUID> {

    Optional<StudentFeePlan> findByEnrollmentId(UUID enrollmentId);

    List<StudentFeePlan> findAllByStudentIdOrderByCreatedAtDesc(UUID studentId);

    List<StudentFeePlan> findAllByStudentIdAndStatus(UUID studentId, FeePlanStatus status);
}
