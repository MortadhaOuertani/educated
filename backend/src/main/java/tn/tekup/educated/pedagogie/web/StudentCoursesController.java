package tn.tekup.educated.pedagogie.web;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.tekup.educated.pedagogie.domain.CourseMaterial;
import tn.tekup.educated.pedagogie.domain.TeachingAssignment;
import tn.tekup.educated.pedagogie.service.CourseService;
import tn.tekup.educated.pedagogie.service.TeachingService;
import tn.tekup.educated.pedagogie.web.PedagogieDtos.AssignmentDto;
import tn.tekup.educated.pedagogie.web.PedagogieDtos.HomeworkDto;
import tn.tekup.educated.pedagogie.web.PedagogieDtos.LinkDto;
import tn.tekup.educated.pedagogie.web.PedagogieDtos.MaterialDto;
import tn.tekup.educated.scolarite.api.StudentDirectory;
import tn.tekup.educated.shared.domain.PersonType;
import tn.tekup.educated.shared.files.FileStorage;
import tn.tekup.educated.shared.security.CurrentUserProvider;
import tn.tekup.educated.shared.web.NotFoundException;

/** Student portal: subjects, course materials and homework of the student's current class. */
@RestController
@RequestMapping("/api/student")
class StudentCoursesController {

    private final CurrentUserProvider currentUserProvider;
    private final StudentDirectory students;
    private final TeachingService teaching;
    private final CourseService courses;
    private final FileStorage fileStorage;

    StudentCoursesController(CurrentUserProvider currentUserProvider, StudentDirectory students,
            TeachingService teaching, CourseService courses, FileStorage fileStorage) {
        this.currentUserProvider = currentUserProvider;
        this.students = students;
        this.teaching = teaching;
        this.courses = courses;
        this.fileStorage = fileStorage;
    }

    @GetMapping("/subjects")
    @Transactional(readOnly = true)
    List<AssignmentDto> subjects() {
        return myAssignments().stream().map(AssignmentDto::from).toList();
    }

    @GetMapping("/materials")
    @Transactional(readOnly = true)
    List<MaterialDto> materials() {
        return courses.visibleMaterials(myAssignmentIds()).stream().map(MaterialDto::from).toList();
    }

    /** A short-lived download link, only for materials of the student's class that are visible to students. */
    @GetMapping("/materials/{id}/link")
    @Transactional(readOnly = true)
    LinkDto materialLink(@PathVariable UUID id) {
        CourseMaterial material = courses.material(id);
        if (!material.isVisibleToStudents() || !myAssignmentIds().contains(material.getTeachingAssignment().getId())) {
            throw new NotFoundException("CourseMaterial", id);
        }
        return new LinkDto(fileStorage.downloadUrl(material.getFileId(), Duration.ofMinutes(10)));
    }

    @GetMapping("/homework")
    @Transactional(readOnly = true)
    List<HomeworkDto> homework() {
        return courses.homeworkOf(myAssignmentIds()).stream().map(HomeworkDto::from).toList();
    }

    private List<TeachingAssignment> myAssignments() {
        UUID studentId = currentUserProvider.requirePerson(PersonType.STUDENT);
        return students.currentClassId(studentId).map(teaching::assignmentsOfClass).orElse(List.of());
    }

    private List<UUID> myAssignmentIds() {
        return myAssignments().stream().map(TeachingAssignment::getId).toList();
    }
}
