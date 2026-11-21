package tn.tekup.educated.assiduite.api;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.assiduite.domain.TeacherAttendanceRepository;

/** Read-only access for other modules (teacher payments are based on validated hours). */
@Service
@Transactional(readOnly = true)
public class AttendanceDirectory {

    private final TeacherAttendanceRepository teacherAttendance;

    AttendanceDirectory(TeacherAttendanceRepository teacherAttendance) {
        this.teacherAttendance = teacherAttendance;
    }

    public List<TeacherHoursInfo> validatedTeacherHours(UUID teacherId, LocalDate from, LocalDate to) {
        return teacherAttendance.findAllByTeacherIdAndValidatedTrueAndSessionDateBetweenOrderBySessionDate(teacherId,
                        from, to).stream()
                .map(t -> new TeacherHoursInfo(t.getId(), t.getTeacherId(), t.getSessionOccurrenceId(),
                        t.getSessionDate(), t.getHours()))
                .toList();
    }
}
