package tn.tekup.educated.stages.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InternshipRepository extends JpaRepository<Internship, UUID> {

    List<Internship> findAllByStudentIdOrderByCreatedAtDesc(UUID studentId);

    List<Internship> findAllByAcademicSupervisorIdOrderByCreatedAtDesc(UUID teacherId);

    List<Internship> findAllByStatusOrderByCreatedAt(InternshipStatus status);

    List<Internship> findAllByOrderByCreatedAtDesc();

    List<Internship> findAllByStudentIdAndAcademicYearIdAndNature_Id(UUID studentId, UUID academicYearId,
            UUID natureId);

    long countByAcademicYearId(UUID academicYearId);
}
