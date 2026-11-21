package tn.tekup.educated.stages.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import tn.tekup.educated.pedagogie.api.TeacherInfo;
import tn.tekup.educated.pedagogie.api.TeachingDirectory;
import tn.tekup.educated.scolarite.api.StudentDirectory;
import tn.tekup.educated.scolarite.api.StudentInfo;
import tn.tekup.educated.shared.domain.Address;
import tn.tekup.educated.shared.domain.LocalizedText;
import tn.tekup.educated.stages.domain.Company;
import tn.tekup.educated.stages.domain.CompanySupervisor;
import tn.tekup.educated.stages.domain.Defense;
import tn.tekup.educated.stages.domain.DefenseSession;
import tn.tekup.educated.stages.domain.Internship;
import tn.tekup.educated.stages.domain.InternshipNature;
import tn.tekup.educated.stages.domain.Jury;
import tn.tekup.educated.stages.service.DefenseService;

/** Builds the JSON views of the internship module, with student and teacher names. */
@Component
class StagesViews {

    record NatureDto(UUID id, String code, LocalizedText label, boolean requiresDefense, Integer minWeeks) {
        static NatureDto from(InternshipNature n) {
            return new NatureDto(n.getId(), n.getCode(), n.getLabel(), n.isRequiresDefense(), n.getMinWeeks());
        }
    }

    record CompanyDto(UUID id, String name, String sector, String email, String phone, Address address,
            boolean partner) {
        static CompanyDto from(Company c) {
            return new CompanyDto(c.getId(), c.getName(), c.getSector(), c.getEmail(), c.getPhone(), c.getAddress(),
                    c.isPartner());
        }
    }

    record SessionDto(UUID id, String name, UUID academicYearId, LocalDate startDate, LocalDate endDate,
            LocalDate requestsOpenFrom, LocalDate requestsOpenTo) {
        static SessionDto from(DefenseSession s) {
            return new SessionDto(s.getId(), s.getName(), s.getAcademicYearId(), s.getStartDate(), s.getEndDate(),
                    s.getRequestsOpenFrom(), s.getRequestsOpenTo());
        }
    }

    record DocumentDto(UUID id, String documentType, UUID fileId, Instant createdAt) {
    }

    record HistoryDto(String fromStatus, String toStatus, String comment, Instant at) {
    }

    record DefenseDto(UUID id, UUID internshipId, String internshipTitle, String studentName, UUID juryId,
            String juryCode, LocalDate date, LocalTime startTime, LocalTime endTime, String status, BigDecimal grade,
            String mention, boolean published) {
    }

    record InternshipDto(UUID id, String referenceNumber, String title, String objectives, LocalDate startDate,
            LocalDate endDate, String status, String natureCode, LocalizedText nature, UUID companyId,
            String companyName, String companySupervisor, UUID studentId, String matricule, String studentName,
            UUID academicSupervisorId, String academicSupervisorName, String refusalReason, UUID requestedSessionId,
            List<DocumentDto> documents, List<HistoryDto> history, DefenseDto defense) {
    }

    record MemberDto(UUID teacherId, String name, String role) {
    }

    record JuryDto(UUID id, String code, LocalDate juryDate, UUID roomId, UUID defenseSessionId,
            List<MemberDto> members, List<DefenseDto> defenses) {
    }

    private final StudentDirectory students;
    private final TeachingDirectory teaching;
    private final DefenseService defenses;

    StagesViews(StudentDirectory students, TeachingDirectory teaching, DefenseService defenses) {
        this.students = students;
        this.teaching = teaching;
        this.defenses = defenses;
    }

    /** Students only see the defense grade once it is published. */
    InternshipDto internship(Internship i, boolean forStudent) {
        Optional<StudentInfo> student = students.findStudent(i.getStudentId());
        CompanySupervisor supervisor = i.getCompanySupervisor();
        DefenseDto defense = defenses.defenseOf(i.getId()).map(d -> defense(d, forStudent)).orElse(null);
        return new InternshipDto(i.getId(), i.getReferenceNumber(), i.getTitle(), i.getObjectives(), i.getStartDate(),
                i.getEndDate(), i.getStatus().name(), i.getNature().getCode(), i.getNature().getLabel(),
                i.getCompany().getId(), i.getCompany().getName(),
                supervisor == null ? null : (supervisor.getFirstName() + " " + supervisor.getLastName()).trim(),
                i.getStudentId(), student.map(StudentInfo::matricule).orElse(null),
                student.map(StudentInfo::fullName).orElse(null), i.getAcademicSupervisorId(),
                teacherName(i.getAcademicSupervisorId()), i.getRefusalReason(),
                i.getRequestedSession() == null ? null : i.getRequestedSession().getId(),
                i.getDocuments().stream().map(d -> new DocumentDto(d.getId(), d.getDocumentType().name(),
                        d.getFileId(), d.getCreatedAt())).toList(),
                i.getStatusChanges().stream()
                        .sorted(Comparator.comparing(c -> c.getCreatedAt() == null ? Instant.now() : c.getCreatedAt()))
                        .map(c -> new HistoryDto(c.getFromStatus() == null ? null : c.getFromStatus().name(),
                                c.getToStatus().name(), c.getComment(), c.getCreatedAt()))
                        .toList(),
                defense);
    }

    DefenseDto defense(Defense d, boolean forStudent) {
        boolean showGrade = !forStudent || d.isPublished();
        return new DefenseDto(d.getId(), d.getInternship().getId(), d.getInternship().getTitle(),
                students.findStudent(d.getInternship().getStudentId()).map(StudentInfo::fullName).orElse(null),
                d.getJury().getId(), d.getJury().getCode(), d.getDefenseDate(), d.getStartTime(), d.getEndTime(),
                d.getStatus().name(), showGrade ? d.getGrade() : null,
                showGrade && d.getMention() != null ? d.getMention().name() : null, d.isPublished());
    }

    JuryDto jury(Jury j) {
        List<MemberDto> members = j.getMembers().stream()
                .map(m -> new MemberDto(m.getTeacherId(),
                        m.getTeacherId() != null ? teacherName(m.getTeacherId()) : m.getExternalName(),
                        m.getRole().name()))
                .toList();
        List<DefenseDto> planned = defenses.defensesOf(j.getId()).stream().map(d -> defense(d, false)).toList();
        return new JuryDto(j.getId(), j.getCode(), j.getJuryDate(), j.getRoomId(), j.getDefenseSession().getId(),
                members, planned);
    }

    private String teacherName(UUID teacherId) {
        return teacherId == null ? null : teaching.findTeacher(teacherId).map(TeacherInfo::fullName).orElse(null);
    }
}
