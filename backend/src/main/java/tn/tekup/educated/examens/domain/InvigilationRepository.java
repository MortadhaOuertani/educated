package tn.tekup.educated.examens.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvigilationRepository extends JpaRepository<Invigilation, UUID> {

    List<Invigilation> findAllByTeacherIdAndPublishedTrueOrderByExam_ExamDateAscExam_StartTimeAsc(UUID teacherId);
}
