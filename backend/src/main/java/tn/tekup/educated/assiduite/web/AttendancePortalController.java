package tn.tekup.educated.assiduite.web;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tn.tekup.educated.assiduite.service.AbsenceRequestService;
import tn.tekup.educated.assiduite.service.AttendanceService;
import tn.tekup.educated.assiduite.web.AttendanceController.RecordsRequest;
import tn.tekup.educated.assiduite.web.AttendanceViews.AbsenceRequestDto;
import tn.tekup.educated.assiduite.web.AttendanceViews.AbsencesDto;
import tn.tekup.educated.assiduite.web.AttendanceViews.SheetDto;
import tn.tekup.educated.shared.domain.PersonType;
import tn.tekup.educated.shared.files.FileStorage;
import tn.tekup.educated.shared.security.CurrentUserProvider;

/** Attendance in the portals: teachers take the call of their sessions, students follow their absences. */
@RestController
class AttendancePortalController {

    private final CurrentUserProvider currentUserProvider;
    private final AttendanceService attendance;
    private final AbsenceRequestService absenceRequests;
    private final AttendanceViews views;
    private final FileStorage fileStorage;

    AttendancePortalController(CurrentUserProvider currentUserProvider, AttendanceService attendance,
            AbsenceRequestService absenceRequests, AttendanceViews views, FileStorage fileStorage) {
        this.currentUserProvider = currentUserProvider;
        this.attendance = attendance;
        this.absenceRequests = absenceRequests;
        this.views = views;
        this.fileStorage = fileStorage;
    }

    @GetMapping("/api/teacher/sessions/{id}/attendance")
    @Transactional
    SheetDto sheet(@PathVariable UUID id) {
        return views.sheet(attendance.openSheet(id, teacherId()));
    }

    @PutMapping("/api/teacher/sessions/{id}/attendance")
    @Transactional
    SheetDto saveRecords(@PathVariable UUID id, @Valid @RequestBody RecordsRequest r) {
        return views.sheet(attendance.saveRecords(id, teacherId(), r.toData()));
    }

    @PostMapping("/api/teacher/sessions/{id}/attendance/confirm")
    @Transactional
    SheetDto confirm(@PathVariable UUID id) {
        return views.sheet(attendance.confirm(id, teacherId()));
    }

    @GetMapping("/api/student/absences")
    @Transactional(readOnly = true)
    AbsencesDto myAbsences() {
        return views.absences(studentId());
    }

    @GetMapping("/api/student/absence-requests")
    @Transactional(readOnly = true)
    List<AbsenceRequestDto> myAbsenceRequests() {
        return absenceRequests.requestsOf(studentId()).stream().map(views::request).toList();
    }

    /** Justifier une absence: the period, a reason and an optional proof (PDF/JPG/PNG, 5 MB). */
    @PostMapping(value = "/api/student/absence-requests", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    AbsenceRequestDto requestAbsence(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDateTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDateTime,
            @RequestParam String reason, @RequestParam(required = false) MultipartFile proof) {
        UUID studentId = studentId();
        UUID proofFileId = proof == null || proof.isEmpty() ? null
                : fileStorage.store(proof, FileStorage.MAX_PROOF_BYTES, FileStorage.PROOF_TYPES);
        return views.request(absenceRequests.submit(studentId, fromDateTime, toDateTime, reason, proofFileId));
    }

    private UUID teacherId() {
        return currentUserProvider.requirePerson(PersonType.TEACHER);
    }

    private UUID studentId() {
        return currentUserProvider.requirePerson(PersonType.STUDENT);
    }
}
