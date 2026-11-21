package tn.tekup.educated.stages.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, UUID> {

    List<Company> findAllByActiveTrueOrderByName();
}
