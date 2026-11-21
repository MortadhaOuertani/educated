package tn.tekup.educated.scolarite.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.parametrage.api.ParametrageDirectory;
import tn.tekup.educated.parametrage.api.SchoolClassInfo;
import tn.tekup.educated.scolarite.api.StudentEnrolled;
import tn.tekup.educated.scolarite.domain.ClassAssignment;
import tn.tekup.educated.scolarite.domain.ClassAssignmentRepository;
import tn.tekup.educated.scolarite.domain.Enrollment;
import tn.tekup.educated.scolarite.domain.EnrollmentRepository;
import tn.tekup.educated.scolarite.domain.EnrollmentStatus;
import tn.tekup.educated.scolarite.domain.EnrollmentType;
import tn.tekup.educated.scolarite.domain.PedagogicalStatus;
import tn.tekup.educated.scolarite.domain.Student;
import tn.tekup.educated.shared.web.BusinessRuleException;
import tn.tekup.educated.shared.web.NotFoundException;
import tn.tekup.educated.shared.tenant.TenantContext;

@Service
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollments;
    private final ClassAssignmentRepository assignments;
    private final StudentService studentService;
    private final ParametrageDirectory parametrage;
    private final ApplicationEventPublisher events;

    public EnrollmentService(EnrollmentRepository enrollments, ClassAssignmentRepository assignments,
            StudentService studentService, ParametrageDirectory parametrage, ApplicationEventPublisher events) {
        this.enrollments = enrollments;
        this.assignments = assignments;
        this.studentService = studentService;
        this.parametrage = parametrage;
        this.events = events;
    }

    /** Enrolls the student in the class's study level for the class's academic year, and assigns the class. */
    public Enrollment enroll(UUID studentId, UUID schoolClassId, EnrollmentType type, LocalDate date, boolean repeating) {
        Student student = studentService.get(studentId);
        SchoolClassInfo schoolClass = parametrage.findSchoolClass(schoolClassId)
                .orElseThrow(() -> new NotFoundException("SchoolClass", schoolClassId));
        if (enrollments.existsByStudent_IdAndAcademicYearIdAndStatusNot(studentId, schoolClass.academicYearId(),
                EnrollmentStatus.CANCELLED)) {
            throw new BusinessRuleException("enrollment.duplicate",
                    "The student is already enrolled for this academic year.");
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setAcademicYearId(schoolClass.academicYearId());
        enrollment.setStudyLevelId(schoolClass.studyLevelId());
        enrollment.setEnrollmentType(type);
        enrollment.setEnrollmentDate(date);
        enrollment.setRepeating(repeating);
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        enrollment.getAssignments().add(assignment(enrollment, schoolClassId, date, repeating));
        enrollment = enrollments.save(enrollment);
        events.publishEvent(new StudentEnrolled(TenantContext.requireEstablishment(), studentId, enrollment.getId(),
                schoolClass.academicYearId(), schoolClassId));
        return enrollment;
    }

    /** Moves the student to another class of the same academic year (Historique d'affectation keeps the old one). */
    public Enrollment changeClass(UUID enrollmentId, UUID newSchoolClassId, LocalDate date) {
        Enrollment enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new NotFoundException("Enrollment", enrollmentId));
        SchoolClassInfo newClass = parametrage.findSchoolClass(newSchoolClassId)
                .orElseThrow(() -> new NotFoundException("SchoolClass", newSchoolClassId));
        if (!newClass.academicYearId().equals(enrollment.getAcademicYearId())) {
            throw new BusinessRuleException("enrollment.otherYear", "The new class belongs to another academic year.");
        }
        for (ClassAssignment open : enrollment.getAssignments()) {
            if (open.getEndDate() == null) {
                if (open.getSchoolClassId().equals(newSchoolClassId)) {
                    throw new BusinessRuleException("enrollment.sameClass", "The student is already in this class.");
                }
                open.setEndDate(date);
            }
        }
        assignments.flush();
        enrollment.getAssignments().add(assignment(enrollment, newSchoolClassId, date, enrollment.isRepeating()));
        return enrollment;
    }

    @Transactional(readOnly = true)
    public List<Enrollment> enrollmentsOf(UUID studentId) {
        return enrollments.findAllByStudent_IdOrderByEnrollmentDateDesc(studentId);
    }

    @Transactional(readOnly = true)
    public Optional<SchoolClassInfo> currentClassOf(UUID studentId) {
        return assignments.findOpenForStudent(studentId).stream().findFirst()
                .flatMap(assignment -> parametrage.findSchoolClass(assignment.getSchoolClassId()));
    }

    private static ClassAssignment assignment(Enrollment enrollment, UUID schoolClassId, LocalDate date,
            boolean repeating) {
        ClassAssignment assignment = new ClassAssignment();
        assignment.setEnrollment(enrollment);
        assignment.setSchoolClassId(schoolClassId);
        assignment.setStartDate(date);
        assignment.setPedagogicalStatus(repeating ? PedagogicalStatus.REPEATING : PedagogicalStatus.REGULAR);
        return assignment;
    }
}
