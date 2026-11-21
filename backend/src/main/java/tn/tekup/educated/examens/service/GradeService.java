package tn.tekup.educated.examens.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.examens.api.GradesPublished;
import tn.tekup.educated.examens.domain.GradeEntry;
import tn.tekup.educated.examens.domain.GradeEntryRepository;
import tn.tekup.educated.examens.domain.GradeGrid;
import tn.tekup.educated.examens.domain.GradeGridRepository;
import tn.tekup.educated.examens.domain.GradeGridStatus;
import tn.tekup.educated.examens.domain.GradeReason;
import tn.tekup.educated.parametrage.api.ParametrageDirectory;
import tn.tekup.educated.pedagogie.api.StudyPlanSubjectInfo;
import tn.tekup.educated.pedagogie.api.TeachingAssignmentInfo;
import tn.tekup.educated.pedagogie.api.TeachingDirectory;
import tn.tekup.educated.scolarite.api.StudentDirectory;
import tn.tekup.educated.shared.domain.AssessmentType;
import tn.tekup.educated.shared.web.BusinessRuleException;
import tn.tekup.educated.shared.web.NotFoundException;

/**
 * Grilles de notes. The teacher enters a score or a reason (absence, exemption, fraud) for every student, confirms
 * the grid, and the administration publishes it to the students.
 */
@Service
@Transactional
public class GradeService {

    public record GradeData(UUID studentId, BigDecimal score, GradeReason reason, String comment) {
    }

    private final GradeGridRepository grids;
    private final GradeEntryRepository entries;
    private final TeachingDirectory teaching;
    private final StudentDirectory students;
    private final ParametrageDirectory parametrage;
    private final ApplicationEventPublisher events;

    public GradeService(GradeGridRepository grids, GradeEntryRepository entries, TeachingDirectory teaching,
            StudentDirectory students, ParametrageDirectory parametrage, ApplicationEventPublisher events) {
        this.grids = grids;
        this.entries = entries;
        this.teaching = teaching;
        this.students = students;
        this.parametrage = parametrage;
        this.events = events;
    }

    /** Opens (or creates) the grid of one assessment of a subject the teacher teaches. */
    public GradeGrid openGrid(UUID teachingAssignmentId, UUID academicPeriodId, AssessmentType type, UUID teacherId) {
        TeachingAssignmentInfo assignment = teaching.findAssignment(teachingAssignmentId)
                .filter(a -> teacherId == null || teacherId.equals(a.teacherId()))
                .orElseThrow(() -> new NotFoundException("TeachingAssignment", teachingAssignmentId));
        if (parametrage.findAcademicPeriod(academicPeriodId).isEmpty()) {
            throw new NotFoundException("AcademicPeriod", academicPeriodId);
        }
        StudyPlanSubjectInfo subject = teaching.findStudyPlanSubject(assignment.studyPlanSubjectId()).orElseThrow();
        if (!subject.weights().isEmpty() && !subject.weights().containsKey(type)) {
            throw new BusinessRuleException("grades.assessmentType",
                    "This subject has no " + type + " in its study plan.");
        }
        return grids.findByStudyPlanSubjectIdAndSchoolClassIdAndAcademicPeriodIdAndAssessmentType(
                        assignment.studyPlanSubjectId(), assignment.schoolClassId(), academicPeriodId, type)
                .orElseGet(() -> {
                    GradeGrid grid = new GradeGrid();
                    grid.setStudyPlanSubjectId(assignment.studyPlanSubjectId());
                    grid.setSchoolClassId(assignment.schoolClassId());
                    grid.setAcademicPeriodId(academicPeriodId);
                    grid.setTeacherId(assignment.teacherId());
                    grid.setAssessmentType(type);
                    grid.setMaxScore(BigDecimal.valueOf(20));
                    grid.setStatus(GradeGridStatus.OPEN);
                    return grids.save(grid);
                });
    }

    @Transactional(readOnly = true)
    public GradeGrid grid(UUID id, UUID teacherId) {
        GradeGrid grid = grids.findById(id).orElseThrow(() -> new NotFoundException("GradeGrid", id));
        if (teacherId != null && !teacherId.equals(grid.getTeacherId())) {
            throw new NotFoundException("GradeGrid", id);
        }
        return grid;
    }

