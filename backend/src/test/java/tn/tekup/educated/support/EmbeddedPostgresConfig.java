package tn.tekup.educated.support;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.io.IOException;
import java.io.UncheckedIOException;
import javax.sql.DataSource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/** A real PostgreSQL 18 started once for the whole test run (no Docker needed). */
@TestConfiguration(proxyBeanMethods = false)
public class EmbeddedPostgresConfig {

    private static EmbeddedPostgres postgres;

    @Bean
    DataSource dataSource() {
        return server().getPostgresDatabase();
    }

    private static synchronized EmbeddedPostgres server() {
        if (postgres == null) {
            try {
                postgres = EmbeddedPostgres.builder().start();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return postgres;
    }
}
