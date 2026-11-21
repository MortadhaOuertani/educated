package tn.tekup.educated.emploidutemps.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tn.tekup.educated.emploidutemps.domain.SessionOccurrence;
import tn.tekup.educated.emploidutemps.domain.TimeSlotTemplate;
import tn.tekup.educated.emploidutemps.domain.WeekPattern;
import tn.tekup.educated.emploidutemps.service.TimetableService;
import tn.tekup.educated.emploidutemps.service.TimetableService.SlotData;
import tn.tekup.educated.emploidutemps.web.SessionViews.SessionDto;
import tn.tekup.educated.emploidutemps.web.SessionViews.SlotDto;
import tn.tekup.educated.emploidutemps.web.SessionViews.TimetableDto;

/** Back-office screens of the Emplois du temps module. */
@RestController
@RequestMapping("/api/emploidutemps")
class TimetableController {

    record TimeSlotDto(UUID id, String code, LocalTime startTime, LocalTime endTime) {
        static TimeSlotDto from(TimeSlotTemplate t) {
            return new TimeSlotDto(t.getId(), t.getCode(), t.getStartTime(), t.getEndTime());
        }
    }

    record NewTimetableRequest(@NotNull UUID schoolClassId, @NotNull UUID academicPeriodId, String name) {
    }

    record SlotRequest(@NotNull UUID teachingAssignmentId, UUID roomId, @NotNull DayOfWeek dayOfWeek,
            LocalTime startTime, LocalTime endTime, WeekPattern weekPattern, String timeSlotCode) {
    }

    record CancelRequest(String reason) {
    }

    private final TimetableService timetables;
    private final SessionViews views;

    TimetableController(TimetableService timetables, SessionViews views) {
        this.timetables = timetables;
        this.views = views;
    }

    @GetMapping("/time-slots")
    @PreAuthorize("@perm.can('TimeSlotTemplate', 'READ')")
    List<TimeSlotDto> timeSlots() {
        return timetables.timeSlots().stream().map(TimeSlotDto::from).toList();
    }

    @GetMapping("/timetables")
    @PreAuthorize("@perm.can('Timetable', 'READ')")
    @Transactional(readOnly = true)
    List<TimetableDto> timetables(@RequestParam UUID schoolClassId) {
        return timetables.timetablesOfClass(schoolClassId).stream().map(views::timetable).toList();
    }

    @GetMapping("/timetables/{id}")
    @PreAuthorize("@perm.can('Timetable', 'READ')")
    @Transactional(readOnly = true)
    TimetableDto timetable(@PathVariable UUID id) {
        return views.timetable(timetables.timetable(id));
    }

    @PostMapping("/timetables")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('Timetable', 'CREATE')")
    @Transactional
    TimetableDto create(@Valid @RequestBody NewTimetableRequest r) {
        return views.timetable(timetables.create(r.schoolClassId(), r.academicPeriodId(), r.name()));
    }

    @PostMapping("/timetables/{id}/slots")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('Timetable', 'WRITE')")
    @Transactional
    SlotDto addSlot(@PathVariable UUID id, @Valid @RequestBody SlotRequest r) {
        return views.slot(timetables.addSlot(id, new SlotData(r.teachingAssignmentId(), r.roomId(), r.dayOfWeek(),
                r.startTime(), r.endTime(), r.weekPattern(), r.timeSlotCode())));
    }

    @DeleteMapping("/timetables/{id}/slots/{slotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@perm.can('Timetable', 'WRITE')")
    void removeSlot(@PathVariable UUID id, @PathVariable UUID slotId) {
        timetables.removeSlot(id, slotId);
    }

    @PostMapping("/timetables/{id}/publish")
    @PreAuthorize("@perm.can('Timetable', 'WRITE')")
    @Transactional
    TimetableDto publish(@PathVariable UUID id) {
        return views.timetable(timetables.publish(id));
    }

    /** Programme des séances of a class or a teacher between two dates. */
    @GetMapping("/sessions")
    @PreAuthorize("@perm.can('SessionOccurrence', 'READ')")
    @Transactional(readOnly = true)
    List<SessionDto> sessions(@RequestParam(required = false) UUID schoolClassId,
            @RequestParam(required = false) UUID teacherId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<SessionOccurrence> found = schoolClassId != null ? timetables.sessionsOfClass(schoolClassId, from, to)
                : teacherId != null ? timetables.sessionsOfTeacher(teacherId, from, to) : List.of();
        return views.sessions(found);
    }

    @PostMapping("/sessions/{id}/cancel")
    @PreAuthorize("@perm.can('SessionOccurrence', 'WRITE')")
    @Transactional
    SessionDto cancel(@PathVariable UUID id, @RequestBody CancelRequest r) {
        return views.sessions(List.of(timetables.cancelSession(id, r.reason()))).getFirst();
    }
}
