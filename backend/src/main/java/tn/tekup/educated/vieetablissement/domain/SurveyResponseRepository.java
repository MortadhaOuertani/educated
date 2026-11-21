package tn.tekup.educated.vieetablissement.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, UUID> {

    boolean existsBySurvey_IdAndRespondentUserId(UUID surveyId, UUID respondentUserId);

    long countBySurvey_Id(UUID surveyId);

    List<SurveyResponse> findAllBySurvey_Id(UUID surveyId);

    /** Anonymous answers must not keep who wrote them, so the audit columns are cleared. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update SurveyResponse r set r.createdByUserId = null, r.updatedByUserId = null where r.id = :id")
    void forgetAuthor(@Param("id") UUID id);
}
