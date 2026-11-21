package tn.tekup.educated.vieetablissement.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.scolarite.api.StudentDirectory;
import tn.tekup.educated.shared.domain.Money;
import tn.tekup.educated.shared.domain.PersonType;
import tn.tekup.educated.shared.web.BusinessRuleException;
import tn.tekup.educated.shared.web.NotFoundException;
import tn.tekup.educated.vieetablissement.domain.AccessDirection;
import tn.tekup.educated.vieetablissement.domain.AccessEvent;
import tn.tekup.educated.vieetablissement.domain.AccessEventRepository;
import tn.tekup.educated.vieetablissement.domain.AccessMethod;
import tn.tekup.educated.vieetablissement.domain.AccessPoint;
import tn.tekup.educated.vieetablissement.domain.AccessPointRepository;
import tn.tekup.educated.vieetablissement.domain.CanteenMenu;
import tn.tekup.educated.vieetablissement.domain.CanteenMenuRepository;
import tn.tekup.educated.vieetablissement.domain.EntryTicket;
import tn.tekup.educated.vieetablissement.domain.EntryTicketReason;
import tn.tekup.educated.vieetablissement.domain.EntryTicketRepository;
import tn.tekup.educated.vieetablissement.domain.InfirmaryVisit;
import tn.tekup.educated.vieetablissement.domain.InfirmaryVisitRepository;
import tn.tekup.educated.vieetablissement.domain.MealReservation;
import tn.tekup.educated.vieetablissement.domain.MealReservationRepository;
import tn.tekup.educated.vieetablissement.domain.MealType;
import tn.tekup.educated.vieetablissement.domain.ReservationStatus;

/** Services of the establishment: canteen, infirmary, entry tickets and access control. */
@Service
@Transactional
public class CampusService {

    private final CanteenMenuRepository menus;
    private final MealReservationRepository reservations;
    private final InfirmaryVisitRepository visits;
    private final EntryTicketRepository tickets;
    private final AccessPointRepository accessPoints;
    private final AccessEventRepository accessEvents;
    private final StudentDirectory students;

    public CampusService(CanteenMenuRepository menus, MealReservationRepository reservations,
            InfirmaryVisitRepository visits, EntryTicketRepository tickets, AccessPointRepository accessPoints,
            AccessEventRepository accessEvents, StudentDirectory students) {
        this.menus = menus;
        this.reservations = reservations;
        this.visits = visits;
        this.tickets = tickets;
        this.accessPoints = accessPoints;
        this.accessEvents = accessEvents;
        this.students = students;
    }

    @Transactional(readOnly = true)
    public List<CanteenMenu> menus(LocalDate from, LocalDate to) {
        return menus.findAllByMenuDateBetweenOrderByMenuDateAscMealTypeAsc(from, to);
    }

    public CanteenMenu createMenu(LocalDate date, MealType mealType, String items, BigDecimal price) {
        if (menus.existsByMenuDateAndMealType(date, mealType)) {
            throw new BusinessRuleException("canteen.menuExists", "There is already a menu for this meal.");
        }
        CanteenMenu menu = new CanteenMenu();
        menu.setMenuDate(date);
        menu.setMealType(mealType);
        menu.setItems(items);
        menu.setPrice(price == null ? null : Money.tnd(price));
        return menus.save(menu);
    }

    @Transactional(readOnly = true)
    public long reservedCount(UUID menuId) {
        return reservations.countByMenu_IdAndStatus(menuId, ReservationStatus.RESERVED);
    }

    /** Reservations are taken until the day before the meal. */
    public MealReservation reserve(UUID menuId, PersonType personType, UUID personId) {
        CanteenMenu menu = menus.findById(menuId).orElseThrow(() -> new NotFoundException("CanteenMenu", menuId));
        if (!menu.getMenuDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("canteen.tooLate", "Meals must be reserved before the day of the meal.");
        }
        MealReservation reservation = reservations.findByMenu_IdAndPersonId(menuId, personId).orElseGet(() -> {
            MealReservation created = new MealReservation();
            created.setMenu(menu);
            created.setPersonType(personType);
            created.setPersonId(personId);
            return created;
        });
        if (reservation.getStatus() == ReservationStatus.RESERVED) {
            throw new BusinessRuleException("canteen.alreadyReserved", "This meal is already reserved.");
        }
        reservation.setStatus(ReservationStatus.RESERVED);
        return reservations.save(reservation);
    }

