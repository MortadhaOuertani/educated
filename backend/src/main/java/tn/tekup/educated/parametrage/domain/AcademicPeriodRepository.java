package tn.tekup.educated.parametrage.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicPeriodRepository extends JpaRepository<AcademicPeriod, UUID> {

    List<AcademicPeriod> findAllByAcademicYear_IdOrderByOrderIndex(UUID academicYearId);
}
