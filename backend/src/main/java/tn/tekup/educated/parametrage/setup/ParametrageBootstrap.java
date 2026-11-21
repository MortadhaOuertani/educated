package tn.tekup.educated.parametrage.setup;

import java.time.LocalDate;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tn.tekup.educated.parametrage.domain.Building;
import tn.tekup.educated.parametrage.domain.BuildingRepository;
import tn.tekup.educated.parametrage.domain.Establishment;
import tn.tekup.educated.parametrage.domain.EstablishmentRepository;
import tn.tekup.educated.parametrage.domain.FeatureToggle;
import tn.tekup.educated.parametrage.domain.FeatureToggleRepository;
import tn.tekup.educated.parametrage.domain.Portal;
import tn.tekup.educated.parametrage.domain.Room;
import tn.tekup.educated.parametrage.domain.RoomRepository;
import tn.tekup.educated.parametrage.domain.RoomType;
import tn.tekup.educated.parametrage.domain.Site;
import tn.tekup.educated.parametrage.domain.SiteRepository;
import tn.tekup.educated.parametrage.domain.StudyRhythm;
import tn.tekup.educated.parametrage.domain.AcademicYearRepository;
import tn.tekup.educated.parametrage.service.AcademicYearService;
import tn.tekup.educated.parametrage.service.StructureService;
import tn.tekup.educated.shared.EducatedProperties;
import tn.tekup.educated.shared.domain.LocalizedText;
import tn.tekup.educated.shared.tenant.TenantContext;

/** Creates the default establishment on first start and, in development, a small demo structure. */
@Component
@Order(10)
class ParametrageBootstrap implements ApplicationRunner {

    private final EducatedProperties.Bootstrap settings;
    private final EstablishmentRepository establishments;
    private final AcademicYearRepository academicYears;
    private final AcademicYearService academicYearService;
    private final StructureService structureService;
    private final SiteRepository sites;
    private final BuildingRepository buildings;
    private final RoomRepository rooms;
    private final FeatureToggleRepository featureToggles;
    private final TransactionTemplate tx;

    ParametrageBootstrap(EducatedProperties properties, EstablishmentRepository establishments,
            AcademicYearRepository academicYears, AcademicYearService academicYearService,
            StructureService structureService, SiteRepository sites, BuildingRepository buildings, RoomRepository rooms,
            FeatureToggleRepository featureToggles, PlatformTransactionManager transactionManager) {
        this.settings = properties.bootstrap();
        this.establishments = establishments;
        this.academicYears = academicYears;
        this.academicYearService = academicYearService;
        this.structureService = structureService;
        this.sites = sites;
        this.buildings = buildings;
        this.rooms = rooms;
        this.featureToggles = featureToggles;
        this.tx = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(ApplicationArguments args) {
        Establishment establishment = TenantContext.callAsRoot(() -> tx.execute(status ->
                establishments.findByCode(settings.establishmentCode()).orElseGet(this::createEstablishment)));
        if (settings.demoData()) {
            TenantContext.runAs(establishment.getId(), () -> tx.executeWithoutResult(status -> createDemoStructure()));
        }
    }

    private Establishment createEstablishment() {
        Establishment establishment = new Establishment();
        establishment.setCode(settings.establishmentCode());
        establishment.setName(LocalizedText.of(settings.establishmentName()));
        establishment.setTimezone("Africa/Tunis");
        establishment.setDefaultLocale("fr");
        establishment.setActive(true);
        return establishments.save(establishment);
    }

    private void createDemoStructure() {
        if (academicYears.count() > 0) {
            return;
        }
        var year = academicYearService.create("2026-2027", LocalDate.of(2026, 9, 1), LocalDate.of(2027, 7, 31), List.of(
                new AcademicYearService.NewPeriod("S1", "Semestre 1", LocalDate.of(2026, 9, 1), LocalDate.of(2027, 1, 31)),
                new AcademicYearService.NewPeriod("S2", "Semestre 2", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 7, 31))));
        academicYearService.open(year.getId());

        var engineering = structureService.createCycle("ING", "Cycle d'Ingénieur", 3);
        var computing = structureService.createProgram("INFO", "Informatique", "Diplôme d'ingénieur en informatique");
        var glsi = structureService.createSpecialization("GLSI", "Génie Logiciel et Système d'Information",
                computing.getId());
        var level = structureService.createStudyLevel("ING-5-S-GLSI",
                "Troisième année du cycle d'ingénieur informatique (GLSI)", 5, StudyRhythm.EVENING, computing.getId(),
                engineering.getId(), glsi.getId());

        Site campus = new Site();
        campus.setCode("MAIN");
        campus.setName("Campus principal");
        campus = sites.save(campus);
        Building building = new Building();
        building.setCode("B");
        building.setName("Bloc B");
        building.setSite(campus);
        building = buildings.save(building);
        Room room = new Room();
        room.setCode("B-213");
        room.setName("Salle B-213");
        room.setCapacity(40);
        room.setRoomType(RoomType.CLASSROOM);
        room.setActive(true);
        room.setBuilding(building);
        rooms.save(room);

        structureService.createSchoolClass("ING-5-S-GLSI-B", "ING-5-S-GLSI groupe B", 40, level.getId(), year.getId(),
                campus.getId());

        FeatureToggle payments = new FeatureToggle();
        payments.setFeatureKey("student.payments");
        payments.setPortal(Portal.STUDENT);
        payments.setEnabled(false);
        payments.setDisabledMessage("Cette rubrique est actuellement désactivée.");
        featureToggles.save(payments);
    }
}
