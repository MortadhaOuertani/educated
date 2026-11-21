package tn.tekup.educated.emploidutemps.api;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.emploidutemps.domain.SessionOccurrence;
import tn.tekup.educated.emploidutemps.domain.SessionOccurrenceRepository;

/** Read-only access to dated sessions for other modules (attendance calls, teacher hours). */
@Service
@Transactional(readOnly = true)
public class ScheduleDirectory {

    private final SessionOccurrenceRepository sessions;

    ScheduleDirectory(SessionOccurrenceRepository sessions) {
        this.sessions = sessions;
    }

    public Optional<SessionInfo> findSession(UUID id) {
        return sessions.findById(id).map(ScheduleDirectory::toInfo);
    }

    public List<SessionInfo> findSessions(Collection<UUID> ids) {
        return sessions.findAllById(ids).stream().map(ScheduleDirectory::toInfo).toList();
    }

    public List<SessionInfo> sessionsOfClass(UUID schoolClassId, LocalDate from, LocalDate to) {
        return sessions.findAllBySchoolClassIdAndSessionDateBetweenOrderBySessionDateAscStartTimeAsc(schoolClassId,
                from, to).stream().map(ScheduleDirectory::toInfo).toList();
    }

    public List<SessionInfo> sessionsOfTeacher(UUID teacherId, LocalDate from, LocalDate to) {
        return sessions.findAllByTeacherIdAndSessionDateBetweenOrderBySessionDateAscStartTimeAsc(teacherId, from, to)
                .stream().map(ScheduleDirectory::toInfo).toList();
    }

    private static SessionInfo toInfo(SessionOccurrence s) {
        return new SessionInfo(s.getId(), s.getTeachingAssignmentId(), s.getTeacherId(), s.getSubjectId(),
                s.getSchoolClassId(), s.getRoomId(), s.getSessionDate(), s.getStartTime(), s.getEndTime(),
                s.getSessionType(), s.getStatus().name(), s.isMakeUp());
    }
}
