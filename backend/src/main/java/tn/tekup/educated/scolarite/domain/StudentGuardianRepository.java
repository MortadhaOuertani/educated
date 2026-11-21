package tn.tekup.educated.scolarite.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentGuardianRepository extends JpaRepository<StudentGuardian, UUID> {

    @EntityGraph(attributePaths = {"guardian", "kinship"})
    List<StudentGuardian> findAllByStudent_Id(UUID studentId);

    boolean existsByStudent_IdAndGuardian_Id(UUID studentId, UUID guardianId);
}
