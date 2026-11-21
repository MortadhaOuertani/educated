package tn.tekup.educated.scolarite.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    List<Enrollment> findAllByStudent_IdOrderByEnrollmentDateDesc(UUID studentId);

    boolean existsByStudent_IdAndAcademicYearIdAndStatusNot(UUID studentId, UUID academicYearId, EnrollmentStatus status);
}
