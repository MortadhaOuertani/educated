package tn.tekup.educated.finances.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import tn.tekup.educated.finances.domain.FeeSchedule;
import tn.tekup.educated.finances.domain.Installment;
import tn.tekup.educated.finances.domain.Payment;
import tn.tekup.educated.finances.domain.PaymentReminder;
import tn.tekup.educated.finances.domain.StudentFeePlan;
import tn.tekup.educated.finances.domain.TeacherPayment;
import tn.tekup.educated.pedagogie.api.TeacherInfo;
import tn.tekup.educated.pedagogie.api.TeachingDirectory;
import tn.tekup.educated.scolarite.api.StudentDirectory;
import tn.tekup.educated.scolarite.api.StudentInfo;

/** Builds the JSON views of the finances module. Amounts are in TND with 3 decimals. */
@Component
class FinanceViews {

    record FeeScheduleDto(UUID id, UUID academicYearId, UUID studyLevelId, String feeType, BigDecimal amount,
            Integer installmentsCount, boolean active) {
        static FeeScheduleDto from(FeeSchedule f) {
            return new FeeScheduleDto(f.getId(), f.getAcademicYearId(), f.getStudyLevelId(), f.getFeeType().name(),
                    f.getAmount().amount(), f.getInstallmentsCount(), f.isActive());
        }
    }

    record InstallmentDto(UUID id, Integer number, LocalDate dueDate, BigDecimal amount, BigDecimal paidAmount,
            String status, String matricule, String studentName) {
    }

    record FeePlanDto(UUID id, UUID studentId, UUID enrollmentId, BigDecimal totalAmount, BigDecimal paidAmount,
            String status, List<InstallmentDto> installments) {
    }

    record PaymentDto(UUID id, UUID studentId, BigDecimal amount, Instant paidAt, String method, String reference,
            String receiptNumber) {
        static PaymentDto from(Payment p) {
            return new PaymentDto(p.getId(), p.getStudentId(), p.getAmount().amount(), p.getPaidAt(),
                    p.getMethod().name(), p.getReference(), p.getReceiptNumber());
        }
    }

    record ReminderDto(UUID id, UUID installmentId, Integer level, String channel, String message, Instant sentAt) {
        static ReminderDto from(PaymentReminder r) {
            return new ReminderDto(r.getId(), r.getInstallment().getId(), r.getReminderLevel(), r.getChannel().name(),
                    r.getMessage(), r.getSentAt());
        }
    }

    record StudentAccountDto(List<FeePlanDto> plans, List<PaymentDto> payments) {
    }

    record TeacherPaymentLineDto(UUID teacherAttendanceId, LocalDate sessionDate, BigDecimal hours, BigDecimal amount) {
    }

    record TeacherPaymentDto(UUID id, UUID teacherId, String teacherName, LocalDate periodFrom, LocalDate periodTo,
            BigDecimal totalHours, BigDecimal hourlyRate, BigDecimal totalAmount, String status, Instant paidAt,
            List<TeacherPaymentLineDto> lines) {
    }

    private final StudentDirectory students;
    private final TeachingDirectory teaching;

    FinanceViews(StudentDirectory students, TeachingDirectory teaching) {
        this.students = students;
        this.teaching = teaching;
    }

    FeePlanDto plan(StudentFeePlan p) {
        List<InstallmentDto> installments = p.getInstallments().stream()
                .sorted(Comparator.comparing(Installment::getInstallmentNumber))
                .map(i -> installment(i, false))
                .toList();
        BigDecimal paid = p.getInstallments().stream().map(i -> i.getPaidAmount().amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new FeePlanDto(p.getId(), p.getStudentId(), p.getEnrollmentId(), p.getTotalAmount().amount(), paid,
                p.getStatus().name(), installments);
    }

    InstallmentDto installment(Installment i, boolean withStudent) {
        Optional<StudentInfo> student = withStudent ? students.findStudent(i.getFeePlan().getStudentId())
                : Optional.empty();
        return new InstallmentDto(i.getId(), i.getInstallmentNumber(), i.getDueDate(), i.getAmount().amount(),
                i.getPaidAmount().amount(), i.getStatus().name(), student.map(StudentInfo::matricule).orElse(null),
                student.map(StudentInfo::fullName).orElse(null));
    }

    TeacherPaymentDto teacherPayment(TeacherPayment p) {
        return new TeacherPaymentDto(p.getId(), p.getTeacherId(),
                teaching.findTeacher(p.getTeacherId()).map(TeacherInfo::fullName).orElse(null), p.getPeriodFrom(),
                p.getPeriodTo(), p.getTotalHours(), p.getHourlyRate().amount(), p.getTotalAmount().amount(),
                p.getStatus().name(), p.getPaidAt(),
                p.getLines().stream().map(l -> new TeacherPaymentLineDto(l.getTeacherAttendanceId(), l.getSessionDate(),
                        l.getHours(), l.getAmount().amount())).toList());
    }
}
