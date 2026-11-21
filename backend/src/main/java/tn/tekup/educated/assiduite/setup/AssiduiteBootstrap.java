package tn.tekup.educated.assiduite.setup;

import java.math.BigDecimal;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tn.tekup.educated.assiduite.domain.AttendancePolicy;
import tn.tekup.educated.assiduite.domain.AttendancePolicyRepository;
import tn.tekup.educated.shared.security.EstablishmentDirectory;
import tn.tekup.educated.shared.tenant.TenantContext;

/** The default attendance policy: at risk from 10 % of absences, critical from 25 %, calls editable for 48 h. */
@Component
@Order(50)
class AssiduiteBootstrap implements ApplicationRunner {

    private final EstablishmentDirectory establishments;
    private final AttendancePolicyRepository policies;
    private final TransactionTemplate tx;

    AssiduiteBootstrap(EstablishmentDirectory establishments, AttendancePolicyRepository policies,
            PlatformTransactionManager transactionManager) {
        this.establishments = establishments;
        this.policies = policies;
        this.tx = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(ApplicationArguments args) {
        for (var establishmentId : establishments.allEstablishmentIds()) {
            TenantContext.runAs(establishmentId, () -> tx.executeWithoutResult(status -> {
                if (policies.findFirstByStudyLevelIdIsNull().isEmpty()) {
                    AttendancePolicy policy = new AttendancePolicy();
                    policy.setAtRiskPercent(new BigDecimal("10"));
                    policy.setCriticalPercent(new BigDecimal("25"));
                    policy.setCallDeadlineMinutes(15);
                    policy.setEditWindowHours(48);
                    policy.setCountOnlyConfirmedCalls(true);
                    policies.save(policy);
                }
            }));
        }
    }
}
