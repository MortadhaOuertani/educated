package tn.tekup.educated.assiduite.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.assiduite.domain.AttendanceRecord;
import tn.tekup.educated.assiduite.domain.AttendanceRecordRepository;
import tn.tekup.educated.assiduite.domain.AttendanceStatus;
import tn.tekup.educated.assiduite.domain.RequestStatus;
import tn.tekup.educated.assiduite.domain.StudentAbsenceRequest;
import tn.tekup.educated.assiduite.domain.StudentAbsenceRequestRepository;
import tn.tekup.educated.emploidutemps.api.ScheduleDirectory;
import tn.tekup.educated.emploidutemps.api.SessionInfo;
import tn.tekup.educated.scolarite.api.StudentDirectory;
import tn.tekup.educated.shared.security.CurrentUserProvider;
import tn.tekup.educated.shared.web.BusinessRuleException;
import tn.tekup.educated.shared.web.NotFoundException;

/** Demandes d'absence: a student justifies a period; approving it justifies the absences of that period. */
@Service
@Transactional
public class AbsenceRequestService {

    private final StudentAbsenceRequestRepository requests;
    private final AttendanceRecordRepository records;
    private final ScheduleDirectory schedule;
    private final StudentDirectory students;
    private final CurrentUserProvider currentUserProvider;

    public AbsenceRequestService(StudentAbsenceRequestRepository requests, AttendanceRecordRepository records,
            ScheduleDirectory schedule, StudentDirectory students, CurrentUserProvider currentUserProvider) {
        this.requests = requests;
        this.records = records;
        this.schedule = schedule;
        this.students = students;
        this.currentUserProvider = currentUserProvider;
    }

    public StudentAbsenceRequest submit(UUID studentId, LocalDateTime from, LocalDateTime to, String reason,
            UUID proofFileId) {
        if (!to.isAfter(from)) {
            throw new BusinessRuleException("absenceRequest.period", "The end must be after the start.");
        }
        StudentAbsenceRequest request = new StudentAbsenceRequest();
        request.setStudentId(studentId);
        request.setFromDateTime(from);
        request.setToDateTime(to);
        request.setReason(reason);
        request.setProofFileId(proofFileId);
        request.setStatus(RequestStatus.PENDING);
        return requests.save(request);
    }

    @Transactional(readOnly = true)
    public List<StudentAbsenceRequest> requestsOf(UUID studentId) {
        return requests.findAllByStudentIdOrderByCreatedAtDesc(studentId);
    }

    @Transactional(readOnly = true)
    public List<StudentAbsenceRequest> list(RequestStatus status) {
        return status == null ? requests.findAllByOrderByCreatedAtDesc() : requests.findAllByStatusOrderByCreatedAt(status);
    }

    /** Approving marks the student's absences and late arrivals in the requested period as justified. */
    public StudentAbsenceRequest review(UUID requestId, RequestStatus decision) {
        if (decision == RequestStatus.PENDING) {
            throw new BusinessRuleException("absenceRequest.decision", "Approve or reject the request.");
        }
        StudentAbsenceRequest request = requests.findById(requestId)
                .orElseThrow(() -> new NotFoundException("StudentAbsenceRequest", requestId));
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BusinessRuleException("absenceRequest.alreadyReviewed", "This request was already reviewed.");
        }
        request.setStatus(decision);
        request.setReviewedAt(Instant.now());
        request.setReviewedByUserId(currentUserProvider.require().userId());
        if (decision == RequestStatus.APPROVED) {
            justifyAbsences(request);
        }
        return request;
    }

    private void justifyAbsences(StudentAbsenceRequest request) {
        UUID classId = students.currentClassId(request.getStudentId()).orElse(null);
        if (classId == null) {
            return;
        }
        List<UUID> sessionIds = schedule.sessionsOfClass(classId, request.getFromDateTime().toLocalDate(),
                        request.getToDateTime().toLocalDate()).stream()
                .filter(session -> overlaps(session, request))
                .map(SessionInfo::id)
                .toList();
        if (sessionIds.isEmpty()) {
            return;
        }
        for (AttendanceRecord record : records.findAllByStudentIdAndSheet_SessionOccurrenceIdIn(request.getStudentId(),
                sessionIds)) {
            if (record.getStatus() != AttendanceStatus.PRESENT) {
                record.setJustified(true);
                record.setJustifiedBy(request);
            }
        }
    }

    private static boolean overlaps(SessionInfo session, StudentAbsenceRequest request) {
        LocalDateTime start = session.date().atTime(session.startTime());
        LocalDateTime end = session.date().atTime(session.endTime());
        return start.isBefore(request.getToDateTime()) && end.isAfter(request.getFromDateTime());
    }
}
