package tn.tekup.educated.scolarite.web;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tn.tekup.educated.scolarite.service.DocumentRequestService;
import tn.tekup.educated.scolarite.service.EnrollmentService;
import tn.tekup.educated.scolarite.service.StudentService;
import tn.tekup.educated.scolarite.web.ScolariteDtos.DocumentRequestDto;
import tn.tekup.educated.scolarite.web.ScolariteDtos.StudentDto;
import tn.tekup.educated.scolarite.web.ScolariteDtos.StudentProfileDto;
import tn.tekup.educated.shared.domain.PersonType;
import tn.tekup.educated.shared.files.FileStorage;
import tn.tekup.educated.shared.security.CurrentUser;
import tn.tekup.educated.shared.security.CurrentUserProvider;

/** Student portal: a student only ever sees and changes their own data here. */
@RestController
@RequestMapping("/api/student")
class StudentPortalController {

    private final CurrentUserProvider currentUserProvider;
    private final StudentService students;
    private final EnrollmentService enrollments;
    private final DocumentRequestService documentRequests;
    private final FileStorage fileStorage;

    StudentPortalController(CurrentUserProvider currentUserProvider, StudentService students,
            EnrollmentService enrollments, DocumentRequestService documentRequests, FileStorage fileStorage) {
        this.currentUserProvider = currentUserProvider;
        this.students = students;
        this.enrollments = enrollments;
        this.documentRequests = documentRequests;
        this.fileStorage = fileStorage;
    }

    @GetMapping("/profile")
    StudentProfileDto profile() {
        UUID studentId = currentStudentId();
        return new StudentProfileDto(StudentDto.from(students.get(studentId)),
                enrollments.currentClassOf(studentId).orElse(null));
    }

    @GetMapping("/document-requests")
    @Transactional(readOnly = true)
    List<DocumentRequestDto> myDocumentRequests() {
        return documentRequests.requestsOf(currentStudentId()).stream().map(DocumentRequestDto::from).toList();
    }

    /** "Nouvelle demande": a document type, a reason and an optional proof (PDF/JPG/PNG, 5 MB). */
    @PostMapping(value = "/document-requests", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    DocumentRequestDto requestDocument(@RequestParam UUID documentTypeId,
            @RequestParam(required = false) String reason, @RequestParam(required = false) MultipartFile proof) {
        UUID studentId = currentStudentId();
        UUID proofFileId = proof == null || proof.isEmpty() ? null
                : fileStorage.store(proof, FileStorage.MAX_PROOF_BYTES, FileStorage.PROOF_TYPES);
        return DocumentRequestDto.from(documentRequests.create(studentId, documentTypeId, reason, proofFileId));
    }

    private UUID currentStudentId() {
        CurrentUser user = currentUserProvider.require();
        if (!user.is(PersonType.STUDENT)) {
            throw new AccessDeniedException("Only students can use the student portal.");
        }
        return user.personId();
    }
}
