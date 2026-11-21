package tn.tekup.educated.finances.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherPaymentRepository extends JpaRepository<TeacherPayment, UUID> {

    List<TeacherPayment> findAllByTeacherIdOrderByPeriodFromDesc(UUID teacherId);

    List<TeacherPayment> findAllByOrderByCreatedAtDesc();
}
