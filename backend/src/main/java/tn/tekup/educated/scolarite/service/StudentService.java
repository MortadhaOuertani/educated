package tn.tekup.educated.scolarite.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.scolarite.domain.Guardian;
import tn.tekup.educated.scolarite.domain.GuardianRepository;
import tn.tekup.educated.scolarite.domain.Kinship;
import tn.tekup.educated.scolarite.domain.KinshipRepository;
import tn.tekup.educated.scolarite.domain.Student;
import tn.tekup.educated.scolarite.domain.StudentGuardian;
import tn.tekup.educated.scolarite.domain.StudentGuardianRepository;
import tn.tekup.educated.scolarite.domain.StudentRepository;
import tn.tekup.educated.scolarite.domain.StudentStatus;
import tn.tekup.educated.shared.domain.PersonInfo;
import tn.tekup.educated.shared.web.BusinessRuleException;
import tn.tekup.educated.shared.web.NotFoundException;

@Service
@Transactional
public class StudentService {

    public record StudentData(PersonInfo person, String matricule, LocalDate admissionDate, String baccalaureateSection,
            BigDecimal baccalaureateAverage, Integer baccalaureateYear, String previousInstitution) {
    }

    public record GuardianData(PersonInfo person, String profession, String employer, String workPhone,
            String preferredLanguage, boolean smsOptIn) {
    }

    private final StudentRepository students;
    private final GuardianRepository guardians;
    private final StudentGuardianRepository studentGuardians;
    private final KinshipRepository kinships;

    public StudentService(StudentRepository students, GuardianRepository guardians,
            StudentGuardianRepository studentGuardians, KinshipRepository kinships) {
        this.students = students;
        this.guardians = guardians;
        this.studentGuardians = studentGuardians;
        this.kinships = kinships;
    }

    @Transactional(readOnly = true)
    public Page<Student> search(String text, Pageable pageable) {
        return students.search(text == null ? "" : text.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public Student get(UUID id) {
        return students.findById(id).orElseThrow(() -> new NotFoundException("Student", id));
    }

    public Student create(StudentData data) {
        if (students.findByMatricule(data.matricule()).isPresent()) {
            throw new BusinessRuleException("student.matricule", "Matricule " + data.matricule() + " is already used.");
        }
        Student student = new Student();
        student.setStatus(StudentStatus.ACTIVE);
        apply(student, data);
        return students.save(student);
    }

    public Student update(UUID id, StudentData data) {
        Student student = get(id);
        if (!student.getMatricule().equals(data.matricule())
                && students.findByMatricule(data.matricule()).isPresent()) {
            throw new BusinessRuleException("student.matricule", "Matricule " + data.matricule() + " is already used.");
        }
        apply(student, data);
        return student;
    }

    public Student changeStatus(UUID id, StudentStatus status) {
        Student student = get(id);
        student.setStatus(status);
        return student;
    }

    @Transactional(readOnly = true)
    public List<StudentGuardian> guardiansOf(UUID studentId) {
        get(studentId);
        return studentGuardians.findAllByStudent_Id(studentId);
    }

    /** Creates a guardian and links them to the student. Only one guardian can be the primary contact. */
    public StudentGuardian addGuardian(UUID studentId, GuardianData data, String kinshipCode, boolean primaryContact,
            boolean financialResponsible) {
        Student student = get(studentId);
        Kinship kinship = kinships.findByCode(kinshipCode)
                .orElseThrow(() -> new NotFoundException("Kinship", kinshipCode));
        Guardian guardian = new Guardian();
        guardian.setPerson(data.person());
        guardian.setProfession(data.profession());
        guardian.setEmployer(data.employer());
        guardian.setWorkPhone(data.workPhone());
        guardian.setPreferredLanguage(data.preferredLanguage());
        guardian.setSmsOptIn(data.smsOptIn());
        guardian = guardians.save(guardian);

        if (primaryContact) {
            studentGuardians.findAllByStudent_Id(studentId).forEach(link -> link.setPrimaryContact(false));
            studentGuardians.flush();
        }
        StudentGuardian link = new StudentGuardian();
        link.setStudent(student);
        link.setGuardian(guardian);
        link.setKinship(kinship);
        link.setPrimaryContact(primaryContact);
        link.setFinancialResponsible(financialResponsible);
        link.setNotifyPush(true);
        link.setNotifySms(data.smsOptIn());
        return studentGuardians.save(link);
    }

    @Transactional(readOnly = true)
    public List<Kinship> kinships() {
        return kinships.findAllByOrderByCode();
    }

    private static void apply(Student student, StudentData data) {
        student.setPerson(data.person());
        student.setMatricule(data.matricule());
        student.setAdmissionDate(data.admissionDate());
        student.setBaccalaureateSection(data.baccalaureateSection());
        student.setBaccalaureateAverage(data.baccalaureateAverage());
        student.setBaccalaureateYear(data.baccalaureateYear());
        student.setPreviousInstitution(data.previousInstitution());
    }
}
