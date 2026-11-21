package tn.tekup.educated.pedagogie.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherSubjectPreferenceRepository extends JpaRepository<TeacherSubjectPreference, UUID> {
}
