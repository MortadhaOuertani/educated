package tn.tekup.educated.stages.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.parametrage.api.AcademicYearInfo;
import tn.tekup.educated.parametrage.api.ParametrageDirectory;
import tn.tekup.educated.pedagogie.api.TeachingDirectory;
import tn.tekup.educated.shared.domain.Address;
import tn.tekup.educated.shared.security.CurrentUserProvider;
import tn.tekup.educated.shared.web.BusinessRuleException;
import tn.tekup.educated.shared.web.NotFoundException;
import tn.tekup.educated.stages.api.InternshipStatusChanged;
import tn.tekup.educated.stages.domain.Company;
import tn.tekup.educated.stages.domain.CompanyRepository;
import tn.tekup.educated.stages.domain.CompanySupervisor;
import tn.tekup.educated.stages.domain.DefenseSession;
import tn.tekup.educated.stages.domain.DefenseSessionRepository;
import tn.tekup.educated.stages.domain.Internship;
import tn.tekup.educated.stages.domain.InternshipDocument;
import tn.tekup.educated.stages.domain.InternshipDocumentType;
import tn.tekup.educated.stages.domain.InternshipNature;
import tn.tekup.educated.stages.domain.InternshipNatureRepository;
import tn.tekup.educated.stages.domain.InternshipRepository;
import tn.tekup.educated.stages.domain.InternshipStatus;
import tn.tekup.educated.stages.domain.InternshipStatusChange;

/**
 * The internship timeline: declared by the student (PENDING_APPROVAL), approved or refused by the internship office,
 * defense requested by the student, authorized by the academic supervisor then by the office, defended, validated.
 */
@Service
@Transactional
public class InternshipService {

    private static final Map<InternshipStatus, Set<InternshipStatus>> NEXT = Map.of(
            InternshipStatus.PENDING_APPROVAL, Set.of(InternshipStatus.APPROVED, InternshipStatus.REFUSED),
            InternshipStatus.APPROVED, Set.of(InternshipStatus.DEFENSE_REQUESTED),
            InternshipStatus.DEFENSE_REQUESTED, Set.of(InternshipStatus.AUTHORIZED_BY_SUPERVISOR),
            InternshipStatus.AUTHORIZED_BY_SUPERVISOR, Set.of(InternshipStatus.AUTHORIZED_TO_DEFEND),
            InternshipStatus.AUTHORIZED_TO_DEFEND, Set.of(InternshipStatus.DEFENDED),
            InternshipStatus.DEFENDED, Set.of(InternshipStatus.VALIDATED),
            InternshipStatus.REFUSED, Set.of(),
            InternshipStatus.VALIDATED, Set.of());

    public record CompanyData(String name, String sector, String email, String phone, Address address) {
    }

    public record SupervisorData(String firstName, String lastName, String position, String email, String phone) {
    }

    public record InternshipData(UUID natureId, UUID companyId, CompanyData newCompany, SupervisorData supervisor,
            String title, String objectives, LocalDate startDate, LocalDate endDate) {
    }

    private final InternshipRepository internships;
    private final InternshipNatureRepository natures;
    private final CompanyRepository companies;
    private final DefenseSessionRepository sessions;
    private final ParametrageDirectory parametrage;
    private final TeachingDirectory teaching;
    private final CurrentUserProvider currentUserProvider;
    private final ApplicationEventPublisher events;

