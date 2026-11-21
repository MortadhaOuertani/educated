package tn.tekup.educated.pedagogie.api;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.pedagogie.domain.StudyPlanSubject;
import tn.tekup.educated.pedagogie.domain.StudyPlanSubjectRepository;
import tn.tekup.educated.pedagogie.domain.SubjectAssessment;
import tn.tekup.educated.pedagogie.domain.Teacher;
import tn.tekup.educated.pedagogie.domain.TeacherRepository;
import tn.tekup.educated.pedagogie.domain.TeachingAssignment;
import tn.tekup.educated.pedagogie.domain.TeachingAssignmentRepository;
import tn.tekup.educated.shared.domain.AssessmentType;

/** Read-only lookups other modules use (who teaches what, coefficients, teacher names and rates). */
@Service
@Transactional(readOnly = true)
public class TeachingDirectory {

    private final TeacherRepository teachers;
    private final TeachingAssignmentRepository assignments;
    private final StudyPlanSubjectRepository studyPlanSubjects;

    TeachingDirectory(TeacherRepository teachers, TeachingAssignmentRepository assignments,
            StudyPlanSubjectRepository studyPlanSubjects) {
        this.teachers = teachers;
        this.assignments = assignments;
        this.studyPlanSubjects = studyPlanSubjects;
    }

    public Optional<TeacherInfo> findTeacher(UUID id) {
        return teachers.findById(id).map(TeachingDirectory::toInfo);
    }

    public Optional<TeacherInfo> findTeacherByEmployeeNumber(String employeeNumber) {
        return teachers.findByEmployeeNumber(employeeNumber).map(TeachingDirectory::toInfo);
    }

    public Optional<TeachingAssignmentInfo> findAssignment(UUID id) {
        return assignments.findById(id).map(TeachingDirectory::toInfo);
    }

    public List<TeachingAssignmentInfo> assignmentsOfClass(UUID schoolClassId) {
        return assignments.findAllBySchoolClassId(schoolClassId).stream().map(TeachingDirectory::toInfo).toList();
    }

    public List<TeachingAssignmentInfo> assignmentsOfTeacher(UUID teacherId) {
        return assignments.findAllByTeacher_Id(teacherId).stream().map(TeachingDirectory::toInfo).toList();
    }

    public Optional<StudyPlanSubjectInfo> findStudyPlanSubject(UUID id) {
        return studyPlanSubjects.findById(id).map(TeachingDirectory::toInfo);
    }

    private static TeacherInfo toInfo(Teacher t) {
        return new TeacherInfo(t.getId(), t.getEmployeeNumber(), t.getPerson().fullName(), t.getHourlyRate(),
                t.isActive());
    }

    private static TeachingAssignmentInfo toInfo(TeachingAssignment a) {
        var subject = a.getStudyPlanSubject().getSubject();
        Teacher teacher = a.getTeacher();
        return new TeachingAssignmentInfo(a.getId(), teacher == null ? null : teacher.getId(),
                teacher == null ? null : teacher.getPerson().fullName(), subject.getId(), subject.getCode(),
                subject.getName().fr(), a.getStudyPlanSubject().getId(), a.getSchoolClassId(), a.getAcademicYearId(),
                a.getSessionType(), a.getPlannedHours());
    }

    private static StudyPlanSubjectInfo toInfo(StudyPlanSubject s) {
        Map<AssessmentType, BigDecimal> weights = new EnumMap<>(AssessmentType.class);
        for (SubjectAssessment assessment : s.getAssessments()) {
            weights.put(assessment.getAssessmentType(), assessment.getWeightPercent());
        }
        return new StudyPlanSubjectInfo(s.getId(), s.getSubject().getId(), s.getSubject().getCode(),
                s.getSubject().getName().fr(), s.getCoefficient(), s.getCredits(), weights);
    }
}
