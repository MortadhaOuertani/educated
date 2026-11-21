package tn.tekup.educated.pedagogie.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramChapterRepository extends JpaRepository<ProgramChapter, UUID> {
}
