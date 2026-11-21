package tn.tekup.educated.examens.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.examens.api.ReportCardsPublished;
import tn.tekup.educated.examens.domain.AppreciationScale;
import tn.tekup.educated.examens.domain.AppreciationScaleRepository;
import tn.tekup.educated.examens.domain.GradeEntry;
import tn.tekup.educated.examens.domain.GradeGrid;
import tn.tekup.educated.examens.domain.GradeGridRepository;
import tn.tekup.educated.examens.domain.GradeGridStatus;
import tn.tekup.educated.examens.domain.GradeReason;
import tn.tekup.educated.examens.domain.ReportCard;
import tn.tekup.educated.examens.domain.ReportCardRepository;
import tn.tekup.educated.examens.domain.ResultSession;
import tn.tekup.educated.examens.domain.SubjectResult;
import tn.tekup.educated.examens.domain.SubjectResultRepository;
import tn.tekup.educated.pedagogie.api.StudyPlanSubjectInfo;
import tn.tekup.educated.pedagogie.api.TeachingDirectory;
import tn.tekup.educated.scolarite.api.StudentDirectory;
import tn.tekup.educated.shared.domain.AssessmentType;
import tn.tekup.educated.shared.web.BusinessRuleException;

/**
 * Bulletins. From the published grids of a class and a period:
 * <ul>
 *   <li>subject average = grades on 20 weighted by the study plan (e.g. exam 60 %, test 40 %);</li>
 *   <li>general average = subject averages weighted by their coefficients;</li>
 *   <li>a subject with an average of at least 10 earns its credits.</li>
 * </ul>
 * A justified absence or an exemption leaves the assessment out; an unjustified absence or a fraud counts as 0.
 */
@Service
@Transactional
public class ReportCardService {

    private static final BigDecimal TWENTY = BigDecimal.valueOf(20);
    private static final BigDecimal TEN = BigDecimal.TEN;

    private final GradeGridRepository grids;
    private final ReportCardRepository reportCards;
    private final SubjectResultRepository subjectResults;
    private final AppreciationScaleRepository scales;
    private final TeachingDirectory teaching;
    private final StudentDirectory students;
    private final ApplicationEventPublisher events;

    public ReportCardService(GradeGridRepository grids, ReportCardRepository reportCards,
            SubjectResultRepository subjectResults, AppreciationScaleRepository scales, TeachingDirectory teaching,
            StudentDirectory students, ApplicationEventPublisher events) {
        this.grids = grids;
        this.reportCards = reportCards;
        this.subjectResults = subjectResults;
        this.scales = scales;
        this.teaching = teaching;
        this.students = students;
        this.events = events;
    }

    /** (Re)computes the report cards of every student of the class for the period. They stay unpublished. */
    public List<ReportCard> generate(UUID schoolClassId, UUID academicPeriodId) {
        List<GradeGrid> published = grids.findAllBySchoolClassIdAndAcademicPeriodIdAndStatus(schoolClassId,
                academicPeriodId, GradeGridStatus.PUBLISHED);
        if (published.isEmpty()) {
            throw new BusinessRuleException("reportCards.noGrades", "No grades of this class are published yet.");
        }
        Map<UUID, List<GradeGrid>> gridsBySubject = new LinkedHashMap<>();
        published.forEach(grid -> gridsBySubject.computeIfAbsent(grid.getStudyPlanSubjectId(), id -> new ArrayList<>()).add(grid));
        Map<UUID, StudyPlanSubjectInfo> subjects = new HashMap<>();
        gridsBySubject.keySet().forEach(id -> teaching.findStudyPlanSubject(id).ifPresent(s -> subjects.put(id, s)));
        List<AppreciationScale> appreciation = scales.findAllByOrderByMinAverage();

        List<ReportCard> cards = new ArrayList<>();
        for (UUID studentId : students.studentIdsInClass(schoolClassId)) {
            ReportCard card = reportCards.findByStudentIdAndAcademicPeriodId(studentId, academicPeriodId)
                    .orElseGet(ReportCard::new);
            if (card.getId() != null) {
                subjectResults.deleteAllByReportCard_Id(card.getId());
            }
            card.setStudentId(studentId);
            card.setAcademicPeriodId(academicPeriodId);
            card.setPublished(false);
            card.setGeneratedAt(Instant.now());
            card = reportCards.save(card);

            BigDecimal weightedSum = BigDecimal.ZERO;
            BigDecimal coefficients = BigDecimal.ZERO;
            int credits = 0;
            for (var subjectGrids : gridsBySubject.entrySet()) {
                StudyPlanSubjectInfo subject = subjects.get(subjectGrids.getKey());
                BigDecimal average = subject == null ? null : subjectAverage(studentId, subject, subjectGrids.getValue());
                if (average == null) {
                    continue;
                }
                boolean validated = average.compareTo(TEN) >= 0;
                int subjectCredits = validated && subject.credits() != null ? subject.credits() : 0;
                SubjectResult result = new SubjectResult();
                result.setReportCard(card);
                result.setStudentId(studentId);
                result.setStudyPlanSubjectId(subject.id());
                result.setAcademicPeriodId(academicPeriodId);
                result.setAverage(average);
                result.setValidated(validated);
                result.setCreditsEarned(subjectCredits);
                result.setResultSession(ResultSession.MAIN);
                subjectResults.save(result);

                BigDecimal coefficient = subject.coefficient() == null ? BigDecimal.ONE : subject.coefficient();
                weightedSum = weightedSum.add(average.multiply(coefficient));
                coefficients = coefficients.add(coefficient);
                credits += subjectCredits;
            }
            BigDecimal general = coefficients.signum() == 0 ? null
                    : weightedSum.divide(coefficients, 2, RoundingMode.HALF_UP);
            card.setAverage(general);
            card.setCreditsEarned(credits);
            AppreciationScale scale = general == null ? null : appreciation.stream()
                    .filter(s -> general.compareTo(s.getMinAverage()) >= 0 && general.compareTo(s.getMaxAverage()) <= 0)
                    .findFirst().orElse(null);
            card.setAppreciationScale(scale);
            card.setAppreciationText(scale == null ? null : scale.getLabel().fr());
            cards.add(card);
        }
        rank(cards);
        return cards;
    }

