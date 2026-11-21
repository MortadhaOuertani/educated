package tn.tekup.educated.finances.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherPaymentLineRepository extends JpaRepository<TeacherPaymentLine, UUID> {

    boolean existsByTeacherAttendanceIdAndPayment_StatusNot(UUID teacherAttendanceId, TeacherPaymentStatus status);
}
