package tn.tekup.educated.finances.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import tn.tekup.educated.scolarite.api.StudentEnrolled;
import tn.tekup.educated.shared.tenant.TenantContext;

/** Creates the fee plan of a newly enrolled student, once the enrollment is saved. */
@Component
class EnrollmentListener {

    private final FeeService fees;

    EnrollmentListener(FeeService fees) {
        this.fees = fees;
    }

    @TransactionalEventListener
    void on(StudentEnrolled event) {
        TenantContext.runAs(event.establishmentId(), () -> fees.onEnrollment(event.studentId()));
    }
}
