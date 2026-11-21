package tn.tekup.educated.pedagogie.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.pedagogie.domain.CourseJournalEntry;
import tn.tekup.educated.pedagogie.domain.CourseJournalEntryRepository;
import tn.tekup.educated.pedagogie.domain.CourseMaterial;
import tn.tekup.educated.pedagogie.domain.CourseMaterialRepository;
import tn.tekup.educated.pedagogie.domain.Homework;
import tn.tekup.educated.pedagogie.domain.HomeworkRepository;
import tn.tekup.educated.pedagogie.domain.MaterialType;
import tn.tekup.educated.pedagogie.domain.TeachingAssignment;
import tn.tekup.educated.shared.security.CurrentUserProvider;
import tn.tekup.educated.shared.web.BusinessRuleException;
import tn.tekup.educated.shared.web.NotFoundException;

/** What happens in class: course materials, the course journal (cahier de texte) and homework. */
@Service
@Transactional
public class CourseService {

    public record MaterialData(String title, MaterialType materialType, String description, UUID fileId,
            boolean visibleToStudents) {
    }

    public record HomeworkData(String title, String description, LocalDate assignedDate, LocalDate dueDate,
            UUID sessionOccurrenceId) {
    }

    private final CourseMaterialRepository materials;
    private final CourseJournalEntryRepository journal;
    private final HomeworkRepository homework;
    private final CurrentUserProvider currentUserProvider;

    public CourseService(CourseMaterialRepository materials, CourseJournalEntryRepository journal,
            HomeworkRepository homework, CurrentUserProvider currentUserProvider) {
        this.materials = materials;
        this.journal = journal;
        this.homework = homework;
        this.currentUserProvider = currentUserProvider;
    }

    public CourseMaterial addMaterial(TeachingAssignment assignment, MaterialData data) {
        CourseMaterial material = new CourseMaterial();
        material.setTeachingAssignment(assignment);
        material.setTitle(data.title());
        material.setMaterialType(data.materialType());
        material.setDescription(data.description());
        material.setFileId(data.fileId());
        material.setVisibleToStudents(data.visibleToStudents());
        material.setPublishedAt(data.visibleToStudents() ? Instant.now() : null);
        return materials.save(material);
    }

    @Transactional(readOnly = true)
    public List<CourseMaterial> materialsOf(UUID assignmentId) {
        return materials.findAllByTeachingAssignment_IdOrderByCreatedAtDesc(assignmentId);
    }

    @Transactional(readOnly = true)
    public List<CourseMaterial> visibleMaterials(Collection<UUID> assignmentIds) {
        return assignmentIds.isEmpty() ? List.of()
                : materials.findAllByTeachingAssignment_IdInAndVisibleToStudentsTrueOrderByCreatedAtDesc(assignmentIds);
    }

    @Transactional(readOnly = true)
    public CourseMaterial material(UUID id) {
        return materials.findById(id).orElseThrow(() -> new NotFoundException("CourseMaterial", id));
    }

    /** One journal entry per session, confirmed by the teacher when written. */
    public CourseJournalEntry writeJournal(TeachingAssignment assignment, UUID sessionOccurrenceId, String content) {
        if (journal.existsBySessionOccurrenceId(sessionOccurrenceId)) {
            throw new BusinessRuleException("journal.duplicate", "This session already has a journal entry.");
        }
        CourseJournalEntry entry = new CourseJournalEntry();
        entry.setTeachingAssignment(assignment);
        entry.setSessionOccurrenceId(sessionOccurrenceId);
        entry.setContent(content);
        entry.setConfirmed(true);
        entry.setConfirmedAt(Instant.now());
        entry.setConfirmedByUserId(currentUserProvider.require().userId());
        return journal.save(entry);
    }

    @Transactional(readOnly = true)
    public List<CourseJournalEntry> journalOf(UUID assignmentId) {
        return journal.findAllByTeachingAssignment_IdOrderByCreatedAtDesc(assignmentId);
    }

    public Homework giveHomework(TeachingAssignment assignment, HomeworkData data) {
        if (data.dueDate() != null && data.dueDate().isBefore(data.assignedDate())) {
            throw new BusinessRuleException("homework.dueDate", "The due date is before the assigned date.");
        }
        Homework item = new Homework();
        item.setTeachingAssignment(assignment);
        item.setTitle(data.title());
        item.setDescription(data.description());
        item.setAssignedDate(data.assignedDate());
        item.setDueDate(data.dueDate());
        item.setSessionOccurrenceId(data.sessionOccurrenceId());
        item.setConfirmed(true);
        return homework.save(item);
    }

    @Transactional(readOnly = true)
    public List<Homework> homeworkOf(UUID assignmentId) {
        return homework.findAllByTeachingAssignment_IdOrderByAssignedDateDesc(assignmentId);
    }

    @Transactional(readOnly = true)
    public List<Homework> homeworkOf(Collection<UUID> assignmentIds) {
        return assignmentIds.isEmpty() ? List.of() : homework.findAllByTeachingAssignment_IdInOrderByDueDateAsc(assignmentIds);
    }
}
