package tn.tekup.educated.pedagogie.domain;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HomeworkRepository extends JpaRepository<Homework, UUID> {

    List<Homework> findAllByTeachingAssignment_IdOrderByAssignedDateDesc(UUID teachingAssignmentId);

    List<Homework> findAllByTeachingAssignment_IdInOrderByDueDateAsc(Collection<UUID> teachingAssignmentIds);
}
