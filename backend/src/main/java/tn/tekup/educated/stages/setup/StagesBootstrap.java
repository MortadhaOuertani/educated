package tn.tekup.educated.stages.setup;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tn.tekup.educated.shared.domain.LocalizedText;
import tn.tekup.educated.shared.security.EstablishmentDirectory;
import tn.tekup.educated.shared.tenant.TenantContext;
import tn.tekup.educated.stages.domain.InternshipNature;
import tn.tekup.educated.stages.domain.InternshipNatureRepository;

/** The three kinds of internship every establishment starts with. */
@Component
@Order(55)
class StagesBootstrap implements ApplicationRunner {

    private record Nature(String code, String label, boolean requiresDefense, int minWeeks) {
    }

    private static final Nature[] NATURES = {
            new Nature("STAGE-INITIATION", "Stage d'initiation", false, 4),
            new Nature("STAGE-PERFECTIONNEMENT", "Stage de perfectionnement", false, 6),
            new Nature("PFE", "Projet de fin d'études", true, 12)};

    private final EstablishmentDirectory establishments;
    private final InternshipNatureRepository natures;
    private final TransactionTemplate tx;

    StagesBootstrap(EstablishmentDirectory establishments, InternshipNatureRepository natures,
            PlatformTransactionManager transactionManager) {
        this.establishments = establishments;
        this.natures = natures;
        this.tx = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(ApplicationArguments args) {
        for (var establishmentId : establishments.allEstablishmentIds()) {
            TenantContext.runAs(establishmentId, () -> tx.executeWithoutResult(status -> {
                for (Nature n : NATURES) {
                    if (natures.findByCode(n.code()).isEmpty()) {
                        InternshipNature nature = new InternshipNature();
                        nature.setCode(n.code());
                        nature.setLabel(LocalizedText.of(n.label()));
                        nature.setRequiresDefense(n.requiresDefense());
                        nature.setMinWeeks(n.minWeeks());
                        natures.save(nature);
                    }
                }
            }));
        }
    }
}
