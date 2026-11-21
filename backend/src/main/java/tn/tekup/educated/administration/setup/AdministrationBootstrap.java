package tn.tekup.educated.administration.setup;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tn.tekup.educated.shared.security.EstablishmentDirectory;
import tn.tekup.educated.shared.tenant.TenantContext;

/** Makes sure every establishment has the system roles. Runs after the establishments are created. */
@Component
@Order(20)
class AdministrationBootstrap implements ApplicationRunner {

    private final EstablishmentDirectory establishments;
    private final SystemRoles systemRoles;

    AdministrationBootstrap(EstablishmentDirectory establishments, SystemRoles systemRoles) {
        this.establishments = establishments;
        this.systemRoles = systemRoles;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (var establishmentId : establishments.allEstablishmentIds()) {
            TenantContext.runAs(establishmentId, systemRoles::ensureExist);
        }
    }
}
