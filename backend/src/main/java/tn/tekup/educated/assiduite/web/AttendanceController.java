package tn.tekup.educated.assiduite.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.tekup.educated.assiduite.domain.AttendancePolicy;
import tn.tekup.educated.assiduite.domain.AttendanceStatus;
import tn.tekup.educated.assiduite.domain.RequestStatus;
import tn.tekup.educated.assiduite.service.AbsenceRequestService;
import tn.tekup.educated.assiduite.service.AttendanceService;
import tn.tekup.educated.assiduite.service.AttendanceService.RecordData;
import tn.tekup.educated.assiduite.service.TeacherHoursService;
import tn.tekup.educated.assiduite.web.AttendanceViews.AbsenceRequestDto;
import tn.tekup.educated.assiduite.web.AttendanceViews.AbsencesDto;
import tn.tekup.educated.assiduite.web.AttendanceViews.SheetDto;
import tn.tekup.educated.assiduite.web.AttendanceViews.TeacherHoursDto;

/** Back-office screens of the attendance part of Scolarité (appels, absences, présence des enseignants). */
@RestController
@RequestMapping("/api/assiduite")
class AttendanceController {

    record RecordRequest(@NotNull UUID studentId, @NotNull AttendanceStatus status, Integer lateMinutes,
            String comment) {
    }

    record RecordsRequest(@NotNull List<@Valid RecordRequest> records) {
        List<RecordData> toData() {
            return records.stream().map(r -> new RecordData(r.studentId(), r.status(), r.lateMinutes(), r.comment()))
                    .toList();
        }
    }

    record ReviewRequest(@NotNull RequestStatus status) {
    }

    record PolicyDto(BigDecimal atRiskPercent, BigDecimal criticalPercent, Integer callDeadlineMinutes,
            Integer editWindowHours) {
        static PolicyDto from(AttendancePolicy p) {
            return new PolicyDto(p.getAtRiskPercent(), p.getCriticalPercent(), p.getCallDeadlineMinutes(),
                    p.getEditWindowHours());
        }
    }

    private final AttendanceService attendance;
    private final AbsenceRequestService absenceRequests;
    private final TeacherHoursService teacherHours;
    private final AttendanceViews views;

    AttendanceController(AttendanceService attendance, AbsenceRequestService absenceRequests,
            TeacherHoursService teacherHours, AttendanceViews views) {
        this.attendance = attendance;
        this.absenceRequests = absenceRequests;
        this.teacherHours = teacherHours;
        this.views = views;
    }

    @GetMapping("/policy")
    @PreAuthorize("@perm.can('AttendancePolicy', 'READ')")
    PolicyDto policy() {
        return PolicyDto.from(attendance.policy());
    }

    @GetMapping("/sessions/{id}/attendance")
    @PreAuthorize("@perm.can('AttendanceSheet', 'READ')")
    @Transactional
    SheetDto sheet(@PathVariable UUID id) {
        return views.sheet(attendance.openSheet(id, null));
    }

    @PutMapping("/sessions/{id}/attendance")
    @PreAuthorize("@perm.can('AttendanceRecord', 'WRITE')")
    @Transactional
    SheetDto saveRecords(@PathVariable UUID id, @Valid @RequestBody RecordsRequest r) {
        return views.sheet(attendance.saveRecords(id, null, r.toData()));
    }

    @PostMapping("/sessions/{id}/attendance/confirm")
    @PreAuthorize("@perm.can('AttendanceSheet', 'WRITE')")
    @Transactional
    SheetDto confirm(@PathVariable UUID id) {
        return views.sheet(attendance.confirm(id, null));
    }

    @GetMapping("/students/{id}/absences")
    @PreAuthorize("@perm.can('AttendanceRecord', 'READ')")
    @Transactional(readOnly = true)
    AbsencesDto absences(@PathVariable UUID id) {
        return views.absences(id);
    }

    @GetMapping("/absence-requests")
    @PreAuthorize("@perm.can('StudentAbsenceRequest', 'READ')")
    @Transactional(readOnly = true)
    List<AbsenceRequestDto> absenceRequests(@RequestParam(required = false) RequestStatus status) {
        return absenceRequests.list(status).stream().map(views::request).toList();
    }

    @PatchMapping("/absence-requests/{id}")
    @PreAuthorize("@perm.can('StudentAbsenceRequest', 'WRITE')")
    @Transactional
    AbsenceRequestDto review(@PathVariable UUID id, @Valid @RequestBody ReviewRequest r) {
        return views.request(absenceRequests.review(id, r.status()));
    }

    @GetMapping("/teacher-hours")
    @PreAuthorize("@perm.can('TeacherAttendance', 'READ')")
    List<TeacherHoursDto> teacherHours(@RequestParam UUID teacherId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return teacherHours.hoursOf(teacherId, from, to).stream().map(TeacherHoursDto::from).toList();
    }

    @PostMapping("/teacher-hours/{id}/validate")
    @PreAuthorize("@perm.can('TeacherAttendance', 'WRITE')")
    TeacherHoursDto validate(@PathVariable UUID id) {
        return TeacherHoursDto.from(teacherHours.validate(id));
    }
}
