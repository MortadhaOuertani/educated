package tn.tekup.educated.parametrage.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeachingDomainRepository extends JpaRepository<TeachingDomain, UUID> {

    List<TeachingDomain> findAllByOrderByCode();
}
