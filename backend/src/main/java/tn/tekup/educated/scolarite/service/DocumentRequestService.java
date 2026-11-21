package tn.tekup.educated.scolarite.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.scolarite.api.DocumentRequestStatusChanged;
import tn.tekup.educated.scolarite.domain.DocumentRequest;
import tn.tekup.educated.scolarite.domain.DocumentRequestRepository;
import tn.tekup.educated.scolarite.domain.DocumentRequestStatus;
import tn.tekup.educated.scolarite.domain.DocumentType;
import tn.tekup.educated.scolarite.domain.DocumentTypeRepository;
import tn.tekup.educated.shared.security.CurrentUserProvider;
import tn.tekup.educated.shared.web.BusinessRuleException;
import tn.tekup.educated.shared.web.NotFoundException;

/** Demandes de documents: Déposée (SUBMITTED) → En traitement (IN_PROGRESS) → Traitée (DONE), or REJECTED. */
@Service
@Transactional
public class DocumentRequestService {

    private static final Map<DocumentRequestStatus, Set<DocumentRequestStatus>> NEXT = Map.of(
            DocumentRequestStatus.SUBMITTED, Set.of(DocumentRequestStatus.IN_PROGRESS, DocumentRequestStatus.REJECTED),
            DocumentRequestStatus.IN_PROGRESS, Set.of(DocumentRequestStatus.DONE, DocumentRequestStatus.REJECTED),
            DocumentRequestStatus.DONE, Set.of(),
            DocumentRequestStatus.REJECTED, Set.of());

    private final DocumentRequestRepository requests;
    private final DocumentTypeRepository documentTypes;
    private final StudentService studentService;
    private final CurrentUserProvider currentUserProvider;
    private final ApplicationEventPublisher events;

    public DocumentRequestService(DocumentRequestRepository requests, DocumentTypeRepository documentTypes,
            StudentService studentService, CurrentUserProvider currentUserProvider, ApplicationEventPublisher events) {
        this.requests = requests;
        this.documentTypes = documentTypes;
        this.studentService = studentService;
        this.currentUserProvider = currentUserProvider;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<DocumentType> activeTypes() {
        return documentTypes.findAllByActiveTrueOrderByCodeAscLanguageAsc();
    }

    public DocumentRequest create(UUID studentId, UUID documentTypeId, String reason, UUID proofFileId) {
        DocumentType type = documentTypes.findById(documentTypeId)
                .filter(DocumentType::isActive)
                .orElseThrow(() -> new NotFoundException("DocumentType", documentTypeId));
        DocumentRequest request = new DocumentRequest();
        request.setStudent(studentService.get(studentId));
        request.setDocumentType(type);
        request.setReason(reason);
        request.setProofFileId(proofFileId);
        request.setStatus(DocumentRequestStatus.SUBMITTED);
        return requests.save(request);
    }

    @Transactional(readOnly = true)
    public List<DocumentRequest> requestsOf(UUID studentId) {
        return requests.findAllByStudent_IdOrderByCreatedAtDesc(studentId);
    }

    @Transactional(readOnly = true)
    public Page<DocumentRequest> list(DocumentRequestStatus status, Pageable pageable) {
        return status == null ? requests.findAllBy(pageable) : requests.findAllByStatus(status, pageable);
    }

    public DocumentRequest changeStatus(UUID requestId, DocumentRequestStatus newStatus, String rejectionReason) {
        DocumentRequest request = requests.findById(requestId)
                .orElseThrow(() -> new NotFoundException("DocumentRequest", requestId));
        if (!NEXT.get(request.getStatus()).contains(newStatus)) {
            throw new BusinessRuleException("documentRequest.transition",
                    "A request cannot go from " + request.getStatus() + " to " + newStatus + ".");
        }
        if (newStatus == DocumentRequestStatus.REJECTED && (rejectionReason == null || rejectionReason.isBlank())) {
            throw new BusinessRuleException("documentRequest.reason", "Give a reason when rejecting a request.");
        }
        request.setStatus(newStatus);
        if (newStatus == DocumentRequestStatus.DONE || newStatus == DocumentRequestStatus.REJECTED) {
            request.setProcessedByUserId(currentUserProvider.require().userId());
            request.setProcessedAt(Instant.now());
            request.setRejectionReason(rejectionReason);
        }
        events.publishEvent(new DocumentRequestStatusChanged(request.getEstablishmentId(), request.getId(),
                request.getStudent().getId(), newStatus.name()));
        return request;
    }
}
