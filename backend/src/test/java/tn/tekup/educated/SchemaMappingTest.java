package tn.tekup.educated;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import tn.tekup.educated.parametrage.domain.Site;
import tn.tekup.educated.parametrage.domain.SiteRepository;
import tn.tekup.educated.shared.audit.JpaAuditingConfig;
import tn.tekup.educated.shared.tenant.TenantContext;
import tn.tekup.educated.support.EmbeddedPostgresConfig;

/** Runs every Flyway migration on PostgreSQL and lets Hibernate validate all entities against the tables. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({EmbeddedPostgresConfig.class, JpaAuditingConfig.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class SchemaMappingTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    SiteRepository sites;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Test
    void allEntitiesMatchTheMigrations() {
        // The context only starts if ddl-auto=validate accepts every entity.
        assertThat(entityManager.getMetamodel().getEntities()).hasSizeGreaterThan(40);
    }

    @Test
    void establishmentsCannotSeeEachOthersRows() {
        UUID schoolA = UUID.randomUUID();
        UUID schoolB = UUID.randomUUID();
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        TenantContext.runAs(schoolA, () -> tx.executeWithoutResult(status -> sites.save(site("A-MAIN"))));
        TenantContext.runAs(schoolB, () -> tx.executeWithoutResult(status -> sites.save(site("B-MAIN"))));

        var seenByA = TenantContext.callAs(schoolA, () -> tx.execute(status -> sites.findAll()));
        var seenByB = TenantContext.callAs(schoolB, () -> tx.execute(status -> sites.findAll()));
        var seenWithoutTenant = TenantContext.callAs(TenantContext.NONE, () -> tx.execute(status -> sites.findAll()));

        assertThat(seenByA).extracting(Site::getCode).containsExactly("A-MAIN");
        assertThat(seenByA.getFirst().getEstablishmentId()).isEqualTo(schoolA);
        assertThat(seenByB).extracting(Site::getCode).containsExactly("B-MAIN");
        assertThat(seenWithoutTenant).isEmpty();
    }

    private static Site site(String code) {
        Site site = new Site();
        site.setCode(code);
        site.setName("Campus " + code);
        return site;
    }
}
