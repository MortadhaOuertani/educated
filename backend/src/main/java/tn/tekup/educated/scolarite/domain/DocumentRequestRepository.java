package tn.tekup.educated.scolarite.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRequestRepository extends JpaRepository<DocumentRequest, UUID> {

    @EntityGraph(attributePaths = "documentType")
    List<DocumentRequest> findAllByStudent_IdOrderByCreatedAtDesc(UUID studentId);

    @EntityGraph(attributePaths = {"documentType", "student"})
    Page<DocumentRequest> findAllByStatus(DocumentRequestStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"documentType", "student"})
    Page<DocumentRequest> findAllBy(Pageable pageable);
}
