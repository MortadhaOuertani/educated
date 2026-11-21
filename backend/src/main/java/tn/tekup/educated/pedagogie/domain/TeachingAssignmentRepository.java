package tn.tekup.educated.pedagogie.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.tekup.educated.shared.domain.SessionType;

public interface TeachingAssignmentRepository extends JpaRepository<TeachingAssignment, UUID> {

    List<TeachingAssignment> findAllByTeacher_IdAndAcademicYearId(UUID teacherId, UUID academicYearId);

    List<TeachingAssignment> findAllBySchoolClassId(UUID schoolClassId);

    List<TeachingAssignment> findAllByTeacher_Id(UUID teacherId);

    boolean existsByStudyPlanSubject_IdAndSchoolClassIdAndSessionType(UUID studyPlanSubjectId, UUID schoolClassId,
            SessionType sessionType);
}
