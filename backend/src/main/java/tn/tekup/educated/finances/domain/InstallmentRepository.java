package tn.tekup.educated.finances.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstallmentRepository extends JpaRepository<Installment, UUID> {

    List<Installment> findAllByDueDateBeforeAndStatusNotOrderByDueDate(LocalDate date, InstallmentStatus status);
}
