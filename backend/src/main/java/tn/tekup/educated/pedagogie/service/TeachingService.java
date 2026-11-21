package tn.tekup.educated.pedagogie.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.parametrage.api.ParametrageDirectory;
import tn.tekup.educated.parametrage.api.SchoolClassInfo;
import tn.tekup.educated.pedagogie.domain.EmploymentType;
import tn.tekup.educated.pedagogie.domain.StudyPlanSubject;
import tn.tekup.educated.pedagogie.domain.StudyPlanSubjectRepository;
import tn.tekup.educated.pedagogie.domain.Teacher;
import tn.tekup.educated.pedagogie.domain.TeacherGradeRepository;
import tn.tekup.educated.pedagogie.domain.TeacherRepository;
import tn.tekup.educated.pedagogie.domain.TeachingAssignment;
import tn.tekup.educated.pedagogie.domain.TeachingAssignmentRepository;
import tn.tekup.educated.shared.domain.Money;
import tn.tekup.educated.shared.domain.PersonInfo;
import tn.tekup.educated.shared.domain.SessionType;
import tn.tekup.educated.shared.web.BusinessRuleException;
import tn.tekup.educated.shared.web.NotFoundException;

/** Teachers and teaching assignments (who teaches which subject to which class). */
@Service
@Transactional
public class TeachingService {

    public record TeacherData(PersonInfo person, String employeeNumber, String gradeCode,
            EmploymentType employmentType, LocalDate hireDate, Money hourlyRate, Integer statutoryHours,
            Integer maxWeeklyHours) {
    }

    public record Workload(UUID teacherId, UUID academicYearId, Integer statutoryHours, int assignedHours,
            boolean overloaded) {
    }

    private final TeacherRepository teachers;
    private final TeacherGradeRepository grades;
    private final TeachingAssignmentRepository assignments;
    private final StudyPlanSubjectRepository studyPlanSubjects;
    private final ParametrageDirectory parametrage;

    public TeachingService(TeacherRepository teachers, TeacherGradeRepository grades,
            TeachingAssignmentRepository assignments, StudyPlanSubjectRepository studyPlanSubjects,
            ParametrageDirectory parametrage) {
        this.teachers = teachers;
        this.grades = grades;
        this.assignments = assignments;
        this.studyPlanSubjects = studyPlanSubjects;
        this.parametrage = parametrage;
    }

    @Transactional(readOnly = true)
    public List<Teacher> teachers() {
        return teachers.findAllByOrderByPersonLastNameAscPersonFirstNameAsc();
    }

    @Transactional(readOnly = true)
    public Teacher teacher(UUID id) {
        return teachers.findById(id).orElseThrow(() -> new NotFoundException("Teacher", id));
    }

    public Teacher createTeacher(TeacherData data) {
        if (data.employeeNumber() != null && teachers.findByEmployeeNumber(data.employeeNumber()).isPresent()) {
            throw new BusinessRuleException("teacher.employeeNumber",
                    "Employee number " + data.employeeNumber() + " is already used.");
        }
        Teacher teacher = new Teacher();
        teacher.setPerson(data.person());
        teacher.setEmployeeNumber(data.employeeNumber());
        teacher.setGrade(grades.findByCode(data.gradeCode())
                .orElseThrow(() -> new NotFoundException("TeacherGrade", data.gradeCode())));
        teacher.setEmploymentType(data.employmentType());
        teacher.setHireDate(data.hireDate());
        teacher.setHourlyRate(data.hourlyRate());
        teacher.setStatutoryHours(data.statutoryHours());
        teacher.setMaxWeeklyHours(data.maxWeeklyHours());
        teacher.setActive(true);
        return teachers.save(teacher);
    }

    @Transactional(readOnly = true)
    public TeachingAssignment assignment(UUID id) {
        return assignments.findById(id).orElseThrow(() -> new NotFoundException("TeachingAssignment", id));
    }

    @Transactional(readOnly = true)
    public List<TeachingAssignment> assignmentsOfClass(UUID schoolClassId) {
        return assignments.findAllBySchoolClassId(schoolClassId);
    }

    @Transactional(readOnly = true)
    public List<TeachingAssignment> assignmentsOfTeacher(UUID teacherId) {
        return assignments.findAllByTeacher_Id(teacherId);
    }

    /** The subject must belong to a study plan of the class's level, and each session type is assigned once. */
    public TeachingAssignment createAssignment(UUID studyPlanSubjectId, UUID schoolClassId, SessionType sessionType,
            Integer plannedHours, UUID teacherId) {
        StudyPlanSubject planSubject = studyPlanSubjects.findById(studyPlanSubjectId)
                .orElseThrow(() -> new NotFoundException("StudyPlanSubject", studyPlanSubjectId));
        SchoolClassInfo schoolClass = parametrage.findSchoolClass(schoolClassId)
                .orElseThrow(() -> new NotFoundException("SchoolClass", schoolClassId));
        UUID planLevel = planSubject.getTeachingUnit().getStudyPlan().getStudyLevelId();
        if (!Objects.equals(planLevel, schoolClass.studyLevelId())) {
            throw new BusinessRuleException("assignment.level",
                    "This subject is not in a study plan of the class's level.");
        }
        if (assignments.existsByStudyPlanSubject_IdAndSchoolClassIdAndSessionType(studyPlanSubjectId, schoolClassId,
                sessionType)) {
            throw new BusinessRuleException("assignment.duplicate",
                    "This subject is already assigned to the class for " + sessionType + ".");
        }
        TeachingAssignment assignment = new TeachingAssignment();
        assignment.setStudyPlanSubject(planSubject);
        assignment.setSchoolClassId(schoolClassId);
        assignment.setAcademicYearId(schoolClass.academicYearId());
        assignment.setSessionType(sessionType);
        assignment.setPlannedHours(plannedHours);
        assignment.setTeacher(teacherId == null ? null : teacher(teacherId));
        return assignments.save(assignment);
    }

    public TeachingAssignment changeTeacher(UUID assignmentId, UUID teacherId) {
        TeachingAssignment assignment = assignment(assignmentId);
        assignment.setTeacher(teacherId == null ? null : teacher(teacherId));
        return assignment;
    }

    /** Fiche de charge: planned hours of the teacher's assignments in a year, compared to the statutory hours. */
    @Transactional(readOnly = true)
    public Workload workload(UUID teacherId, UUID academicYearId) {
        Teacher teacher = teacher(teacherId);
        int assigned = assignments.findAllByTeacher_IdAndAcademicYearId(teacherId, academicYearId).stream()
                .mapToInt(a -> a.getPlannedHours() == null ? 0 : a.getPlannedHours())
                .sum();
        Integer statutory = teacher.getStatutoryHours();
        return new Workload(teacherId, academicYearId, statutory, assigned, statutory != null && assigned > statutory);
    }

    /** The assignment, if this teacher teaches it. Used by the teacher portal so teachers only see their classes. */
    @Transactional(readOnly = true)
    public TeachingAssignment assignmentOfTeacher(UUID assignmentId, UUID teacherId) {
        TeachingAssignment assignment = assignment(assignmentId);
        if (assignment.getTeacher() == null || !assignment.getTeacher().getId().equals(teacherId)) {
            throw new NotFoundException("TeachingAssignment", assignmentId);
        }
        return assignment;
    }
}