    @Transactional(readOnly = true)
    public List<GradeGrid> gridsOf(UUID schoolClassId, UUID academicPeriodId) {
        return grids.findAllBySchoolClassIdAndAcademicPeriodId(schoolClassId, academicPeriodId);
    }

    /** Each change gives a score (0 to the maximum) or a reason; giving neither removes the student's grade. */
    public GradeGrid saveGrades(UUID gridId, UUID teacherId, List<GradeData> changes) {
        GradeGrid grid = grid(gridId, teacherId);
        if (grid.getStatus() == GradeGridStatus.CONFIRMED || grid.getStatus() == GradeGridStatus.PUBLISHED) {
            throw new BusinessRuleException("grades.locked", "This grid is confirmed and can no longer be changed.");
        }
        Set<UUID> classStudents = new HashSet<>(students.studentIdsInClass(grid.getSchoolClassId()));
        Map<UUID, GradeEntry> byStudent = new HashMap<>();
        grid.getEntries().forEach(entry -> byStudent.put(entry.getStudentId(), entry));
        for (GradeData change : changes) {
            if (!classStudents.contains(change.studentId())) {
                throw new BusinessRuleException("grades.student", "This student is not in the class.");
            }
            if (change.score() != null && change.reason() != null) {
                throw new BusinessRuleException("grades.scoreOrReason", "Give a score or a reason, not both.");
            }
            if (change.score() != null
                    && (change.score().signum() < 0 || change.score().compareTo(grid.getMaxScore()) > 0)) {
                throw new BusinessRuleException("grades.score",
                        "A score must be between 0 and " + grid.getMaxScore().stripTrailingZeros().toPlainString() + ".");
            }
            GradeEntry entry = byStudent.get(change.studentId());
            if (change.score() == null && change.reason() == null) {
                if (entry != null) {
                    grid.getEntries().remove(entry);
                }
                continue;
            }
            if (entry == null) {
                entry = new GradeEntry();
                entry.setGrid(grid);
                entry.setStudentId(change.studentId());
                grid.getEntries().add(entry);
            }
            entry.setScore(change.score());
            entry.setReason(change.reason());
            entry.setComment(change.comment());
        }
        grid.setStatus(GradeGridStatus.IN_PROGRESS);
        return grid;
    }

    /** The teacher confirms once every student of the class has a score or a reason. */
    public GradeGrid confirm(UUID gridId, UUID teacherId) {
        GradeGrid grid = grid(gridId, teacherId);
        if (grid.getStatus() == GradeGridStatus.CONFIRMED || grid.getStatus() == GradeGridStatus.PUBLISHED) {
            throw new BusinessRuleException("grades.alreadyConfirmed", "This grid is already confirmed.");
        }
        Set<UUID> graded = new HashSet<>();
        grid.getEntries().forEach(entry -> graded.add(entry.getStudentId()));
        if (!graded.containsAll(students.studentIdsInClass(grid.getSchoolClassId()))) {
            throw new BusinessRuleException("grades.incomplete", "Every student needs a score or a reason.");
        }
        grid.setStatus(GradeGridStatus.CONFIRMED);
        grid.setConfirmedAt(Instant.now());
        return grid;
    }

    public GradeGrid publish(UUID gridId) {
        GradeGrid grid = grid(gridId, null);
        if (grid.getStatus() != GradeGridStatus.CONFIRMED) {
            throw new BusinessRuleException("grades.notConfirmed", "Only a confirmed grid can be published.");
        }
        grid.setStatus(GradeGridStatus.PUBLISHED);
        grid.setPublishedAt(Instant.now());
        events.publishEvent(new GradesPublished(grid.getEstablishmentId(), grid.getId(), grid.getSchoolClassId(),
                grid.getStudyPlanSubjectId(), grid.getAssessmentType().name()));
        return grid;
    }

    @Transactional(readOnly = true)
    public List<GradeEntry> publishedGradesOf(UUID studentId) {
        return entries.findAllByStudentIdAndGrid_StatusOrderByGrid_PublishedAtDesc(studentId, GradeGridStatus.PUBLISHED);
    }
}
