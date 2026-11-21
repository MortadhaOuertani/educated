package tn.tekup.educated.examens.setup;

import java.math.BigDecimal;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tn.tekup.educated.examens.domain.AppreciationScale;
import tn.tekup.educated.examens.domain.AppreciationScaleRepository;
import tn.tekup.educated.shared.domain.LocalizedText;
import tn.tekup.educated.shared.security.EstablishmentDirectory;
import tn.tekup.educated.shared.tenant.TenantContext;

/** The appreciation printed on report cards, by general average. */
@Component
@Order(60)
class ExamensBootstrap implements ApplicationRunner {

    private static final String[][] SCALE = {
            {"0", "9.99", "Insuffisant"}, {"10", "11.99", "Passable"}, {"12", "13.99", "Assez bien"},
            {"14", "15.99", "Bien"}, {"16", "17.99", "Très bien"}, {"18", "20", "Excellent"}};

    private final EstablishmentDirectory establishments;
    private final AppreciationScaleRepository scales;
    private final TransactionTemplate tx;

    ExamensBootstrap(EstablishmentDirectory establishments, AppreciationScaleRepository scales,
            PlatformTransactionManager transactionManager) {
        this.establishments = establishments;
        this.scales = scales;
        this.tx = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(ApplicationArguments args) {
        for (var establishmentId : establishments.allEstablishmentIds()) {
            TenantContext.runAs(establishmentId, () -> tx.executeWithoutResult(status -> {
                if (scales.count() > 0) {
                    return;
                }
                for (String[] row : SCALE) {
                    AppreciationScale scale = new AppreciationScale();
                    scale.setMinAverage(new BigDecimal(row[0]));
                    scale.setMaxAverage(new BigDecimal(row[1]));
                    scale.setLabel(LocalizedText.of(row[2]));
                    scales.save(scale);
                }
            }));
        }
    }
}
