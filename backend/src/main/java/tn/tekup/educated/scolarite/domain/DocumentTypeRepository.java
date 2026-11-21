package tn.tekup.educated.scolarite.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentTypeRepository extends JpaRepository<DocumentType, UUID> {

    List<DocumentType> findAllByActiveTrueOrderByCodeAscLanguageAsc();

    boolean existsByCodeAndLanguage(String code, DocumentLanguage language);
}
