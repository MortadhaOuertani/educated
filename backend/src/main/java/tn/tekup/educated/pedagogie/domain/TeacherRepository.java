package tn.tekup.educated.pedagogie.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, UUID> {

    Optional<Teacher> findByEmployeeNumber(String employeeNumber);

    List<Teacher> findAllByOrderByPersonLastNameAscPersonFirstNameAsc();
}