    public InternshipService(InternshipRepository internships, InternshipNatureRepository natures,
            CompanyRepository companies, DefenseSessionRepository sessions, ParametrageDirectory parametrage,
            TeachingDirectory teaching, CurrentUserProvider currentUserProvider, ApplicationEventPublisher events) {
        this.internships = internships;
        this.natures = natures;
        this.companies = companies;
        this.sessions = sessions;
        this.parametrage = parametrage;
        this.teaching = teaching;
        this.currentUserProvider = currentUserProvider;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<InternshipNature> natures() {
        return natures.findAllByOrderByCode();
    }

    @Transactional(readOnly = true)
    public List<Company> companies() {
        return companies.findAllByActiveTrueOrderByName();
    }

    public Company createCompany(CompanyData data, boolean partner) {
        if (data.name() == null || data.name().isBlank()) {
            throw new BusinessRuleException("company.name", "The company name is required.");
        }
        Company company = new Company();
        company.setName(data.name().trim());
        company.setSector(data.sector());
        company.setEmail(data.email());
        company.setPhone(data.phone());
        company.setAddress(data.address());
        company.setPartner(partner);
        company.setActive(true);
        return companies.save(company);
    }

    @Transactional(readOnly = true)
    public Internship internship(UUID id) {
        return internships.findById(id).orElseThrow(() -> new NotFoundException("Internship", id));
    }

    @Transactional(readOnly = true)
    public List<Internship> list(InternshipStatus status) {
        return status == null ? internships.findAllByOrderByCreatedAtDesc() : internships.findAllByStatusOrderByCreatedAt(status);
    }

    @Transactional(readOnly = true)
    public List<Internship> internshipsOf(UUID studentId) {
        return internships.findAllByStudentIdOrderByCreatedAtDesc(studentId);
    }

    @Transactional(readOnly = true)
    public List<Internship> supervisedBy(UUID teacherId) {
        return internships.findAllByAcademicSupervisorIdOrderByCreatedAtDesc(teacherId);
    }

    /** The student declares an internship of the current academic year. */
    public Internship declare(UUID studentId, InternshipData data) {
        AcademicYearInfo year = parametrage.currentAcademicYear()
                .orElseThrow(() -> new BusinessRuleException("internship.noYear", "No academic year is open."));
        InternshipNature nature = natures.findById(data.natureId())
                .orElseThrow(() -> new NotFoundException("InternshipNature", data.natureId()));
        if (!data.endDate().isAfter(data.startDate())) {
            throw new BusinessRuleException("internship.dates", "The internship must end after it starts.");
        }
        long weeks = ChronoUnit.WEEKS.between(data.startDate(), data.endDate().plusDays(1));
        if (nature.getMinWeeks() != null && weeks < nature.getMinWeeks()) {
            throw new BusinessRuleException("internship.duration",
                    "This internship must last at least " + nature.getMinWeeks() + " weeks.");
        }
        boolean alreadyDeclared = internships.findAllByStudentIdAndAcademicYearIdAndNature_Id(studentId, year.id(),
                nature.getId()).stream().anyMatch(i -> i.getStatus() != InternshipStatus.REFUSED);
        if (alreadyDeclared) {
            throw new BusinessRuleException("internship.duplicate", "You already declared this internship this year.");
        }
        Company company = data.companyId() != null
                ? companies.findById(data.companyId()).orElseThrow(() -> new NotFoundException("Company", data.companyId()))
                : createCompany(data.newCompany() == null ? new CompanyData(null, null, null, null, null) : data.newCompany(),
                        false);

        Internship internship = new Internship();
        internship.setStudentId(studentId);
        internship.setAcademicYearId(year.id());
        internship.setReferenceNumber("STG-" + year.code().substring(0, 4) + "-"
                + String.format("%05d", internships.countByAcademicYearId(year.id()) + 1));
        internship.setNature(nature);
        internship.setCompany(company);
        internship.setTitle(data.title());
        internship.setObjectives(data.objectives());
        internship.setStartDate(data.startDate());
        internship.setEndDate(data.endDate());
        if (data.supervisor() != null && data.supervisor().lastName() != null) {
            CompanySupervisor supervisor = new CompanySupervisor();
            supervisor.setCompany(company);
            supervisor.setFirstName(data.supervisor().firstName() == null ? "" : data.supervisor().firstName());
            supervisor.setLastName(data.supervisor().lastName());
            supervisor.setPosition(data.supervisor().position());
            supervisor.setEmail(data.supervisor().email());
            supervisor.setPhone(data.supervisor().phone());
            company.getSupervisors().add(supervisor);
            internship.setCompanySupervisor(supervisor);
        }
        internship.setStatus(InternshipStatus.PENDING_APPROVAL);
        internship.getStatusChanges().add(change(internship, null, InternshipStatus.PENDING_APPROVAL, null));
        return internships.save(internship);
    }

    /** The internship office approves the internship and names the academic supervisor (a teacher). */
    public Internship approve(UUID id, UUID academicSupervisorId) {
        Internship internship = internship(id);
        if (academicSupervisorId == null || teaching.findTeacher(academicSupervisorId).isEmpty()) {
            throw new NotFoundException("Teacher", academicSupervisorId);
        }
        move(internship, InternshipStatus.APPROVED, null);
        internship.setAcademicSupervisorId(academicSupervisorId);
        internship.setApprovedAt(Instant.now());
        internship.setApprovedByUserId(currentUserProvider.require().userId());
        return internship;
    }

    public Internship refuse(UUID id, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessRuleException("internship.reason", "Give a reason when refusing an internship.");
        }
        Internship internship = internship(id);
        move(internship, InternshipStatus.REFUSED, reason);
        internship.setRefusalReason(reason);
        return internship;
    }

