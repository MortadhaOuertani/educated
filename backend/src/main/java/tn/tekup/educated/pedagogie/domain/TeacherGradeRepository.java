package tn.tekup.educated.pedagogie.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherGradeRepository extends JpaRepository<TeacherGrade, UUID> {

    Optional<TeacherGrade> findByCode(String code);

    List<TeacherGrade> findAllByOrderByCode();
}
