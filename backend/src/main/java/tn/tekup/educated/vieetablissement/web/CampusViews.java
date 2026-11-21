package tn.tekup.educated.vieetablissement.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import tn.tekup.educated.vieetablissement.domain.AccessEvent;
import tn.tekup.educated.vieetablissement.domain.AccessPoint;
import tn.tekup.educated.vieetablissement.domain.CanteenMenu;
import tn.tekup.educated.vieetablissement.domain.EntryTicket;
import tn.tekup.educated.vieetablissement.domain.InfirmaryVisit;
import tn.tekup.educated.vieetablissement.domain.MealReservation;
import tn.tekup.educated.vieetablissement.domain.Survey;

/** JSON views of the school-life module. */
final class CampusViews {

    private CampusViews() {
    }

    record QuestionDto(UUID id, Integer orderIndex, String text, String questionType, String optionsJson,
            boolean required) {
    }

    record SurveyDto(UUID id, String title, String description, String purpose, String audience, Instant opensAt,
            Instant closesAt, boolean anonymous, List<QuestionDto> questions, Long responseCount, Boolean answered) {
        static SurveyDto from(Survey s, Long responseCount, Boolean answered) {
            return new SurveyDto(s.getId(), s.getTitle(), s.getDescription(), s.getPurpose().name(),
                    s.getAudience().name(), s.getOpensAt(), s.getClosesAt(), s.isAnonymous(),
                    s.getQuestions().stream().map(q -> new QuestionDto(q.getId(), q.getOrderIndex(), q.getText(),
                            q.getQuestionType().name(), q.getOptionsJson(), q.isRequired())).toList(),
                    responseCount, answered);
        }
    }

    record MenuDto(UUID id, LocalDate menuDate, String mealType, String items, BigDecimal price, Long reserved) {
        static MenuDto from(CanteenMenu m, Long reserved) {
            return new MenuDto(m.getId(), m.getMenuDate(), m.getMealType().name(), m.getItems(),
                    m.getPrice() == null ? null : m.getPrice().amount(), reserved);
        }
    }

    record ReservationDto(UUID id, UUID menuId, LocalDate menuDate, String mealType, String status) {
        static ReservationDto from(MealReservation r) {
            return new ReservationDto(r.getId(), r.getMenu().getId(), r.getMenu().getMenuDate(),
                    r.getMenu().getMealType().name(), r.getStatus().name());
        }
    }

    record VisitDto(UUID id, UUID studentId, Instant visitedAt, String reason, String careGiven,
            boolean guardiansNotified, String followUp) {
        static VisitDto from(InfirmaryVisit v) {
            return new VisitDto(v.getId(), v.getStudentId(), v.getVisitedAt(), v.getReason(), v.getCareGiven(),
                    v.isGuardiansNotified(), v.getFollowUp());
        }
    }

    record TicketDto(UUID id, UUID studentId, String reason, LocalDate validForDate, String note) {
        static TicketDto from(EntryTicket t) {
            return new TicketDto(t.getId(), t.getStudentId(), t.getReason().name(), t.getValidForDate(), t.getNote());
        }
    }

    record AccessPointDto(UUID id, String code, String name, UUID siteId) {
        static AccessPointDto from(AccessPoint p) {
            return new AccessPointDto(p.getId(), p.getCode(), p.getName(), p.getSiteId());
        }
    }

    record AccessEventDto(UUID id, String accessPointCode, String personType, UUID personId, Instant occurredAt,
            String direction, String method, boolean granted) {
        static AccessEventDto from(AccessEvent e) {
            return new AccessEventDto(e.getId(), e.getAccessPoint().getCode(), e.getPersonType().name(),
                    e.getPersonId(), e.getOccurredAt(), e.getDirection().name(), e.getMethod().name(), e.isGranted());
        }
    }
}
