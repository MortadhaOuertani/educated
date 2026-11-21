package tn.tekup.educated.pedagogie.domain;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseMaterialRepository extends JpaRepository<CourseMaterial, UUID> {

    List<CourseMaterial> findAllByTeachingAssignment_IdOrderByCreatedAtDesc(UUID teachingAssignmentId);

    List<CourseMaterial> findAllByTeachingAssignment_IdInAndVisibleToStudentsTrueOrderByCreatedAtDesc(
            Collection<UUID> teachingAssignmentIds);
}
