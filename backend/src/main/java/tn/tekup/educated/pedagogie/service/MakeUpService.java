package tn.tekup.educated.pedagogie.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.pedagogie.api.MakeUpConfirmed;
import tn.tekup.educated.pedagogie.domain.MakeUpRequest;
import tn.tekup.educated.pedagogie.domain.MakeUpRequestRepository;
import tn.tekup.educated.pedagogie.domain.MakeUpStatus;
import tn.tekup.educated.pedagogie.domain.TeachingAssignment;
import tn.tekup.educated.shared.security.CurrentUserProvider;
import tn.tekup.educated.shared.web.BusinessRuleException;
import tn.tekup.educated.shared.web.NotFoundException;

/** Séances de rattrapage: the teacher asks, the administration confirms or rejects. */
@Service
@Transactional
public class MakeUpService {

    public record MakeUpData(UUID originalOccurrenceId, LocalDate requestedDate, LocalTime startTime,
            LocalTime endTime, UUID roomId, String reason) {
    }

    private final MakeUpRequestRepository requests;
    private final CurrentUserProvider currentUserProvider;
    private final ApplicationEventPublisher events;

    public MakeUpService(MakeUpRequestRepository requests, CurrentUserProvider currentUserProvider,
            ApplicationEventPublisher events) {
        this.requests = requests;
        this.currentUserProvider = currentUserProvider;
        this.events = events;
    }

    public MakeUpRequest request(TeachingAssignment assignment, MakeUpData data) {
        if (!data.endTime().isAfter(data.startTime())) {
            throw new BusinessRuleException("makeUp.time", "The session must end after it starts.");
        }
        MakeUpRequest request = new MakeUpRequest();
        request.setTeachingAssignment(assignment);
        request.setTeacher(assignment.getTeacher());
        request.setOriginalOccurrenceId(data.originalOccurrenceId());
        request.setRequestedDate(data.requestedDate());
        request.setStartTime(data.startTime());
        request.setEndTime(data.endTime());
        request.setRoomId(data.roomId());
        request.setReason(data.reason());
        request.setStatus(MakeUpStatus.REQUESTED);
        return requests.save(request);
    }

    @Transactional(readOnly = true)
    public List<MakeUpRequest> requestsOfTeacher(UUID teacherId) {
        return requests.findAllByTeacher_IdOrderByCreatedAtDesc(teacherId);
    }

    @Transactional(readOnly = true)
    public List<MakeUpRequest> list(MakeUpStatus status) {
        return status == null ? requests.findAllByOrderByCreatedAtDesc() : requests.findAllByStatusOrderByCreatedAt(status);
    }

    /** Confirming publishes {@link MakeUpConfirmed} so the timetable module schedules the session. */
    public MakeUpRequest decide(UUID requestId, MakeUpStatus decision) {
        if (decision != MakeUpStatus.CONFIRMED && decision != MakeUpStatus.REJECTED) {
            throw new BusinessRuleException("makeUp.decision", "A request can only be confirmed or rejected.");
        }
        MakeUpRequest request = requests.findById(requestId)
                .orElseThrow(() -> new NotFoundException("MakeUpRequest", requestId));
        if (request.getStatus() != MakeUpStatus.REQUESTED) {
            throw new BusinessRuleException("makeUp.alreadyDecided", "This request was already " + request.getStatus() + ".");
        }
        request.setStatus(decision);
        request.setDecidedAt(Instant.now());
        request.setDecidedByUserId(currentUserProvider.require().userId());
        if (decision == MakeUpStatus.CONFIRMED) {
            events.publishEvent(new MakeUpConfirmed(request.getEstablishmentId(), request.getId(),
                    request.getOriginalOccurrenceId(), request.getTeachingAssignment().getId(),
                    request.getRequestedDate(), request.getStartTime(), request.getEndTime(), request.getRoomId()));
        }
        return request;
    }
}
