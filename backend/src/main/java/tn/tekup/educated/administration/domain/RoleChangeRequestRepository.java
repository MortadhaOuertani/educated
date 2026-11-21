package tn.tekup.educated.administration.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleChangeRequestRepository extends JpaRepository<RoleChangeRequest, UUID> {

    List<RoleChangeRequest> findAllByStatusOrderByCreatedAt(ApprovalStatus status);
}
