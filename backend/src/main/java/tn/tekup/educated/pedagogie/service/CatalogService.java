package tn.tekup.educated.pedagogie.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.parametrage.api.ParametrageDirectory;
import tn.tekup.educated.pedagogie.domain.StudyPlan;
import tn.tekup.educated.pedagogie.domain.StudyPlanRepository;
import tn.tekup.educated.pedagogie.domain.StudyPlanStatus;
import tn.tekup.educated.pedagogie.domain.StudyPlanSubject;
import tn.tekup.educated.pedagogie.domain.Subject;
import tn.tekup.educated.pedagogie.domain.SubjectAssessment;
import tn.tekup.educated.pedagogie.domain.SubjectRepository;
import tn.tekup.educated.pedagogie.domain.TeacherGrade;
import tn.tekup.educated.pedagogie.domain.TeacherGradeRepository;
import tn.tekup.educated.pedagogie.domain.TeachingUnit;
import tn.tekup.educated.shared.domain.AssessmentType;
import tn.tekup.educated.shared.domain.LocalizedText;
import tn.tekup.educated.shared.web.BusinessRuleException;
import tn.tekup.educated.shared.web.NotFoundException;

/** Teacher grades, subjects and study plans (plans d'étude). */
@Service
@Transactional
public class CatalogService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public record SubjectData(String code, LocalizedText name, String description, UUID teachingDomainId,
            BigDecimal defaultCoefficient, Integer defaultCredits) {
    }

    public record AssessmentData(AssessmentType assessmentType, BigDecimal weightPercent, BigDecimal maxScore) {
    }

    public record PlanSubjectData(UUID subjectId, BigDecimal coefficient, Integer credits, Integer lectureHours,
            Integer tutorialHours, Integer labHours, Integer projectHours, List<AssessmentData> assessments) {
    }

    public record UnitData(String code, LocalizedText name, BigDecimal coefficient, Integer credits,
            List<PlanSubjectData> subjects) {
    }

    public record StudyPlanData(String code, String description, UUID studyLevelId, int semesterNumber,
            List<UnitData> units) {
    }

    private final TeacherGradeRepository grades;
    private final SubjectRepository subjects;
    private final StudyPlanRepository studyPlans;
    private final ParametrageDirectory parametrage;

    public CatalogService(TeacherGradeRepository grades, SubjectRepository subjects, StudyPlanRepository studyPlans,
            ParametrageDirectory parametrage) {
        this.grades = grades;
        this.subjects = subjects;
        this.studyPlans = studyPlans;
        this.parametrage = parametrage;
    }

    @Transactional(readOnly = true)
    public List<TeacherGrade> grades() {
        return grades.findAllByOrderByCode();
    }

    public TeacherGrade createGrade(String code, LocalizedText label) {
        if (grades.findByCode(code).isPresent()) {
            throw new BusinessRuleException("teacherGrade.code", "Grade " + code + " already exists.");
        }
        TeacherGrade grade = new TeacherGrade();
        grade.setCode(code);
        grade.setLabel(label);
        return grades.save(grade);
    }

    @Transactional(readOnly = true)
    public List<Subject> subjects() {
        return subjects.findAllByOrderByCode();
    }

    @Transactional(readOnly = true)
    public Subject subject(UUID id) {
        return subjects.findById(id).orElseThrow(() -> new NotFoundException("Subject", id));
    }

    public Subject createSubject(SubjectData data) {
        if (subjects.findByCode(data.code()).isPresent()) {
            throw new BusinessRuleException("subject.code", "Subject " + data.code() + " already exists.");
        }
        Subject subject = new Subject();
        subject.setCode(data.code());
        subject.setName(data.name());
        subject.setDescription(data.description());
        subject.setTeachingDomainId(data.teachingDomainId());
        subject.setDefaultCoefficient(data.defaultCoefficient());
        subject.setDefaultCredits(data.defaultCredits());
        subject.setActive(true);
        return subjects.save(subject);
    }

    @Transactional(readOnly = true)
    public List<StudyPlan> studyPlans() {
        return studyPlans.findAllByOrderByCode();
    }

    @Transactional(readOnly = true)
    public StudyPlan studyPlan(UUID id) {
        return studyPlans.findById(id).orElseThrow(() -> new NotFoundException("StudyPlan", id));
    }

    /** Creates a draft plan with its teaching units, subjects and grade weights (which must add up to 100 %). */
    public StudyPlan createStudyPlan(StudyPlanData data) {
        if (studyPlans.findByCode(data.code()).isPresent()) {
            throw new BusinessRuleException("studyPlan.code", "Study plan " + data.code() + " already exists.");
        }
        if (!parametrage.studyLevelExists(data.studyLevelId())) {
            throw new NotFoundException("StudyLevel", data.studyLevelId());
        }
        StudyPlan plan = new StudyPlan();
        plan.setCode(data.code());
        plan.setDescription(data.description());
        plan.setStudyLevelId(data.studyLevelId());
        plan.setSemesterNumber(data.semesterNumber());
        plan.setStatus(StudyPlanStatus.DRAFT);
        int order = 1;
        for (UnitData unitData : data.units()) {
            TeachingUnit unit = new TeachingUnit();
            unit.setStudyPlan(plan);
            unit.setCode(unitData.code());
            unit.setName(unitData.name());
            unit.setCoefficient(unitData.coefficient());
            unit.setCredits(unitData.credits());
            unit.setOrderIndex(order++);
            for (PlanSubjectData subjectData : unitData.subjects()) {
                unit.getSubjects().add(planSubject(unit, subjectData));
            }
            plan.getUnits().add(unit);
        }
        return studyPlans.save(plan);
    }

    public StudyPlan changeStatus(UUID id, StudyPlanStatus status) {
        StudyPlan plan = studyPlan(id);
        plan.setStatus(status);
        return plan;
    }

    private StudyPlanSubject planSubject(TeachingUnit unit, PlanSubjectData data) {
        Subject subject = subject(data.subjectId());
        StudyPlanSubject planSubject = new StudyPlanSubject();
        planSubject.setTeachingUnit(unit);
        planSubject.setSubject(subject);
        planSubject.setCoefficient(data.coefficient() != null ? data.coefficient() : subject.getDefaultCoefficient());
        planSubject.setCredits(data.credits() != null ? data.credits() : subject.getDefaultCredits());
        planSubject.setLectureHours(data.lectureHours());
        planSubject.setTutorialHours(data.tutorialHours());
        planSubject.setLabHours(data.labHours());
        planSubject.setProjectHours(data.projectHours());
        List<AssessmentData> assessments = data.assessments() == null ? List.of() : data.assessments();
        BigDecimal total = BigDecimal.ZERO;
        for (AssessmentData assessmentData : assessments) {
            SubjectAssessment assessment = new SubjectAssessment();
            assessment.setStudyPlanSubject(planSubject);
            assessment.setAssessmentType(assessmentData.assessmentType());
            assessment.setWeightPercent(assessmentData.weightPercent());
            assessment.setMaxScore(assessmentData.maxScore() != null ? assessmentData.maxScore() : BigDecimal.valueOf(20));
            planSubject.getAssessments().add(assessment);
            total = total.add(assessmentData.weightPercent());
        }
        if (!assessments.isEmpty() && total.compareTo(HUNDRED) != 0) {
            throw new BusinessRuleException("studyPlan.weights",
                    "The grade weights of " + subject.getCode() + " add up to " + total + " %, not 100 %.");
        }
        return planSubject;
    }
}
