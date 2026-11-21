package tn.tekup.educated.parametrage.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, UUID> {

    Optional<AcademicYear> findFirstByCurrentTrue();

    List<AcademicYear> findAllByOrderByStartDateDesc();

    boolean existsByCode(String code);
}
