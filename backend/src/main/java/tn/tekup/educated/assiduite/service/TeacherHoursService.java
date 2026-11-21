package tn.tekup.educated.assiduite.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.assiduite.domain.TeacherAttendance;
import tn.tekup.educated.assiduite.domain.TeacherAttendanceRepository;
import tn.tekup.educated.shared.security.CurrentUserProvider;
import tn.tekup.educated.shared.web.BusinessRuleException;
import tn.tekup.educated.shared.web.NotFoundException;

/** Présence des enseignants: hours recorded from confirmed calls, validated before they can be paid. */
@Service
@Transactional
public class TeacherHoursService {

    private final TeacherAttendanceRepository teacherAttendance;
    private final CurrentUserProvider currentUserProvider;

    public TeacherHoursService(TeacherAttendanceRepository teacherAttendance, CurrentUserProvider currentUserProvider) {
        this.teacherAttendance = teacherAttendance;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional(readOnly = true)
    public List<TeacherAttendance> hoursOf(UUID teacherId, LocalDate from, LocalDate to) {
        return teacherAttendance.findAllByTeacherIdAndSessionDateBetweenOrderBySessionDate(teacherId, from, to);
    }

    public TeacherAttendance validate(UUID id) {
        TeacherAttendance hours = teacherAttendance.findById(id)
                .orElseThrow(() -> new NotFoundException("TeacherAttendance", id));
        if (hours.isValidated()) {
            throw new BusinessRuleException("teacherHours.alreadyValidated", "These hours are already validated.");
        }
        hours.setValidated(true);
        hours.setValidatedAt(Instant.now());
        hours.setValidatedByUserId(currentUserProvider.require().userId());
        return hours;
    }
}