    @Transactional(readOnly = true)
    public List<ReportCard> reportCardsOf(UUID schoolClassId, UUID academicPeriodId) {
        return reportCards.findAllByAcademicPeriodIdAndStudentIdInOrderByRankInClass(academicPeriodId,
                students.studentIdsInClass(schoolClassId));
    }

    @Transactional(readOnly = true)
    public List<SubjectResult> resultsOf(UUID reportCardId) {
        return subjectResults.findAllByReportCard_Id(reportCardId);
    }

    public List<ReportCard> publish(UUID schoolClassId, UUID academicPeriodId) {
        List<ReportCard> cards = reportCardsOf(schoolClassId, academicPeriodId);
        if (cards.isEmpty()) {
            throw new BusinessRuleException("reportCards.none", "Generate the report cards first.");
        }
        cards.forEach(card -> card.setPublished(true));
        events.publishEvent(new ReportCardsPublished(cards.getFirst().getEstablishmentId(), schoolClassId,
                academicPeriodId, cards.stream().map(ReportCard::getStudentId).toList()));
        return cards;
    }

    @Transactional(readOnly = true)
    public List<ReportCard> publishedReportCardsOf(UUID studentId) {
        return reportCards.findAllByStudentIdAndPublishedTrueOrderByGeneratedAtDesc(studentId);
    }

    /** Weighted average on 20 of the student's assessments in one subject, or null when nothing counts. */
    static BigDecimal subjectAverage(UUID studentId, StudyPlanSubjectInfo subject, List<GradeGrid> subjectGrids) {
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal weights = BigDecimal.ZERO;
        for (GradeGrid grid : subjectGrids) {
            GradeEntry entry = grid.getEntries().stream()
                    .filter(e -> e.getStudentId().equals(studentId)).findFirst().orElse(null);
            if (entry == null || entry.getReason() == GradeReason.JUSTIFIED_ABSENCE
                    || entry.getReason() == GradeReason.EXEMPTED) {
                continue;
            }
            BigDecimal onTwenty = entry.getScore() == null ? BigDecimal.ZERO
                    : entry.getScore().multiply(TWENTY).divide(grid.getMaxScore(), 4, RoundingMode.HALF_UP);
            BigDecimal weight = weightOf(subject, grid.getAssessmentType());
            sum = sum.add(onTwenty.multiply(weight));
            weights = weights.add(weight);
        }
        return weights.signum() == 0 ? null : sum.divide(weights, 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal weightOf(StudyPlanSubjectInfo subject, AssessmentType type) {
        BigDecimal weight = subject.weights().get(type);
        return weight == null ? BigDecimal.ONE : weight;
    }

    /** Rank 1 is the best average; equal averages share a rank. Students without an average are not ranked. */
    private static void rank(List<ReportCard> cards) {
        List<ReportCard> ranked = cards.stream().filter(c -> c.getAverage() != null)
                .sorted(Comparator.comparing(ReportCard::getAverage).reversed())
                .toList();
        for (int i = 0; i < ranked.size(); i++) {
            ReportCard card = ranked.get(i);
            boolean tie = i > 0 && ranked.get(i - 1).getAverage().compareTo(card.getAverage()) == 0;
            card.setRankInClass(tie ? ranked.get(i - 1).getRankInClass() : i + 1);
        }
    }
}
