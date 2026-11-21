package tn.tekup.educated.pedagogie.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MakeUpRequestRepository extends JpaRepository<MakeUpRequest, UUID> {

    List<MakeUpRequest> findAllByTeacher_IdOrderByCreatedAtDesc(UUID teacherId);

    List<MakeUpRequest> findAllByStatusOrderByCreatedAt(MakeUpStatus status);

    List<MakeUpRequest> findAllByOrderByCreatedAtDesc();
}