    public MealReservation cancelReservation(UUID reservationId, UUID personId) {
        MealReservation reservation = reservations.findById(reservationId)
                .filter(r -> r.getPersonId().equals(personId))
                .orElseThrow(() -> new NotFoundException("MealReservation", reservationId));
        if (!reservation.getMenu().getMenuDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("canteen.tooLate", "A reservation cannot be cancelled on the day of the meal.");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservation;
    }

    @Transactional(readOnly = true)
    public List<MealReservation> reservationsOf(UUID personId) {
        return reservations.findAllByPersonIdOrderByMenu_MenuDateDesc(personId);
    }

    @Transactional(readOnly = true)
    public List<InfirmaryVisit> visits(UUID studentId) {
        return studentId == null ? visits.findAllByOrderByVisitedAtDesc() : visits.findAllByStudentIdOrderByVisitedAtDesc(studentId);
    }

    public InfirmaryVisit recordVisit(UUID studentId, Instant visitedAt, String reason, String careGiven,
            boolean guardiansNotified, String followUp) {
        requireStudent(studentId);
        InfirmaryVisit visit = new InfirmaryVisit();
        visit.setStudentId(studentId);
        visit.setVisitedAt(visitedAt == null ? Instant.now() : visitedAt);
        visit.setReason(reason);
        visit.setCareGiven(careGiven);
        visit.setGuardiansNotified(guardiansNotified);
        visit.setFollowUp(followUp);
        return visits.save(visit);
    }

    /** Billet d'entrée: lets a late or returning student into class on the given day. */
    public EntryTicket issueTicket(UUID studentId, EntryTicketReason reason, LocalDate validForDate, String note,
            UUID attendanceRecordId) {
        requireStudent(studentId);
        EntryTicket ticket = new EntryTicket();
        ticket.setStudentId(studentId);
        ticket.setReason(reason);
        ticket.setValidForDate(validForDate == null ? LocalDate.now() : validForDate);
        ticket.setNote(note);
        ticket.setAttendanceRecordId(attendanceRecordId);
        return tickets.save(ticket);
    }

    @Transactional(readOnly = true)
    public List<EntryTicket> ticketsOf(UUID studentId) {
        return tickets.findAllByStudentIdOrderByValidForDateDesc(studentId);
    }

    @Transactional(readOnly = true)
    public List<EntryTicket> ticketsOn(LocalDate date) {
        return tickets.findAllByValidForDateOrderByCreatedAt(date);
    }

    @Transactional(readOnly = true)
    public List<AccessPoint> accessPoints() {
        return accessPoints.findAllByOrderByCode();
    }

    public AccessPoint createAccessPoint(String code, String name, UUID siteId) {
        if (accessPoints.findByCode(code).isPresent()) {
            throw new BusinessRuleException("accessPoint.code", "Access point " + code + " already exists.");
        }
        AccessPoint point = new AccessPoint();
        point.setCode(code);
        point.setName(name);
        point.setSiteId(siteId);
        return accessPoints.save(point);
    }

    public AccessEvent recordAccess(UUID accessPointId, PersonType personType, UUID personId,
            AccessDirection direction, AccessMethod method, boolean granted) {
        AccessPoint point = accessPoints.findById(accessPointId)
                .orElseThrow(() -> new NotFoundException("AccessPoint", accessPointId));
        AccessEvent event = new AccessEvent();
        event.setAccessPoint(point);
        event.setPersonType(personType);
        event.setPersonId(personId);
        event.setOccurredAt(Instant.now());
        event.setDirection(direction);
        event.setMethod(method);
        event.setGranted(granted);
        return accessEvents.save(event);
    }

    @Transactional(readOnly = true)
    public List<AccessEvent> accessEventsOn(LocalDate date) {
        ZoneId zone = ZoneId.systemDefault();
        return accessEvents.findAllByOccurredAtBetweenOrderByOccurredAtDesc(date.atStartOfDay(zone).toInstant(),
                date.plusDays(1).atStartOfDay(zone).toInstant());
    }

    private void requireStudent(UUID studentId) {
        if (studentId == null || students.findStudent(studentId).isEmpty()) {
            throw new NotFoundException("Student", studentId);
        }
    }
}
