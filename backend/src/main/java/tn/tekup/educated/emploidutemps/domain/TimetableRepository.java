package tn.tekup.educated.emploidutemps.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimetableRepository extends JpaRepository<Timetable, UUID> {

    List<Timetable> findAllBySchoolClassIdOrderByCreatedAtDesc(UUID schoolClassId);

    List<Timetable> findAllBySchoolClassIdAndAcademicPeriodIdAndStatus(UUID schoolClassId, UUID academicPeriodId,
            TimetableStatus status);

    long countBySchoolClassIdAndAcademicPeriodId(UUID schoolClassId, UUID academicPeriodId);
}
