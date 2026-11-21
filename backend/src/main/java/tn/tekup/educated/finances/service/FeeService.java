package tn.tekup.educated.finances.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.finances.domain.FeePlanStatus;
import tn.tekup.educated.finances.domain.FeeSchedule;
import tn.tekup.educated.finances.domain.FeeScheduleRepository;
import tn.tekup.educated.finances.domain.FeeType;
import tn.tekup.educated.finances.domain.Installment;
import tn.tekup.educated.finances.domain.InstallmentStatus;
import tn.tekup.educated.finances.domain.StudentFeePlan;
import tn.tekup.educated.finances.domain.StudentFeePlanRepository;
import tn.tekup.educated.parametrage.api.AcademicYearInfo;
import tn.tekup.educated.parametrage.api.ParametrageDirectory;
import tn.tekup.educated.scolarite.api.EnrollmentInfo;
import tn.tekup.educated.scolarite.api.StudentDirectory;
import tn.tekup.educated.shared.domain.Money;
import tn.tekup.educated.shared.web.BusinessRuleException;
import tn.tekup.educated.shared.web.NotFoundException;

/**
 * Tarification and fee plans. When a student is enrolled, the fees of their study level for that year are split
 * into monthly installments (due on the 15th, starting the month the year starts).
 */
@Service
@Transactional
public class FeeService {

    private final FeeScheduleRepository schedules;
    private final StudentFeePlanRepository plans;
    private final StudentDirectory students;
    private final ParametrageDirectory parametrage;

    public FeeService(FeeScheduleRepository schedules, StudentFeePlanRepository plans, StudentDirectory students,
            ParametrageDirectory parametrage) {
        this.schedules = schedules;
        this.plans = plans;
        this.students = students;
        this.parametrage = parametrage;
    }

    @Transactional(readOnly = true)
    public List<FeeSchedule> schedulesOf(UUID academicYearId) {
        return schedules.findAllByAcademicYearIdOrderByFeeType(academicYearId);
    }

    public FeeSchedule createSchedule(UUID academicYearId, UUID studyLevelId, FeeType type, BigDecimal amount,
            int installmentsCount) {
        if (parametrage.findAcademicYear(academicYearId).isEmpty()) {
            throw new NotFoundException("AcademicYear", academicYearId);
        }
        if (!parametrage.studyLevelExists(studyLevelId)) {
            throw new NotFoundException("StudyLevel", studyLevelId);
        }
        if (amount.signum() <= 0 || installmentsCount < 1 || installmentsCount > 12) {
            throw new BusinessRuleException("feeSchedule.amount", "Give a positive amount and 1 to 12 installments.");
        }
        FeeSchedule schedule = new FeeSchedule();
        schedule.setAcademicYearId(academicYearId);
        schedule.setStudyLevelId(studyLevelId);
        schedule.setFeeType(type);
        schedule.setAmount(Money.tnd(amount));
        schedule.setInstallmentsCount(installmentsCount);
        schedule.setActive(true);
        return schedules.save(schedule);
    }

    @Transactional(readOnly = true)
    public List<StudentFeePlan> plansOf(UUID studentId) {
        return plans.findAllByStudentIdOrderByCreatedAtDesc(studentId);
    }

    @Transactional(readOnly = true)
    public StudentFeePlan plan(UUID id) {
        return plans.findById(id).orElseThrow(() -> new NotFoundException("StudentFeePlan", id));
    }

    /** Creates the plan of the student's current enrollment (for students enrolled before the fees were set). */
    public StudentFeePlan createPlanForStudent(UUID studentId) {
        EnrollmentInfo enrollment = students.currentEnrollment(studentId)
                .orElseThrow(() -> new BusinessRuleException("feePlan.notEnrolled", "The student is not enrolled."));
        return createPlan(enrollment)
                .orElseThrow(() -> new BusinessRuleException("feePlan.noFees", "No fees are set for this level and year."));
    }

    /** Called when a student is enrolled (own transaction, after the enrollment is saved). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onEnrollment(UUID studentId) {
        students.currentEnrollment(studentId)
                .filter(enrollment -> plans.findByEnrollmentId(enrollment.enrollmentId()).isEmpty())
                .ifPresent(this::createPlan);
    }

    private Optional<StudentFeePlan> createPlan(EnrollmentInfo enrollment) {
        if (plans.findByEnrollmentId(enrollment.enrollmentId()).isPresent()) {
            throw new BusinessRuleException("feePlan.exists", "This enrollment already has a fee plan.");
        }
        List<FeeSchedule> fees = schedules.findAllByAcademicYearIdAndStudyLevelIdAndActiveTrue(
                enrollment.academicYearId(), enrollment.studyLevelId());
        if (fees.isEmpty()) {
            return Optional.empty();
        }
        AcademicYearInfo year = parametrage.findAcademicYear(enrollment.academicYearId()).orElseThrow();
        BigDecimal total = fees.stream().map(f -> f.getAmount().amount()).reduce(BigDecimal.ZERO, BigDecimal::add);
        int count = fees.stream().mapToInt(FeeSchedule::getInstallmentsCount).max().orElse(1);

        StudentFeePlan plan = new StudentFeePlan();
        plan.setStudentId(enrollment.studentId());
        plan.setEnrollmentId(enrollment.enrollmentId());
        plan.setTotalAmount(Money.tnd(total));
        plan.setDiscount(Money.tnd(BigDecimal.ZERO));
        plan.setScholarship(Money.tnd(BigDecimal.ZERO));
        plan.setStatus(FeePlanStatus.ACTIVE);
        plan.getBasedOn().addAll(fees);

        BigDecimal share = total.divide(BigDecimal.valueOf(count), 3, RoundingMode.DOWN);
        LocalDate firstDue = year.startDate().withDayOfMonth(15);
        for (int number = 1; number <= count; number++) {
            BigDecimal amount = number < count ? share
                    : total.subtract(share.multiply(BigDecimal.valueOf(count - 1)));
            Installment installment = new Installment();
            installment.setFeePlan(plan);
            installment.setInstallmentNumber(number);
            installment.setDueDate(firstDue.plusMonths(number - 1));
            installment.setAmount(Money.tnd(amount));
            installment.setPaidAmount(Money.tnd(BigDecimal.ZERO));
            installment.setStatus(InstallmentStatus.DUE);
            plan.getInstallments().add(installment);
        }
        return Optional.of(plans.save(plan));
    }
}
