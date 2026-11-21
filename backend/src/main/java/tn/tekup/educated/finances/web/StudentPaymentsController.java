package tn.tekup.educated.finances.web;

import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.tekup.educated.finances.service.FeeService;
import tn.tekup.educated.finances.service.PaymentService;
import tn.tekup.educated.finances.web.FinanceViews.PaymentDto;
import tn.tekup.educated.finances.web.FinanceViews.StudentAccountDto;
import tn.tekup.educated.shared.domain.PersonType;
import tn.tekup.educated.shared.security.CurrentUserProvider;

/** Student portal "Paiements": the student's own fees, installments and receipts. */
@RestController
class StudentPaymentsController {

    private final CurrentUserProvider currentUserProvider;
    private final FeeService fees;
    private final PaymentService payments;
    private final FinanceViews views;

    StudentPaymentsController(CurrentUserProvider currentUserProvider, FeeService fees, PaymentService payments,
            FinanceViews views) {
        this.currentUserProvider = currentUserProvider;
        this.fees = fees;
        this.payments = payments;
        this.views = views;
    }

    @GetMapping("/api/student/payments")
    @Transactional(readOnly = true)
    StudentAccountDto myAccount() {
        UUID studentId = currentUserProvider.requirePerson(PersonType.STUDENT);
        return new StudentAccountDto(fees.plansOf(studentId).stream().map(views::plan).toList(),
                payments.paymentsOf(studentId).stream().map(PaymentDto::from).toList());
    }
}
