package tn.tekup.educated.finances.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeeScheduleRepository extends JpaRepository<FeeSchedule, UUID> {

    List<FeeSchedule> findAllByAcademicYearIdOrderByFeeType(UUID academicYearId);

    List<FeeSchedule> findAllByAcademicYearIdAndStudyLevelIdAndActiveTrue(UUID academicYearId, UUID studyLevelId);
}
