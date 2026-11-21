package tn.tekup.educated.examens.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectResultRepository extends JpaRepository<SubjectResult, UUID> {

    List<SubjectResult> findAllByReportCard_Id(UUID reportCardId);

    void deleteAllByReportCard_Id(UUID reportCardId);
}
