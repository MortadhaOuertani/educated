package tn.tekup.educated.finances.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tn.tekup.educated.finances.domain.Channel;
import tn.tekup.educated.finances.domain.FeeType;
import tn.tekup.educated.finances.domain.PaymentMethod;
import tn.tekup.educated.finances.service.FeeService;
import tn.tekup.educated.finances.service.PaymentService;
import tn.tekup.educated.finances.service.TeacherPaymentService;
import tn.tekup.educated.finances.web.FinanceViews.FeePlanDto;
import tn.tekup.educated.finances.web.FinanceViews.FeeScheduleDto;
import tn.tekup.educated.finances.web.FinanceViews.InstallmentDto;
import tn.tekup.educated.finances.web.FinanceViews.PaymentDto;
import tn.tekup.educated.finances.web.FinanceViews.ReminderDto;
import tn.tekup.educated.finances.web.FinanceViews.StudentAccountDto;
import tn.tekup.educated.finances.web.FinanceViews.TeacherPaymentDto;

/**
 * Back-office screens of the Finances module. Paying and cancelling teacher payments also check the role's denied
 * actions ("Payer", "Annuler paiements"), so a role can see payments without being allowed to pay.
 */
@RestController
@RequestMapping("/api/finances")
class FinancesController {

    record FeeScheduleRequest(@NotNull UUID academicYearId, @NotNull UUID studyLevelId, @NotNull FeeType feeType,
            @NotNull BigDecimal amount, @NotNull Integer installmentsCount) {
    }

    record PaymentRequest(@NotNull UUID studentId, @NotNull BigDecimal amount, @NotNull PaymentMethod method,
            String reference) {
    }

    record ReminderRequest(@NotNull Channel channel, String message) {
    }

    record TeacherPaymentRequest(@NotNull UUID teacherId, @NotNull LocalDate from, @NotNull LocalDate to) {
    }

    private final FeeService fees;
    private final PaymentService payments;
    private final TeacherPaymentService teacherPayments;
    private final FinanceViews views;

    FinancesController(FeeService fees, PaymentService payments, TeacherPaymentService teacherPayments,
            FinanceViews views) {
        this.fees = fees;
        this.payments = payments;
        this.teacherPayments = teacherPayments;
        this.views = views;
    }

    @GetMapping("/fee-schedules")
    @PreAuthorize("@perm.can('FeeSchedule', 'READ')")
    List<FeeScheduleDto> feeSchedules(@RequestParam UUID academicYearId) {
        return fees.schedulesOf(academicYearId).stream().map(FeeScheduleDto::from).toList();
    }

    @PostMapping("/fee-schedules")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('FeeSchedule', 'CREATE')")
    FeeScheduleDto createFeeSchedule(@Valid @RequestBody FeeScheduleRequest r) {
        return FeeScheduleDto.from(fees.createSchedule(r.academicYearId(), r.studyLevelId(), r.feeType(), r.amount(),
                r.installmentsCount()));
    }

    /** Fees and payments of one student. */
    @GetMapping("/students/{id}/account")
    @PreAuthorize("@perm.can('StudentFeePlan', 'READ')")
    @Transactional(readOnly = true)
    StudentAccountDto account(@PathVariable UUID id) {
        return new StudentAccountDto(fees.plansOf(id).stream().map(views::plan).toList(),
                payments.paymentsOf(id).stream().map(PaymentDto::from).toList());
    }

    @PostMapping("/students/{id}/fee-plan")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('StudentFeePlan', 'CREATE')")
    @Transactional
    FeePlanDto createFeePlan(@PathVariable UUID id) {
        return views.plan(fees.createPlanForStudent(id));
    }

    @PostMapping("/payments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('Payment', 'CREATE')")
    @Transactional
    PaymentDto recordPayment(@Valid @RequestBody PaymentRequest r) {
        return PaymentDto.from(payments.record(r.studentId(), r.amount(), r.method(), r.reference()));
    }

    @GetMapping("/installments/overdue")
    @PreAuthorize("@perm.can('Installment', 'READ')")
    @Transactional
    List<InstallmentDto> overdue() {
        return payments.overdue().stream().map(i -> views.installment(i, true)).toList();
    }

    @PostMapping("/installments/{id}/reminders")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('PaymentReminder', 'CREATE')")
    @Transactional
    ReminderDto remind(@PathVariable UUID id, @Valid @RequestBody ReminderRequest r) {
        return ReminderDto.from(payments.remind(id, r.channel(), r.message()));
    }

    @GetMapping("/teacher-payments")
    @PreAuthorize("@perm.can('TeacherPayment', 'READ')")
    @Transactional(readOnly = true)
    List<TeacherPaymentDto> teacherPayments(@RequestParam(required = false) UUID teacherId) {
        return teacherPayments.list(teacherId).stream().map(views::teacherPayment).toList();
    }

    @PostMapping("/teacher-payments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('TeacherPayment', 'CREATE')")
    @Transactional
    TeacherPaymentDto prepareTeacherPayment(@Valid @RequestBody TeacherPaymentRequest r) {
        return views.teacherPayment(teacherPayments.prepare(r.teacherId(), r.from(), r.to()));
    }

    @PostMapping("/teacher-payments/{id}/pay")
    @PreAuthorize("@perm.can('TeacherPayment', 'WRITE') and @perm.canRunAction('Payer')")
    @Transactional
    TeacherPaymentDto pay(@PathVariable UUID id) {
        return views.teacherPayment(teacherPayments.pay(id));
    }

    @PostMapping("/teacher-payments/{id}/cancel")
    @PreAuthorize("@perm.can('TeacherPayment', 'WRITE') and @perm.canRunAction('Annuler paiements')")
    @Transactional
    TeacherPaymentDto cancel(@PathVariable UUID id) {
        return views.teacherPayment(teacherPayments.cancel(id));
    }
}
