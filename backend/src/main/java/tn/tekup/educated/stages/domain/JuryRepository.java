package tn.tekup.educated.stages.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JuryRepository extends JpaRepository<Jury, UUID> {

    Optional<Jury> findByCode(String code);

    List<Jury> findAllByDefenseSession_IdOrderByJuryDate(UUID defenseSessionId);

    @Query("select distinct m.jury from JuryMember m where m.teacherId = :teacherId order by m.jury.juryDate")
    List<Jury> findAllWithMember(@Param("teacherId") UUID teacherId);
}
