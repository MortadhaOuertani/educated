package tn.tekup.educated;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/** Fails when code breaks the module boundaries or creates a dependency cycle between modules. */
class ModularityTest {

    private final ApplicationModules modules = ApplicationModules.of(EducatedBackendApplication.class);

    @Test
    void modulesRespectTheArchitecture() {
        modules.verify();
    }

    @Test
    void writeModuleDiagrams() {
        new Documenter(modules).writeModulesAsPlantUml().writeIndividualModulesAsPlantUml();
    }
}