    public Internship addDocument(Internship internship, InternshipDocumentType type, UUID fileId) {
        if (internship.getStatus() == InternshipStatus.REFUSED || internship.getStatus() == InternshipStatus.VALIDATED) {
            throw new BusinessRuleException("internship.closed", "Documents can no longer be added.");
        }
        InternshipDocument document = new InternshipDocument();
        document.setInternship(internship);
        document.setDocumentType(type);
        document.setFileId(fileId);
        internship.getDocuments().add(document);
        return internship;
    }

    /** Demande de soutenance: needs a report and a defense session whose request period is open. */
    public Internship requestDefense(Internship internship, UUID defenseSessionId) {
        if (!internship.getNature().isRequiresDefense()) {
            throw new BusinessRuleException("internship.noDefense", "This kind of internship has no defense.");
        }
        DefenseSession session = sessions.findById(defenseSessionId)
                .orElseThrow(() -> new NotFoundException("DefenseSession", defenseSessionId));
        LocalDate today = LocalDate.now();
        boolean open = (session.getRequestsOpenFrom() == null || !today.isBefore(session.getRequestsOpenFrom()))
                && (session.getRequestsOpenTo() == null || !today.isAfter(session.getRequestsOpenTo()));
        if (!open) {
            throw new BusinessRuleException("internship.requestsClosed", "Defense requests are closed for this session.");
        }
        boolean hasReport = internship.getDocuments().stream()
                .anyMatch(d -> d.getDocumentType() == InternshipDocumentType.REPORT);
        if (!hasReport) {
            throw new BusinessRuleException("internship.reportMissing", "Upload your report before requesting a defense.");
        }
        move(internship, InternshipStatus.DEFENSE_REQUESTED, null);
        internship.setRequestedSession(session);
        internship.setDefenseRequestedAt(Instant.now());
        return internship;
    }

    /** The academic supervisor agrees that the student may defend. */
    public Internship authorizeBySupervisor(Internship internship, UUID teacherId) {
        if (!teacherId.equals(internship.getAcademicSupervisorId())) {
            throw new NotFoundException("Internship", internship.getId());
        }
        move(internship, InternshipStatus.AUTHORIZED_BY_SUPERVISOR, null);
        return internship;
    }

    /** The internship office gives the final authorization to defend. */
    public Internship authorizeDefense(UUID id) {
        Internship internship = internship(id);
        move(internship, InternshipStatus.AUTHORIZED_TO_DEFEND, null);
        return internship;
    }

    /** Moves the internship to the next step, keeps the history and tells other modules. */
    void move(Internship internship, InternshipStatus to, String comment) {
        InternshipStatus from = internship.getStatus();
        if (!NEXT.get(from).contains(to)) {
            throw new BusinessRuleException("internship.transition",
                    "An internship cannot go from " + from + " to " + to + ".");
        }
        internship.setStatus(to);
        internship.getStatusChanges().add(change(internship, from, to, comment));
        events.publishEvent(new InternshipStatusChanged(internship.getEstablishmentId(), internship.getId(),
                internship.getStudentId(), internship.getAcademicSupervisorId(), to.name(), comment));
    }

    private static InternshipStatusChange change(Internship internship, InternshipStatus from, InternshipStatus to,
            String comment) {
        InternshipStatusChange change = new InternshipStatusChange();
        change.setInternship(internship);
        change.setFromStatus(from);
        change.setToStatus(to);
        change.setComment(comment);
        return change;
    }
}
