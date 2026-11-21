package tn.tekup.educated.examens.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppreciationScaleRepository extends JpaRepository<AppreciationScale, UUID> {

    List<AppreciationScale> findAllByOrderByMinAverage();
}
