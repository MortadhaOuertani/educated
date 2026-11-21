package tn.tekup.educated.examens.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExamRepository extends JpaRepository<Exam, UUID> {

    List<Exam> findAllByExamDate(LocalDate examDate);

    List<Exam> findAllByExamSession_IdOrderByExamDateAscStartTimeAsc(UUID examSessionId);

    @Query("""
            select e from Exam e join e.classes c
            where c.schoolClassId = :schoolClassId and e.published = true
            order by e.examDate, e.startTime
            """)
    List<Exam> findPublishedForClass(@Param("schoolClassId") UUID schoolClassId);
}
