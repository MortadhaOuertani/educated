package tn.tekup.educated.vieetablissement.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyRepository extends JpaRepository<Survey, UUID> {

    List<Survey> findAllByOrderByCreatedAtDesc();
}
