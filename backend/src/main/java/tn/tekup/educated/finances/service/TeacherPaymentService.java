package tn.tekup.educated.finances.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.assiduite.api.AttendanceDirectory;
import tn.tekup.educated.assiduite.api.TeacherHoursInfo;
import tn.tekup.educated.finances.domain.TeacherPayment;
import tn.tekup.educated.finances.domain.TeacherPaymentLine;
import tn.tekup.educated.finances.domain.TeacherPaymentLineRepository;
import tn.tekup.educated.finances.domain.TeacherPaymentRepository;
import tn.tekup.educated.finances.domain.TeacherPaymentStatus;
import tn.tekup.educated.pedagogie.api.TeacherInfo;
import tn.tekup.educated.pedagogie.api.TeachingDirectory;
import tn.tekup.educated.shared.domain.Money;
import tn.tekup.educated.shared.security.CurrentUserProvider;
import tn.tekup.educated.shared.web.BusinessRuleException;
import tn.tekup.educated.shared.web.NotFoundException;

/** Paiement des enseignants: validated hours not paid yet, times the teacher's hourly rate. */
@Service
@Transactional
public class TeacherPaymentService {

    private final TeacherPaymentRepository payments;
    private final TeacherPaymentLineRepository lines;
    private final AttendanceDirectory attendance;
    private final TeachingDirectory teaching;
    private final CurrentUserProvider currentUserProvider;

    public TeacherPaymentService(TeacherPaymentRepository payments, TeacherPaymentLineRepository lines,
            AttendanceDirectory attendance, TeachingDirectory teaching, CurrentUserProvider currentUserProvider) {
        this.payments = payments;
        this.lines = lines;
        this.attendance = attendance;
        this.teaching = teaching;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional(readOnly = true)
    public List<TeacherPayment> list(UUID teacherId) {
        return teacherId == null ? payments.findAllByOrderByCreatedAtDesc()
                : payments.findAllByTeacherIdOrderByPeriodFromDesc(teacherId);
    }

    /** Prepares a pending payment of the teacher's validated, unpaid hours between the two dates. */
    public TeacherPayment prepare(UUID teacherId, LocalDate from, LocalDate to) {
        TeacherInfo teacher = teaching.findTeacher(teacherId).orElseThrow(() -> new NotFoundException("Teacher", teacherId));
        if (teacher.hourlyRate() == null || teacher.hourlyRate().amount() == null) {
            throw new BusinessRuleException("teacherPayment.noRate", "The teacher has no hourly rate.");
        }
        List<TeacherHoursInfo> unpaid = attendance.validatedTeacherHours(teacherId, from, to).stream()
                .filter(h -> !lines.existsByTeacherAttendanceIdAndPayment_StatusNot(h.id(), TeacherPaymentStatus.CANCELLED))
                .toList();
        if (unpaid.isEmpty()) {
            throw new BusinessRuleException("teacherPayment.nothingToPay", "No validated hours to pay in this period.");
        }
        BigDecimal rate = teacher.hourlyRate().amount();
        TeacherPayment payment = new TeacherPayment();
        payment.setTeacherId(teacherId);
        payment.setPeriodFrom(from);
        payment.setPeriodTo(to);
        payment.setHourlyRate(Money.tnd(rate));
        payment.setStatus(TeacherPaymentStatus.PENDING);
        BigDecimal hours = BigDecimal.ZERO;
        for (TeacherHoursInfo h : unpaid) {
            TeacherPaymentLine line = new TeacherPaymentLine();
            line.setPayment(payment);
            line.setTeacherAttendanceId(h.id());
            line.setSessionDate(h.date());
            line.setHours(h.hours());
            line.setAmount(Money.tnd(h.hours().multiply(rate)));
            payment.getLines().add(line);
            hours = hours.add(h.hours());
        }
        payment.setTotalHours(hours);
        payment.setTotalAmount(Money.tnd(hours.multiply(rate)));
        return payments.save(payment);
    }

    public TeacherPayment pay(UUID id) {
        TeacherPayment payment = payment(id);
        if (payment.getStatus() != TeacherPaymentStatus.PENDING) {
            throw new BusinessRuleException("teacherPayment.notPending", "Only a pending payment can be paid.");
        }
        payment.setStatus(TeacherPaymentStatus.PAID);
        payment.setPaidAt(Instant.now());
        payment.setPaidByUserId(currentUserProvider.require().userId());
        return payment;
    }

    /** Annuler: the hours become payable again. */
    public TeacherPayment cancel(UUID id) {
        TeacherPayment payment = payment(id);
        if (payment.getStatus() == TeacherPaymentStatus.CANCELLED) {
            throw new BusinessRuleException("teacherPayment.cancelled", "This payment is already cancelled.");
        }
        payment.setStatus(TeacherPaymentStatus.CANCELLED);
        payment.setCancelledAt(Instant.now());
        return payment;
    }

    private TeacherPayment payment(UUID id) {
        return payments.findById(id).orElseThrow(() -> new NotFoundException("TeacherPayment", id));
    }
}
