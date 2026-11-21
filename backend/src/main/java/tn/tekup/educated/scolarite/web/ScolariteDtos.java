package tn.tekup.educated.scolarite.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import tn.tekup.educated.parametrage.api.SchoolClassInfo;
import tn.tekup.educated.scolarite.domain.ClassAssignment;
import tn.tekup.educated.scolarite.domain.DocumentLanguage;
import tn.tekup.educated.scolarite.domain.DocumentRequest;
import tn.tekup.educated.scolarite.domain.DocumentRequestStatus;
import tn.tekup.educated.scolarite.domain.DocumentType;
import tn.tekup.educated.scolarite.domain.Enrollment;
import tn.tekup.educated.scolarite.domain.EnrollmentStatus;
import tn.tekup.educated.scolarite.domain.EnrollmentType;
import tn.tekup.educated.scolarite.domain.Kinship;
import tn.tekup.educated.scolarite.domain.PedagogicalStatus;
import tn.tekup.educated.scolarite.domain.Sanction;
import tn.tekup.educated.scolarite.domain.SanctionType;
import tn.tekup.educated.scolarite.domain.Student;
import tn.tekup.educated.scolarite.domain.StudentGuardian;
import tn.tekup.educated.scolarite.domain.StudentStatus;
import tn.tekup.educated.shared.domain.LocalizedText;
import tn.tekup.educated.shared.domain.PersonInfo;

/** Request and response bodies of the scolarite REST API. */
final class ScolariteDtos {

    private ScolariteDtos() {
    }

    record StudentDto(UUID id, String matricule, StudentStatus status, PersonInfo person, LocalDate admissionDate,
            String baccalaureateSection, BigDecimal baccalaureateAverage, Integer baccalaureateYear,
            String previousInstitution) {
        static StudentDto from(Student s) {
            return new StudentDto(s.getId(), s.getMatricule(), s.getStatus(), s.getPerson(), s.getAdmissionDate(),
                    s.getBaccalaureateSection(), s.getBaccalaureateAverage(), s.getBaccalaureateYear(),
                    s.getPreviousInstitution());
        }
    }

    record StudentRequest(@NotBlank @Size(max = 40) String matricule, @NotNull PersonInfo person,
            LocalDate admissionDate, String baccalaureateSection, BigDecimal baccalaureateAverage,
            Integer baccalaureateYear, String previousInstitution) {
    }

    record StatusRequest(@NotNull StudentStatus status) {
    }

    record StudentProfileDto(StudentDto student, SchoolClassInfo currentClass) {
    }

    record GuardianLinkDto(UUID guardianId, PersonInfo person, String profession, String kinshipCode,
            LocalizedText kinship, boolean primaryContact, boolean financialResponsible) {
        static GuardianLinkDto from(StudentGuardian link) {
            return new GuardianLinkDto(link.getGuardian().getId(), link.getGuardian().getPerson(),
                    link.getGuardian().getProfession(), link.getKinship().getCode(), link.getKinship().getLabel(),
                    link.isPrimaryContact(), link.isFinancialResponsible());
        }
    }

    record NewGuardianRequest(@NotNull PersonInfo person, String profession, String employer, String workPhone,
            String preferredLanguage, boolean smsOptIn, @NotBlank String kinshipCode, boolean primaryContact,
            boolean financialResponsible) {
    }

    record KinshipDto(String code, LocalizedText label) {
        static KinshipDto from(Kinship k) {
            return new KinshipDto(k.getCode(), k.getLabel());
        }
    }

    record AssignmentDto(UUID schoolClassId, String groupCode, LocalDate startDate, LocalDate endDate,
            PedagogicalStatus pedagogicalStatus) {
        static AssignmentDto from(ClassAssignment a) {
            return new AssignmentDto(a.getSchoolClassId(), a.getGroupCode(), a.getStartDate(), a.getEndDate(),
                    a.getPedagogicalStatus());
        }
    }

    record EnrollmentDto(UUID id, UUID academicYearId, UUID studyLevelId, LocalDate enrollmentDate,
            EnrollmentType enrollmentType, EnrollmentStatus status, boolean repeating, List<AssignmentDto> assignments) {
        static EnrollmentDto from(Enrollment e) {
            return new EnrollmentDto(e.getId(), e.getAcademicYearId(), e.getStudyLevelId(), e.getEnrollmentDate(),
                    e.getEnrollmentType(), e.getStatus(), e.isRepeating(),
                    e.getAssignments().stream().map(AssignmentDto::from).toList());
        }
    }

    record EnrollRequest(@NotNull UUID schoolClassId, @NotNull EnrollmentType enrollmentType,
            @NotNull LocalDate enrollmentDate, boolean repeating) {
    }

    record ChangeClassRequest(@NotNull UUID schoolClassId, @NotNull LocalDate date) {
    }

    record SanctionDto(UUID id, SanctionType sanctionType, String reason, LocalDate incidentDate, LocalDate startDate,
            LocalDate endDate, boolean notifyGuardians, Instant notifiedAt) {
        static SanctionDto from(Sanction s) {
            return new SanctionDto(s.getId(), s.getSanctionType(), s.getReason(), s.getIncidentDate(),
                    s.getStartDate(), s.getEndDate(), s.isNotifyGuardians(), s.getNotifiedAt());
        }
    }

    record SanctionRequest(@NotNull SanctionType sanctionType, @NotBlank @Size(max = 1000) String reason,
            @NotNull LocalDate incidentDate, LocalDate startDate, LocalDate endDate, boolean notifyGuardians) {
    }

    record DocumentTypeDto(UUID id, String code, LocalizedText label, DocumentLanguage language) {
        static DocumentTypeDto from(DocumentType t) {
            return new DocumentTypeDto(t.getId(), t.getCode(), t.getLabel(), t.getLanguage());
        }
    }

    record DocumentRequestDto(UUID id, UUID studentId, String studentName, String matricule, DocumentTypeDto documentType,
            String reason, boolean hasProof, DocumentRequestStatus status, Instant requestedAt, Instant processedAt,
            String rejectionReason) {
        static DocumentRequestDto from(DocumentRequest r) {
            Student student = r.getStudent();
            return new DocumentRequestDto(r.getId(), student.getId(),
                    student.getPerson() == null ? null : student.getPerson().fullName(), student.getMatricule(),
                    DocumentTypeDto.from(r.getDocumentType()), r.getReason(), r.getProofFileId() != null, r.getStatus(),
                    r.getCreatedAt(), r.getProcessedAt(), r.getRejectionReason());
        }
    }

    record StatusChangeRequest(@NotNull DocumentRequestStatus status, @Size(max = 1000) String rejectionReason) {
    }
}
