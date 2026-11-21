package tn.tekup.educated.vieetablissement.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tn.tekup.educated.shared.domain.Audience;
import tn.tekup.educated.shared.domain.PersonType;
import tn.tekup.educated.vieetablissement.domain.AccessDirection;
import tn.tekup.educated.vieetablissement.domain.AccessMethod;
import tn.tekup.educated.vieetablissement.domain.EntryTicketReason;
import tn.tekup.educated.vieetablissement.domain.MealType;
import tn.tekup.educated.vieetablissement.domain.SurveyPurpose;
import tn.tekup.educated.vieetablissement.service.CampusService;
import tn.tekup.educated.vieetablissement.service.SurveyService;
import tn.tekup.educated.vieetablissement.service.SurveyService.QuestionData;
import tn.tekup.educated.vieetablissement.service.SurveyService.SurveyData;
import tn.tekup.educated.vieetablissement.web.CampusViews.AccessEventDto;
import tn.tekup.educated.vieetablissement.web.CampusViews.AccessPointDto;
import tn.tekup.educated.vieetablissement.web.CampusViews.MenuDto;
import tn.tekup.educated.vieetablissement.web.CampusViews.SurveyDto;
import tn.tekup.educated.vieetablissement.web.CampusViews.TicketDto;
import tn.tekup.educated.vieetablissement.web.CampusViews.VisitDto;

/** Back-office screens of the Vie de l'établissement module. */
@RestController
@RequestMapping("/api/vie-etablissement")
class VieEtablissementController {

    record SurveyRequest(@NotBlank String title, String description, SurveyPurpose purpose, @NotNull Audience audience,
            UUID teachingAssignmentId, Instant opensAt, Instant closesAt, boolean anonymous,
            @NotEmpty List<@Valid QuestionData> questions) {
    }

    record MenuRequest(@NotNull LocalDate menuDate, @NotNull MealType mealType, String items, BigDecimal price) {
    }

    record VisitRequest(@NotNull UUID studentId, Instant visitedAt, @NotBlank String reason, String careGiven,
            boolean guardiansNotified, String followUp) {
    }

    record TicketRequest(@NotNull UUID studentId, @NotNull EntryTicketReason reason, LocalDate validForDate,
            String note, UUID attendanceRecordId) {
    }

    record AccessPointRequest(@NotBlank String code, @NotBlank String name, UUID siteId) {
    }

    record AccessEventRequest(@NotNull UUID accessPointId, @NotNull PersonType personType, @NotNull UUID personId,
            @NotNull AccessDirection direction, @NotNull AccessMethod method, boolean granted) {
    }

    private final SurveyService surveys;
    private final CampusService campus;

    VieEtablissementController(SurveyService surveys, CampusService campus) {
        this.surveys = surveys;
        this.campus = campus;
    }

    @GetMapping("/surveys")
    @PreAuthorize("@perm.can('Survey', 'READ')")
    @Transactional(readOnly = true)
    List<SurveyDto> surveys() {
        return surveys.all().stream().map(s -> SurveyDto.from(s, surveys.responseCount(s.getId()), null)).toList();
    }

    @PostMapping("/surveys")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('Survey', 'CREATE')")
    @Transactional
    SurveyDto createSurvey(@Valid @RequestBody SurveyRequest r) {
        return SurveyDto.from(surveys.create(new SurveyData(r.title(), r.description(), r.purpose(), r.audience(),
                r.teachingAssignmentId(), r.opensAt(), r.closesAt(), r.anonymous(), r.questions())), 0L, null);
    }

    @GetMapping("/canteen-menus")
    @PreAuthorize("@perm.can('CanteenMenu', 'READ')")
    List<MenuDto> menus(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return campus.menus(from, to).stream().map(m -> MenuDto.from(m, campus.reservedCount(m.getId()))).toList();
    }

    @PostMapping("/canteen-menus")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('CanteenMenu', 'CREATE')")
    MenuDto createMenu(@Valid @RequestBody MenuRequest r) {
        return MenuDto.from(campus.createMenu(r.menuDate(), r.mealType(), r.items(), r.price()), 0L);
    }

    @GetMapping("/infirmary-visits")
    @PreAuthorize("@perm.can('InfirmaryVisit', 'READ')")
    List<VisitDto> visits(@RequestParam(required = false) UUID studentId) {
        return campus.visits(studentId).stream().map(VisitDto::from).toList();
    }

    @PostMapping("/infirmary-visits")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('InfirmaryVisit', 'CREATE')")
    VisitDto recordVisit(@Valid @RequestBody VisitRequest r) {
        return VisitDto.from(campus.recordVisit(r.studentId(), r.visitedAt(), r.reason(), r.careGiven(),
                r.guardiansNotified(), r.followUp()));
    }

    @GetMapping("/entry-tickets")
    @PreAuthorize("@perm.can('EntryTicket', 'READ')")
    List<TicketDto> tickets(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return campus.ticketsOn(date).stream().map(TicketDto::from).toList();
    }

    @PostMapping("/entry-tickets")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('EntryTicket', 'CREATE')")
    TicketDto issueTicket(@Valid @RequestBody TicketRequest r) {
        return TicketDto.from(campus.issueTicket(r.studentId(), r.reason(), r.validForDate(), r.note(),
                r.attendanceRecordId()));
    }

    @GetMapping("/access-points")
    @PreAuthorize("@perm.can('AccessPoint', 'READ')")
    List<AccessPointDto> accessPoints() {
        return campus.accessPoints().stream().map(AccessPointDto::from).toList();
    }

    @PostMapping("/access-points")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('AccessPoint', 'CREATE')")
    AccessPointDto createAccessPoint(@Valid @RequestBody AccessPointRequest r) {
        return AccessPointDto.from(campus.createAccessPoint(r.code(), r.name(), r.siteId()));
    }

    @GetMapping("/access-events")
    @PreAuthorize("@perm.can('AccessEvent', 'READ')")
    @Transactional(readOnly = true)
    List<AccessEventDto> accessEvents(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return campus.accessEventsOn(date).stream().map(AccessEventDto::from).toList();
    }

    @PostMapping("/access-events")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('AccessEvent', 'CREATE')")
    @Transactional
    AccessEventDto recordAccess(@Valid @RequestBody AccessEventRequest r) {
        return AccessEventDto.from(campus.recordAccess(r.accessPointId(), r.personType(), r.personId(), r.direction(),
                r.method(), r.granted()));
    }
}
