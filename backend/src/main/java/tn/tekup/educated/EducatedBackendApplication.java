package tn.tekup.educated;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Educated platform backend: a Spring Modulith modular monolith. Each sub-package is one module
 * (shared, parametrage, administration, scolarite, ...) with its own database schema.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class EducatedBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EducatedBackendApplication.class, args);
    }
}
