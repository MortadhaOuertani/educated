package tn.tekup.educated.scolarite.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClassAssignmentRepository extends JpaRepository<ClassAssignment, UUID> {

    List<ClassAssignment> findAllBySchoolClassIdAndEndDateIsNull(UUID schoolClassId);

    @Query("""
            select a from ClassAssignment a
            where a.enrollment.student.id = :studentId and a.endDate is null
              and a.enrollment.status = tn.tekup.educated.scolarite.domain.EnrollmentStatus.ENROLLED
            """)
    List<ClassAssignment> findOpenForStudent(@Param("studentId") UUID studentId);
}
