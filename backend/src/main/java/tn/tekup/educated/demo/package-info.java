/** Development-only demo accounts (enabled by educated.bootstrap.demo-data). Nothing depends on this module. */
@ApplicationModule(displayName = "Demo data", allowedDependencies = {"shared", "administration::api", "scolarite::api", "pedagogie::api"})
package tn.tekup.educated.demo;

import org.springframework.modulith.ApplicationModule;
