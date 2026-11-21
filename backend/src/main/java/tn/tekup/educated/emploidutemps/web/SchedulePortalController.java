package tn.tekup.educated.emploidutemps.web;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.tekup.educated.emploidutemps.service.TimetableService;
import tn.tekup.educated.emploidutemps.web.SessionViews.SessionDto;
import tn.tekup.educated.scolarite.api.StudentDirectory;
import tn.tekup.educated.shared.domain.PersonType;
import tn.tekup.educated.shared.security.CurrentUserProvider;

/** Timetables in the portals: a teacher sees their own sessions, a student the sessions of their class. */
@RestController
class SchedulePortalController {

    private final CurrentUserProvider currentUserProvider;
    private final TimetableService timetables;
    private final StudentDirectory students;
    private final SessionViews views;

    SchedulePortalController(CurrentUserProvider currentUserProvider, TimetableService timetables,
            StudentDirectory students, SessionViews views) {
        this.currentUserProvider = currentUserProvider;
        this.timetables = timetables;
        this.students = students;
        this.views = views;
    }

    @GetMapping("/api/teacher/sessions")
    @Transactional(readOnly = true)
    List<SessionDto> teacherSessions(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        UUID teacherId = currentUserProvider.requirePerson(PersonType.TEACHER);
        return views.sessions(timetables.sessionsOfTeacher(teacherId, from, to));
    }

    @GetMapping("/api/student/timetable")
    @Transactional(readOnly = true)
    List<SessionDto> studentTimetable(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        UUID studentId = currentUserProvider.requirePerson(PersonType.STUDENT);
        return students.currentClassId(studentId)
                .map(classId -> views.sessions(timetables.sessionsOfClass(classId, from, to)))
                .orElse(List.of());
    }
}
