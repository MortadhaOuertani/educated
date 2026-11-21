package tn.tekup.educated.emploidutemps.web;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import tn.tekup.educated.emploidutemps.domain.SessionOccurrence;
import tn.tekup.educated.emploidutemps.domain.Timetable;
import tn.tekup.educated.emploidutemps.domain.TimetableSlot;
import tn.tekup.educated.parametrage.api.ParametrageDirectory;
import tn.tekup.educated.parametrage.api.RoomInfo;
import tn.tekup.educated.parametrage.api.SchoolClassInfo;
import tn.tekup.educated.pedagogie.api.TeachingAssignmentInfo;
import tn.tekup.educated.pedagogie.api.TeachingDirectory;
import tn.tekup.educated.shared.domain.SessionType;

/** Builds the JSON views of sessions and timetables, with subject, teacher, room and class names. */
@Component
class SessionViews {

    record SessionDto(UUID id, LocalDate date, LocalTime startTime, LocalTime endTime, SessionType sessionType,
            String status, boolean makeUp, String cancellationReason, UUID teachingAssignmentId, String subjectCode,
            String subjectName, UUID teacherId, String teacherName, UUID schoolClassId, String classCode,
            UUID roomId, String roomCode) {
    }

    record SlotDto(UUID id, UUID teachingAssignmentId, String subjectCode, String subjectName, String teacherName,
            DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, String weekPattern, UUID roomId,
            String roomCode) {
    }

    record TimetableDto(UUID id, UUID schoolClassId, String classCode, UUID academicPeriodId, String name,
            String status, Integer versionNumber, LocalDate validFrom, LocalDate validTo, List<SlotDto> slots) {
    }

    private final TeachingDirectory teaching;
    private final ParametrageDirectory parametrage;

    SessionViews(TeachingDirectory teaching, ParametrageDirectory parametrage) {
        this.teaching = teaching;
        this.parametrage = parametrage;
    }

    List<SessionDto> sessions(List<SessionOccurrence> sessions) {
        Lookups lookups = new Lookups();
        return sessions.stream().map(s -> {
            Optional<TeachingAssignmentInfo> a = lookups.assignment(s.getTeachingAssignmentId());
            return new SessionDto(s.getId(), s.getSessionDate(), s.getStartTime(), s.getEndTime(),
                    s.getSessionType(), s.getStatus().name(), s.isMakeUp(), s.getCancellationReason(),
                    s.getTeachingAssignmentId(), a.map(TeachingAssignmentInfo::subjectCode).orElse(null),
                    a.map(TeachingAssignmentInfo::subjectName).orElse(null), s.getTeacherId(),
                    a.map(TeachingAssignmentInfo::teacherName).orElse(null), s.getSchoolClassId(),
                    lookups.classCode(s.getSchoolClassId()), s.getRoomId(), lookups.roomCode(s.getRoomId()));
        }).toList();
    }

    TimetableDto timetable(Timetable t) {
        Lookups lookups = new Lookups();
        List<SlotDto> slots = t.getSlots().stream()
                .sorted((x, y) -> x.getDayOfWeek() != y.getDayOfWeek() ? x.getDayOfWeek().compareTo(y.getDayOfWeek())
                        : x.getStartTime().compareTo(y.getStartTime()))
                .map(s -> slot(s, lookups))
                .toList();
        return new TimetableDto(t.getId(), t.getSchoolClassId(), lookups.classCode(t.getSchoolClassId()),
                t.getAcademicPeriodId(), t.getName(), t.getStatus().name(), t.getVersionNumber(), t.getValidFrom(),
                t.getValidTo(), slots);
    }

    SlotDto slot(TimetableSlot s) {
        return slot(s, new Lookups());
    }

    private static SlotDto slot(TimetableSlot s, Lookups lookups) {
        Optional<TeachingAssignmentInfo> a = lookups.assignment(s.getTeachingAssignmentId());
        return new SlotDto(s.getId(), s.getTeachingAssignmentId(), a.map(TeachingAssignmentInfo::subjectCode).orElse(null),
                a.map(TeachingAssignmentInfo::subjectName).orElse(null),
                a.map(TeachingAssignmentInfo::teacherName).orElse(null), s.getDayOfWeek(), s.getStartTime(),
                s.getEndTime(), s.getWeekPattern().name(), s.getRoomId(), lookups.roomCode(s.getRoomId()));
    }

    /** Remembers lookups during one response so each assignment, class and room is read once. */
    private final class Lookups {
        private final Map<UUID, Optional<TeachingAssignmentInfo>> assignments = new HashMap<>();
        private final Map<UUID, String> classCodes = new HashMap<>();
        private final Map<UUID, String> roomCodes = new HashMap<>();

        Optional<TeachingAssignmentInfo> assignment(UUID id) {
            return assignments.computeIfAbsent(id, teaching::findAssignment);
        }

        String classCode(UUID id) {
            return classCodes.computeIfAbsent(id, key -> parametrage.findSchoolClass(key).map(SchoolClassInfo::code)
                    .orElse(null));
        }

        String roomCode(UUID id) {
            return id == null ? null : roomCodes.computeIfAbsent(id, key -> parametrage.findRoom(key)
                    .map(RoomInfo::code).orElse(null));
        }
    }
}
