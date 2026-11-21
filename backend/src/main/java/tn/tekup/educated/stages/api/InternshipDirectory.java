package tn.tekup.educated.stages.api;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.stages.domain.DefenseRepository;

/** Read-only access for other modules (report cards include the published defense grades). */
@Service
@Transactional(readOnly = true)
public class InternshipDirectory {

    private final DefenseRepository defenses;

    InternshipDirectory(DefenseRepository defenses) {
        this.defenses = defenses;
    }

    public List<DefenseGrade> publishedGradesOf(UUID studentId) {
        return defenses.findAllByInternship_StudentIdAndPublishedTrue(studentId).stream()
                .map(d -> new DefenseGrade(d.getInternship().getId(), studentId,
                        d.getInternship().getAcademicYearId(), d.getInternship().getNature().getCode(), d.getGrade(),
                        d.getMention() == null ? null : d.getMention().name()))
                .toList();
    }
}
