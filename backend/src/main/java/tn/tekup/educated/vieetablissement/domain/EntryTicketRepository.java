package tn.tekup.educated.vieetablissement.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntryTicketRepository extends JpaRepository<EntryTicket, UUID> {

    List<EntryTicket> findAllByStudentIdOrderByValidForDateDesc(UUID studentId);

    List<EntryTicket> findAllByValidForDateOrderByCreatedAt(LocalDate date);
}
