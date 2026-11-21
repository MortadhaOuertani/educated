package tn.tekup.educated.vieetablissement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** One person's answers. Anonymous surveys store no respondent. */
@Entity
@Table(name = "survey_response", schema = "vieetablissement")
public class SurveyResponse extends TenantEntity {

    private UUID respondentUserId;

    @Column(nullable = false, columnDefinition = "text")
    private String answersJson;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    public SurveyResponse() {
    }

    public UUID getRespondentUserId() {
        return respondentUserId;
    }

    public void setRespondentUserId(UUID respondentUserId) {
        this.respondentUserId = respondentUserId;
    }

    public String getAnswersJson() {
        return answersJson;
    }

    public void setAnswersJson(String answersJson) {
        this.answersJson = answersJson;
    }

    public Survey getSurvey() {
        return survey;
    }

    public void setSurvey(Survey survey) {
        this.survey = survey;
    }

}
