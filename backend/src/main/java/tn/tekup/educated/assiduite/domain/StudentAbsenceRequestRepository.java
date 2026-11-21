package tn.tekup.educated.assiduite.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentAbsenceRequestRepository extends JpaRepository<StudentAbsenceRequest, UUID> {

    List<StudentAbsenceRequest> findAllByStudentIdOrderByCreatedAtDesc(UUID studentId);

    List<StudentAbsenceRequest> findAllByStatusOrderByCreatedAt(RequestStatus status);

    List<StudentAbsenceRequest> findAllByOrderByCreatedAtDesc();
}
