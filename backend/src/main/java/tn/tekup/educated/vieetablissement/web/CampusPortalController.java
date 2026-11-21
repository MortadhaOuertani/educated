package tn.tekup.educated.vieetablissement.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tn.tekup.educated.shared.domain.PersonType;
import tn.tekup.educated.shared.security.CurrentUser;
import tn.tekup.educated.shared.security.CurrentUserProvider;
import tn.tekup.educated.vieetablissement.service.CampusService;
import tn.tekup.educated.vieetablissement.service.SurveyService;
import tn.tekup.educated.vieetablissement.web.CampusViews.ReservationDto;
import tn.tekup.educated.vieetablissement.web.CampusViews.SurveyDto;
import tn.tekup.educated.vieetablissement.web.CampusViews.TicketDto;

/** School life for every signed-in person: surveys to answer, canteen reservations, the student's entry tickets. */
@RestController
class CampusPortalController {

    record AnswersRequest(@NotNull Map<String, String> answers) {
    }

    record ReservationRequest(@NotNull UUID menuId) {
    }

    private final CurrentUserProvider currentUserProvider;
    private final SurveyService surveys;
    private final CampusService campus;

    CampusPortalController(CurrentUserProvider currentUserProvider, SurveyService surveys, CampusService campus) {
        this.currentUserProvider = currentUserProvider;
        this.surveys = surveys;
        this.campus = campus;
    }

    @GetMapping("/api/me/surveys")
    @Transactional(readOnly = true)
    List<SurveyDto> openSurveys() {
        CurrentUser user = currentUserProvider.require();
        return surveys.openFor(user).stream()
                .map(s -> SurveyDto.from(s, null, !s.isAnonymous() && surveys.hasAnswered(s.getId(), user.userId())))
                .toList();
    }

    @PostMapping("/api/me/surveys/{id}/responses")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    void answer(@PathVariable UUID id, @Valid @RequestBody AnswersRequest r) {
        surveys.respond(id, currentUserProvider.require(), r.answers());
    }

    @GetMapping("/api/me/meal-reservations")
    @Transactional(readOnly = true)
    List<ReservationDto> myReservations() {
        return campus.reservationsOf(person().personId()).stream().map(ReservationDto::from).toList();
    }

    @PostMapping("/api/me/meal-reservations")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    ReservationDto reserve(@Valid @RequestBody ReservationRequest r) {
        CurrentUser user = person();
        return ReservationDto.from(campus.reserve(r.menuId(), user.personType(), user.personId()));
    }

    @DeleteMapping("/api/me/meal-reservations/{id}")
    @Transactional
    ReservationDto cancel(@PathVariable UUID id) {
        return ReservationDto.from(campus.cancelReservation(id, person().personId()));
    }

    @GetMapping("/api/student/entry-tickets")
    List<TicketDto> myTickets() {
        return campus.ticketsOf(currentUserProvider.requirePerson(PersonType.STUDENT)).stream()
                .map(TicketDto::from).toList();
    }

    /** Canteen reservations need an account linked to a student, teacher or staff member. */
    private CurrentUser person() {
        CurrentUser user = currentUserProvider.require();
        if (user.personId() == null || user.personType() == null) {
            throw new AccessDeniedException("This account is not linked to a person.");
        }
        return user;
    }
}
