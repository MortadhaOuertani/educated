package tn.tekup.educated.vieetablissement.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.shared.domain.Audience;
import tn.tekup.educated.shared.domain.PersonType;
import tn.tekup.educated.shared.security.CurrentUser;
import tn.tekup.educated.shared.web.BusinessRuleException;
import tn.tekup.educated.shared.web.NotFoundException;
import tn.tekup.educated.vieetablissement.domain.QuestionType;
import tn.tekup.educated.vieetablissement.domain.Survey;
import tn.tekup.educated.vieetablissement.domain.SurveyPurpose;
import tn.tekup.educated.vieetablissement.domain.SurveyQuestion;
import tn.tekup.educated.vieetablissement.domain.SurveyRepository;
import tn.tekup.educated.vieetablissement.domain.SurveyResponse;
import tn.tekup.educated.vieetablissement.domain.SurveyResponseRepository;
import tools.jackson.databind.json.JsonMapper;

/**
 * Sondages and course evaluations. Answers of an anonymous survey keep no trace of who wrote them; because of
 * that, the one-answer-per-person rule only applies to surveys that are not anonymous.
 */
@Service
@Transactional
public class SurveyService {

    public record QuestionData(String text, QuestionType questionType, List<String> options, boolean required) {
    }

    public record SurveyData(String title, String description, SurveyPurpose purpose, Audience audience,
            UUID teachingAssignmentId, Instant opensAt, Instant closesAt, boolean anonymous,
            List<QuestionData> questions) {
    }

    private final SurveyRepository surveys;
    private final SurveyResponseRepository responses;
    private final JsonMapper json;

    public SurveyService(SurveyRepository surveys, SurveyResponseRepository responses, JsonMapper json) {
        this.surveys = surveys;
        this.responses = responses;
        this.json = json;
    }

    @Transactional(readOnly = true)
    public List<Survey> all() {
        return surveys.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Survey survey(UUID id) {
        return surveys.findById(id).orElseThrow(() -> new NotFoundException("Survey", id));
    }

    @Transactional(readOnly = true)
    public long responseCount(UUID surveyId) {
        return responses.countBySurvey_Id(surveyId);
    }

    public Survey create(SurveyData data) {
        if (data.questions() == null || data.questions().isEmpty()) {
            throw new BusinessRuleException("survey.questions", "A survey needs at least one question.");
        }
        if (data.opensAt() != null && data.closesAt() != null && !data.closesAt().isAfter(data.opensAt())) {
            throw new BusinessRuleException("survey.dates", "The survey must close after it opens.");
        }
        Survey survey = new Survey();
        survey.setTitle(data.title());
        survey.setDescription(data.description());
        survey.setPurpose(data.purpose() == null ? SurveyPurpose.GENERAL : data.purpose());
        survey.setAudience(data.audience());
        survey.setTeachingAssignmentId(data.teachingAssignmentId());
        survey.setOpensAt(data.opensAt());
        survey.setClosesAt(data.closesAt());
        survey.setAnonymous(data.anonymous());
        int order = 1;
        for (QuestionData q : data.questions()) {
            SurveyQuestion question = new SurveyQuestion();
            question.setSurvey(survey);
            question.setOrderIndex(order++);
            question.setText(q.text());
            question.setQuestionType(q.questionType());
            question.setOptionsJson(q.options() == null ? null : json.writeValueAsString(q.options()));
            question.setRequired(q.required());
            survey.getQuestions().add(question);
        }
        return surveys.save(survey);
    }

    /** Open surveys whose audience includes the user (students, teachers or staff). */
    @Transactional(readOnly = true)
    public List<Survey> openFor(CurrentUser user) {
        Instant now = Instant.now();
        return surveys.findAllByOrderByCreatedAtDesc().stream()
                .filter(s -> isOpen(s, now) && reaches(s.getAudience(), user.personType()))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean hasAnswered(UUID surveyId, UUID userId) {
        return responses.existsBySurvey_IdAndRespondentUserId(surveyId, userId);
    }

    /** Answers are keyed by question id. Required questions must be answered. */
    public void respond(UUID surveyId, CurrentUser user, Map<String, String> answers) {
        Survey survey = survey(surveyId);
        if (!isOpen(survey, Instant.now()) || !reaches(survey.getAudience(), user.personType())) {
            throw new BusinessRuleException("survey.closed", "This survey is not open to you.");
        }
        if (!survey.isAnonymous() && hasAnswered(surveyId, user.userId())) {
            throw new BusinessRuleException("survey.alreadyAnswered", "You already answered this survey.");
        }
        for (SurveyQuestion question : survey.getQuestions()) {
            String answer = answers.get(question.getId().toString());
            if (question.isRequired() && (answer == null || answer.isBlank())) {
                throw new BusinessRuleException("survey.required", "Answer the question: " + question.getText());
            }
        }
        SurveyResponse response = new SurveyResponse();
        response.setSurvey(survey);
        response.setRespondentUserId(survey.isAnonymous() ? null : user.userId());
        response.setAnswersJson(json.writeValueAsString(answers));
        response = responses.save(response);
        if (survey.isAnonymous()) {
            responses.forgetAuthor(response.getId());
        }
    }

    private static boolean isOpen(Survey s, Instant now) {
        return (s.getOpensAt() == null || !now.isBefore(s.getOpensAt()))
                && (s.getClosesAt() == null || now.isBefore(s.getClosesAt()));
    }

    private static boolean reaches(Audience audience, PersonType type) {
        if (audience == Audience.ALL) {
            return true;
        }
        return type != null && switch (audience) {
            case STUDENTS -> type == PersonType.STUDENT;
            case TEACHERS -> type == PersonType.TEACHER;
            case GUARDIANS -> type == PersonType.GUARDIAN;
            case STAFF -> type == PersonType.STAFF;
            default -> false;
        };
    }
}
