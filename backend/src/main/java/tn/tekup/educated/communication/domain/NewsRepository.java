package tn.tekup.educated.communication.domain;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.tekup.educated.shared.domain.Audience;

public interface NewsRepository extends JpaRepository<News, UUID> {

    List<News> findAllByOrderByCreatedAtDesc();

    List<News> findAllByStatusAndAudienceInOrderByPublishedAtDesc(NewsStatus status, Collection<Audience> audiences);
}
