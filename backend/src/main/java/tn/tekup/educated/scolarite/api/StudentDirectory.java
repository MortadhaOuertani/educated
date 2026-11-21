package tn.tekup.educated.scolarite.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.scolarite.domain.ClassAssignmentRepository;
import tn.tekup.educated.scolarite.domain.Student;
import tn.tekup.educated.scolarite.domain.StudentRepository;

/** Read-only lookups other modules use (class rosters, student names). */
@Service
@Transactional(readOnly = true)
public class StudentDirectory {

    private final StudentRepository students;
    private final ClassAssignmentRepository assignments;

    StudentDirectory(StudentRepository students, ClassAssignmentRepository assignments) {
        this.students = students;
        this.assignments = assignments;
    }

    public Optional<StudentInfo> findStudent(UUID id) {
        return students.findById(id).map(StudentDirectory::toInfo);
    }

    public Optional<StudentInfo> findByMatricule(String matricule) {
        return students.findByMatricule(matricule).map(StudentDirectory::toInfo);
    }

    /** Students currently assigned to the class. */
    public List<UUID> studentIdsInClass(UUID schoolClassId) {
        return assignments.findAllBySchoolClassIdAndEndDateIsNull(schoolClassId).stream()
                .map(assignment -> assignment.getEnrollment().getStudent().getId())
                .toList();
    }

    /** The class the student is in now, if they are enrolled. */
    public Optional<UUID> currentClassId(UUID studentId) {
        return assignments.findOpenForStudent(studentId).stream().findFirst()
                .map(assignment -> assignment.getSchoolClassId());
    }

    public Optional<EnrollmentInfo> currentEnrollment(UUID studentId) {
        return assignments.findOpenForStudent(studentId).stream().findFirst()
                .map(a -> new EnrollmentInfo(a.getEnrollment().getId(), studentId,
                        a.getEnrollment().getAcademicYearId(), a.getEnrollment().getStudyLevelId(),
                        a.getSchoolClassId()));
    }

    private static StudentInfo toInfo(Student s) {
        return new StudentInfo(s.getId(), s.getMatricule(), s.getPerson() == null ? null : s.getPerson().fullName());
    }
}
