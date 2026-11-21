package tn.tekup.educated.finances.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.finances.api.PaymentReceived;
import tn.tekup.educated.finances.api.PaymentReminderSent;
import tn.tekup.educated.finances.domain.Channel;
import tn.tekup.educated.finances.domain.FeePlanStatus;
import tn.tekup.educated.finances.domain.Installment;
import tn.tekup.educated.finances.domain.InstallmentRepository;
import tn.tekup.educated.finances.domain.InstallmentStatus;
import tn.tekup.educated.finances.domain.Payment;
import tn.tekup.educated.finances.domain.PaymentAllocation;
import tn.tekup.educated.finances.domain.PaymentMethod;
import tn.tekup.educated.finances.domain.PaymentReminder;
import tn.tekup.educated.finances.domain.PaymentRepository;
import tn.tekup.educated.finances.domain.StudentFeePlan;
import tn.tekup.educated.finances.domain.StudentFeePlanRepository;
import tn.tekup.educated.shared.domain.Money;
import tn.tekup.educated.shared.web.BusinessRuleException;
import tn.tekup.educated.shared.web.NotFoundException;
import tn.tekup.educated.shared.tenant.TenantContext;

/** Payments of students: each payment pays the oldest unpaid installments first and gets a receipt number. */
@Service
@Transactional
public class PaymentService {

    private final PaymentRepository payments;
    private final StudentFeePlanRepository plans;
    private final InstallmentRepository installments;
    private final ApplicationEventPublisher events;

    public PaymentService(PaymentRepository payments, StudentFeePlanRepository plans,
            InstallmentRepository installments, ApplicationEventPublisher events) {
        this.payments = payments;
        this.plans = plans;
        this.installments = installments;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<Payment> paymentsOf(UUID studentId) {
        return payments.findAllByStudentIdOrderByPaidAtDesc(studentId);
    }

    public Payment record(UUID studentId, BigDecimal amount, PaymentMethod method, String reference) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessRuleException("payment.amount", "The amount must be positive.");
        }
        StudentFeePlan plan = plans.findAllByStudentIdAndStatus(studentId, FeePlanStatus.ACTIVE).stream().findFirst()
                .orElseThrow(() -> new BusinessRuleException("payment.noPlan", "The student has no fees to pay."));
        List<Installment> unpaid = plan.getInstallments().stream()
                .filter(i -> i.getStatus() != InstallmentStatus.PAID)
                .sorted(Comparator.comparing(Installment::getInstallmentNumber))
                .toList();
        BigDecimal due = unpaid.stream().map(PaymentService::remaining).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (amount.compareTo(due) > 0) {
            throw new BusinessRuleException("payment.tooMuch", "The amount is more than the " + due + " TND still due.");
        }

        Instant now = Instant.now();
        Payment payment = new Payment();
        payment.setStudentId(studentId);
        payment.setAmount(Money.tnd(amount));
        payment.setPaidAt(now);
        payment.setMethod(method);
        payment.setReference(reference);
        payment.setReceiptNumber("REC-" + LocalDate.ofInstant(now, ZoneId.systemDefault()).getYear() + "-"
                + String.format("%06d", payments.count() + 1));

        BigDecimal left = amount;
        for (Installment installment : unpaid) {
            if (left.signum() == 0) {
                break;
            }
            BigDecimal part = left.min(remaining(installment));
            installment.setPaidAmount(installment.getPaidAmount().plus(Money.tnd(part)));
            installment.setStatus(remaining(installment).signum() == 0 ? InstallmentStatus.PAID
                    : InstallmentStatus.PARTIALLY_PAID);
            PaymentAllocation allocation = new PaymentAllocation();
            allocation.setPayment(payment);
            allocation.setInstallment(installment);
            allocation.setAmount(Money.tnd(part));
            payment.getAllocations().add(allocation);
            left = left.subtract(part);
        }
        if (plan.getInstallments().stream().allMatch(i -> i.getStatus() == InstallmentStatus.PAID)) {
            plan.setStatus(FeePlanStatus.SETTLED);
        }
        payment = payments.save(payment);
        events.publishEvent(new PaymentReceived(TenantContext.requireEstablishment(), payment.getId(), studentId, amount,
                payment.getReceiptNumber()));
        return payment;
    }

    /** Installments past their due date and not fully paid; they are marked OVERDUE. */
    public List<Installment> overdue() {
        List<Installment> late = installments.findAllByDueDateBeforeAndStatusNotOrderByDueDate(LocalDate.now(),
                InstallmentStatus.PAID);
        late.forEach(i -> i.setStatus(InstallmentStatus.OVERDUE));
        return late;
    }

    /** Relance: records the reminder and asks the communication module to send it. */
    public PaymentReminder remind(UUID installmentId, Channel channel, String message) {
        Installment installment = installments.findById(installmentId)
                .orElseThrow(() -> new NotFoundException("Installment", installmentId));
        if (installment.getStatus() == InstallmentStatus.PAID) {
            throw new BusinessRuleException("reminder.paid", "This installment is already paid.");
        }
        PaymentReminder reminder = new PaymentReminder();
        reminder.setInstallment(installment);
        reminder.setReminderLevel(installment.getReminders().size() + 1);
        reminder.setChannel(channel);
        reminder.setMessage(message);
        reminder.setSentAt(Instant.now());
        installment.getReminders().add(reminder);
        events.publishEvent(new PaymentReminderSent(installment.getEstablishmentId(),
                installment.getFeePlan().getStudentId(), installment.getId(), installment.getDueDate(),
                remaining(installment), channel.name(), message));
        return reminder;
    }

    static BigDecimal remaining(Installment installment) {
        return installment.getAmount().amount().subtract(installment.getPaidAmount().amount());
    }
}
