package tn.tekup.educated.parametrage.setup;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.parametrage.domain.Establishment;
import tn.tekup.educated.parametrage.domain.EstablishmentRepository;
import tn.tekup.educated.shared.security.EstablishmentDirectory;

@Component
@Transactional(readOnly = true)
class EstablishmentDirectoryImpl implements EstablishmentDirectory {

    private final EstablishmentRepository establishments;

    EstablishmentDirectoryImpl(EstablishmentRepository establishments) {
        this.establishments = establishments;
    }

    @Override
    public List<UUID> allEstablishmentIds() {
        return establishments.findAllByActiveTrueOrderByCode().stream().map(Establishment::getId).toList();
    }

    @Override
    public boolean exists(UUID establishmentId) {
        return establishmentId != null && establishments.existsById(establishmentId);
    }
}
