package tn.tekup.educated.emploidutemps.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import tn.tekup.educated.pedagogie.api.MakeUpConfirmed;
import tn.tekup.educated.shared.tenant.TenantContext;

/** Schedules the make-up session once the pedagogie module has saved the confirmation. */
@Component
class MakeUpListener {

    private final TimetableService timetables;

    MakeUpListener(TimetableService timetables) {
        this.timetables = timetables;
    }

    @TransactionalEventListener
    void on(MakeUpConfirmed event) {
        TenantContext.runAs(event.establishmentId(), () -> timetables.scheduleMakeUp(event));
    }
}
