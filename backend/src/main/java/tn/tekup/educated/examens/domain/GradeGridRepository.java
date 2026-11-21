package tn.tekup.educated.examens.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.tekup.educated.shared.domain.AssessmentType;

public interface GradeGridRepository extends JpaRepository<GradeGrid, UUID> {

    Optional<GradeGrid> findByStudyPlanSubjectIdAndSchoolClassIdAndAcademicPeriodIdAndAssessmentType(
            UUID studyPlanSubjectId, UUID schoolClassId, UUID academicPeriodId, AssessmentType assessmentType);

    List<GradeGrid> findAllBySchoolClassIdAndAcademicPeriodId(UUID schoolClassId, UUID academicPeriodId);

    List<GradeGrid> findAllBySchoolClassIdAndAcademicPeriodIdAndStatus(UUID schoolClassId, UUID academicPeriodId,
            GradeGridStatus status);
}
