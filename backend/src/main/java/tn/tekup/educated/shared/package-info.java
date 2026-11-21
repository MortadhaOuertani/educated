/**
 * Shared kernel: base entities, value objects, tenant handling, security contracts, auditing,
 * file storage and web error handling. Every other module may depend on it; it depends on none.
 */
@ApplicationModule(displayName = "Shared kernel", type = ApplicationModule.Type.OPEN)
package tn.tekup.educated.shared;

import org.springframework.modulith.ApplicationModule;
