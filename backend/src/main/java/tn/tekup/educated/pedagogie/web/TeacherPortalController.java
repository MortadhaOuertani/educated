package tn.tekup.educated.pedagogie.web;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tn.tekup.educated.parametrage.api.AcademicPeriodInfo;
import tn.tekup.educated.parametrage.api.ParametrageDirectory;
import tn.tekup.educated.pedagogie.domain.MaterialType;
import tn.tekup.educated.pedagogie.domain.TeachingAssignment;
import tn.tekup.educated.pedagogie.service.CourseService;
import tn.tekup.educated.pedagogie.service.CourseService.HomeworkData;
import tn.tekup.educated.pedagogie.service.CourseService.MaterialData;
import tn.tekup.educated.pedagogie.service.MakeUpService;
import tn.tekup.educated.pedagogie.service.MakeUpService.MakeUpData;
import tn.tekup.educated.pedagogie.service.TeachingService;
import tn.tekup.educated.pedagogie.web.PedagogieDtos.AssignmentDto;
import tn.tekup.educated.pedagogie.web.PedagogieDtos.HomeworkDto;
import tn.tekup.educated.pedagogie.web.PedagogieDtos.HomeworkRequest;
import tn.tekup.educated.pedagogie.web.PedagogieDtos.JournalDto;
import tn.tekup.educated.pedagogie.web.PedagogieDtos.JournalRequest;
import tn.tekup.educated.pedagogie.web.PedagogieDtos.MakeUpDto;
import tn.tekup.educated.pedagogie.web.PedagogieDtos.MakeUpRequestBody;
import tn.tekup.educated.pedagogie.web.PedagogieDtos.MaterialDto;
import tn.tekup.educated.pedagogie.web.PedagogieDtos.TeacherDto;
import tn.tekup.educated.shared.domain.PersonType;
import tn.tekup.educated.shared.files.FileStorage;
import tn.tekup.educated.shared.security.CurrentUserProvider;

/** Teacher portal: a teacher only reaches the classes and subjects they teach. */
@RestController
@RequestMapping("/api/teacher")
class TeacherPortalController {

    private final CurrentUserProvider currentUserProvider;
    private final TeachingService teaching;
    private final CourseService courses;
    private final MakeUpService makeUps;
    private final FileStorage fileStorage;
    private final ParametrageDirectory parametrage;

    TeacherPortalController(CurrentUserProvider currentUserProvider, TeachingService teaching, CourseService courses,
            MakeUpService makeUps, FileStorage fileStorage, ParametrageDirectory parametrage) {
        this.currentUserProvider = currentUserProvider;
        this.teaching = teaching;
        this.courses = courses;
        this.makeUps = makeUps;
        this.fileStorage = fileStorage;
        this.parametrage = parametrage;
    }

    /** Semesters of the current academic year (grades are entered per semester). */
    @GetMapping("/periods")
    List<AcademicPeriodInfo> periods() {
        teacherId();
        return parametrage.currentAcademicYear().map(year -> parametrage.periodsOf(year.id())).orElse(List.of());
    }

    @GetMapping("/profile")
    @Transactional(readOnly = true)
    TeacherDto profile() {
        return TeacherDto.from(teaching.teacher(teacherId()));
    }

    @GetMapping("/assignments")
    @Transactional(readOnly = true)
    List<AssignmentDto> assignments() {
        return teaching.assignmentsOfTeacher(teacherId()).stream().map(AssignmentDto::from).toList();
    }

    @GetMapping("/assignments/{id}/materials")
    @Transactional(readOnly = true)
    List<MaterialDto> materials(@PathVariable UUID id) {
        myAssignment(id);
        return courses.materialsOf(id).stream().map(MaterialDto::from).toList();
    }

    /** Dépôt d'un support de cours (PDF, slides, documents, archive; 50 MB at most). */
    @PostMapping(value = "/assignments/{id}/materials", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    MaterialDto addMaterial(@PathVariable UUID id, @RequestParam String title, @RequestParam MaterialType materialType,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "true") boolean visibleToStudents, @RequestParam MultipartFile file) {
        TeachingAssignment assignment = myAssignment(id);
        UUID fileId = fileStorage.store(file, FileStorage.MAX_MATERIAL_BYTES, FileStorage.DOCUMENT_TYPES);
        return MaterialDto.from(courses.addMaterial(assignment,
                new MaterialData(title, materialType, description, fileId, visibleToStudents)));
    }

    @GetMapping("/assignments/{id}/journal")
    @Transactional(readOnly = true)
    List<JournalDto> journal(@PathVariable UUID id) {
        myAssignment(id);
        return courses.journalOf(id).stream().map(JournalDto::from).toList();
    }

    @PostMapping("/assignments/{id}/journal")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    JournalDto writeJournal(@PathVariable UUID id, @Valid @RequestBody JournalRequest r) {
        return JournalDto.from(courses.writeJournal(myAssignment(id), r.sessionOccurrenceId(), r.content()));
    }

    @GetMapping("/assignments/{id}/homework")
    @Transactional(readOnly = true)
    List<HomeworkDto> homework(@PathVariable UUID id) {
        myAssignment(id);
        return courses.homeworkOf(id).stream().map(HomeworkDto::from).toList();
    }

    @PostMapping("/assignments/{id}/homework")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    HomeworkDto giveHomework(@PathVariable UUID id, @Valid @RequestBody HomeworkRequest r) {
        return HomeworkDto.from(courses.giveHomework(myAssignment(id), new HomeworkData(r.title(), r.description(),
                r.assignedDate(), r.dueDate(), r.sessionOccurrenceId())));
    }

    @GetMapping("/make-up-requests")
    @Transactional(readOnly = true)
    List<MakeUpDto> makeUpRequests() {
        return makeUps.requestsOfTeacher(teacherId()).stream().map(MakeUpDto::from).toList();
    }

    @PostMapping("/make-up-requests")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    MakeUpDto requestMakeUp(@Valid @RequestBody MakeUpRequestBody r) {
        TeachingAssignment assignment = myAssignment(r.teachingAssignmentId());
        return MakeUpDto.from(makeUps.request(assignment, new MakeUpData(r.originalOccurrenceId(), r.requestedDate(),
                r.startTime(), r.endTime(), r.roomId(), r.reason())));
    }

    private UUID teacherId() {
        return currentUserProvider.requirePerson(PersonType.TEACHER);
    }

    private TeachingAssignment myAssignment(UUID assignmentId) {
        return teaching.assignmentOfTeacher(assignmentId, teacherId());
    }
}
