package tn.tekup.educated.scolarite.web;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.tekup.educated.scolarite.domain.DocumentRequestStatus;
import tn.tekup.educated.scolarite.service.DocumentRequestService;
import tn.tekup.educated.scolarite.web.ScolariteDtos.DocumentRequestDto;
import tn.tekup.educated.scolarite.web.ScolariteDtos.DocumentTypeDto;
import tn.tekup.educated.scolarite.web.ScolariteDtos.StatusChangeRequest;
import tn.tekup.educated.shared.web.PageResponse;

/** Staff side of "Demandes de documents". */
@RestController
@RequestMapping("/api/scolarite")
class DocumentRequestController {

    private final DocumentRequestService documentRequests;

    DocumentRequestController(DocumentRequestService documentRequests) {
        this.documentRequests = documentRequests;
    }

    /** The document catalogue; every signed-in user may read it (students need it to make a request). */
    @GetMapping("/document-types")
    List<DocumentTypeDto> documentTypes() {
        return documentRequests.activeTypes().stream().map(DocumentTypeDto::from).toList();
    }

    @GetMapping("/document-requests")
    @PreAuthorize("@perm.can('DocumentRequest', 'READ')")
    @Transactional(readOnly = true)
    PageResponse<DocumentRequestDto> list(@RequestParam(required = false) DocumentRequestStatus status,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.of(documentRequests.list(status, pageable), DocumentRequestDto::from);
    }

    @PatchMapping("/document-requests/{id}/status")
    @PreAuthorize("@perm.can('DocumentRequest', 'WRITE')")
    @Transactional
    DocumentRequestDto changeStatus(@PathVariable UUID id, @Valid @RequestBody StatusChangeRequest r) {
        return DocumentRequestDto.from(documentRequests.changeStatus(id, r.status(), r.rejectionReason()));
    }
}
