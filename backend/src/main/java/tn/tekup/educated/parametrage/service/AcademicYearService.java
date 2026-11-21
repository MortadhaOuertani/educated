package tn.tekup.educated.parametrage.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.parametrage.api.AcademicYearOpened;
import tn.tekup.educated.parametrage.domain.AcademicPeriod;
import tn.tekup.educated.parametrage.domain.AcademicYear;
import tn.tekup.educated.parametrage.domain.AcademicYearRepository;
import tn.tekup.educated.parametrage.domain.AcademicYearStatus;
import tn.tekup.educated.parametrage.domain.PeriodType;
import tn.tekup.educated.shared.domain.LocalizedText;
import tn.tekup.educated.shared.web.BusinessRuleException;
import tn.tekup.educated.shared.web.NotFoundException;

@Service
@Transactional
public class AcademicYearService {

    public record NewPeriod(String code, String name, LocalDate startDate, LocalDate endDate) {
    }

    private final AcademicYearRepository academicYears;
    private final ApplicationEventPublisher events;

    public AcademicYearService(AcademicYearRepository academicYears, ApplicationEventPublisher events) {
        this.academicYears = academicYears;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<AcademicYear> list() {
        return academicYears.findAllByOrderByStartDateDesc();
    }

    @Transactional(readOnly = true)
    public AcademicYear get(UUID id) {
        return academicYears.findById(id).orElseThrow(() -> new NotFoundException("AcademicYear", id));
    }

    public AcademicYear create(String code, LocalDate startDate, LocalDate endDate, List<NewPeriod> periods) {
        if (!startDate.isBefore(endDate)) {
            throw new BusinessRuleException("academicYear.dates", "The start date must be before the end date.");
        }
        if (academicYears.existsByCode(code)) {
            throw new BusinessRuleException("academicYear.duplicate", "Academic year " + code + " already exists.");
        }
        AcademicYear year = new AcademicYear();
        year.setCode(code);
        year.setStartDate(startDate);
        year.setEndDate(endDate);
        year.setStatus(AcademicYearStatus.PLANNED);
        int order = 1;
        for (NewPeriod p : periods) {
            if (p.startDate().isBefore(startDate) || p.endDate().isAfter(endDate) || !p.startDate().isBefore(p.endDate())) {
                throw new BusinessRuleException("academicPeriod.dates",
                        "Period " + p.code() + " must lie inside the academic year.");
            }
            AcademicPeriod period = new AcademicPeriod();
            period.setCode(p.code());
            period.setName(LocalizedText.of(p.name()));
            period.setPeriodType(PeriodType.SEMESTER);
            period.setOrderIndex(order++);
            period.setStartDate(p.startDate());
            period.setEndDate(p.endDate());
            period.setAcademicYear(year);
            year.getPeriods().add(period);
        }
        return academicYears.save(year);
    }

    /** Makes the year current (only one current year per establishment) and announces it. */
    public AcademicYear open(UUID id) {
        AcademicYear year = get(id);
        if (year.getStatus() == AcademicYearStatus.CLOSED) {
            throw new BusinessRuleException("academicYear.closed", "A closed academic year cannot be reopened.");
        }
        academicYears.findFirstByCurrentTrue().ifPresent(previous -> previous.setCurrent(false));
        academicYears.flush();
        year.setStatus(AcademicYearStatus.OPEN);
        year.setCurrent(true);
        events.publishEvent(new AcademicYearOpened(year.getEstablishmentId(), year.getId(), year.getCode()));
        return year;
    }
}
