package tn.tekup.educated.examens.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportCardRepository extends JpaRepository<ReportCard, UUID> {

    Optional<ReportCard> findByStudentIdAndAcademicPeriodId(UUID studentId, UUID academicPeriodId);

    List<ReportCard> findAllByAcademicPeriodIdAndStudentIdInOrderByRankInClass(UUID academicPeriodId,
            Collection<UUID> studentIds);

    List<ReportCard> findAllByStudentIdAndPublishedTrueOrderByGeneratedAtDesc(UUID studentId);
}
