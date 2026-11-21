package tn.tekup.educated.parametrage.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, UUID> {

    List<SchoolClass> findAllByAcademicYear_IdOrderByCode(UUID academicYearId);

    List<SchoolClass> findAllByAcademicYear_IdAndStudyLevel_IdOrderByCode(UUID academicYearId, UUID studyLevelId);
}
