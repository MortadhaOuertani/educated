package tn.tekup.educated.scolarite.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.scolarite.api.SanctionIssued;
import tn.tekup.educated.scolarite.domain.Sanction;
import tn.tekup.educated.scolarite.domain.SanctionRepository;
import tn.tekup.educated.scolarite.domain.SanctionType;
import tn.tekup.educated.scolarite.domain.Student;
import tn.tekup.educated.scolarite.domain.StudentStatus;
import tn.tekup.educated.shared.audit.AuditLogger;
import tn.tekup.educated.shared.security.CurrentUserProvider;
import tn.tekup.educated.shared.web.BusinessRuleException;
import tn.tekup.educated.shared.tenant.TenantContext;

@Service
@Transactional
public class SanctionService {

    private final SanctionRepository sanctions;
    private final StudentService studentService;
    private final CurrentUserProvider currentUserProvider;
    private final AuditLogger audit;
    private final ApplicationEventPublisher events;

    public SanctionService(SanctionRepository sanctions, StudentService studentService,
            CurrentUserProvider currentUserProvider, AuditLogger audit, ApplicationEventPublisher events) {
        this.sanctions = sanctions;
        this.studentService = studentService;
        this.currentUserProvider = currentUserProvider;
        this.audit = audit;
        this.events = events;
    }

    public Sanction issue(UUID studentId, SanctionType type, String reason, LocalDate incidentDate, LocalDate startDate,
            LocalDate endDate, boolean notifyGuardians) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessRuleException("sanction.dates", "The end date is before the start date.");
        }
        Student student = studentService.get(studentId);
        Sanction sanction = new Sanction();
        sanction.setStudent(student);
        sanction.setSanctionType(type);
        sanction.setReason(reason);
        sanction.setIncidentDate(incidentDate);
        sanction.setStartDate(startDate);
        sanction.setEndDate(endDate);
        sanction.setNotifyGuardians(notifyGuardians);
        sanction.setIssuedByUserId(currentUserProvider.require().userId());
        sanction = sanctions.save(sanction);
        if (type == SanctionType.DEFINITIVE_EXCLUSION) {
            student.setStatus(StudentStatus.EXCLUDED);
        }
        audit.record("scolarite", "SANCTION_ISSUED", "Sanction", sanction.getId(), null);
        events.publishEvent(new SanctionIssued(TenantContext.requireEstablishment(), sanction.getId(), studentId,
                notifyGuardians));
        return sanction;
    }

    @Transactional(readOnly = true)
    public List<Sanction> sanctionsOf(UUID studentId) {
        return sanctions.findAllByStudent_IdOrderByIncidentDateDesc(studentId);
    }
}
