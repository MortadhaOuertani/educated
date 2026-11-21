package tn.tekup.educated.stages.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.parametrage.api.ParametrageDirectory;
import tn.tekup.educated.shared.web.BusinessRuleException;
import tn.tekup.educated.shared.web.NotFoundException;
import tn.tekup.educated.stages.domain.Defense;
import tn.tekup.educated.stages.domain.DefenseRepository;
import tn.tekup.educated.stages.domain.DefenseSession;
import tn.tekup.educated.stages.domain.DefenseSessionRepository;
import tn.tekup.educated.stages.domain.DefenseStatus;
import tn.tekup.educated.stages.domain.Internship;
import tn.tekup.educated.stages.domain.InternshipStatus;
import tn.tekup.educated.stages.domain.Jury;
import tn.tekup.educated.stages.domain.JuryMember;
import tn.tekup.educated.stages.domain.JuryRepository;
import tn.tekup.educated.stages.domain.JuryRole;
import tn.tekup.educated.stages.domain.Mention;

/** Defense sessions, juries, the planning of defenses, their grades and publication. */
@Service
@Transactional
public class DefenseService {

    private static final BigDecimal TWENTY = BigDecimal.valueOf(20);
    private static final BigDecimal TEN = BigDecimal.TEN;

    public record SessionData(String name, UUID academicYearId, LocalDate startDate, LocalDate endDate,
            LocalDate requestsOpenFrom, LocalDate requestsOpenTo) {
    }

    public record MemberData(UUID teacherId, String externalName, JuryRole role) {
    }

    private final DefenseSessionRepository sessions;
    private final JuryRepository juries;
    private final DefenseRepository defenses;
    private final InternshipService internships;
    private final ParametrageDirectory parametrage;

    public DefenseService(DefenseSessionRepository sessions, JuryRepository juries, DefenseRepository defenses,
            InternshipService internships, ParametrageDirectory parametrage) {
        this.sessions = sessions;
        this.juries = juries;
        this.defenses = defenses;
        this.internships = internships;
        this.parametrage = parametrage;
    }

    @Transactional(readOnly = true)
    public List<DefenseSession> sessions() {
        return sessions.findAllByOrderByStartDateDesc();
    }

    public DefenseSession createSession(SessionData data) {
        if (parametrage.findAcademicYear(data.academicYearId()).isEmpty()) {
            throw new NotFoundException("AcademicYear", data.academicYearId());
        }
        if (data.endDate().isBefore(data.startDate())) {
            throw new BusinessRuleException("defenseSession.dates", "The session must end after it starts.");
        }
        DefenseSession session = new DefenseSession();
        session.setName(data.name());
        session.setAcademicYearId(data.academicYearId());
        session.setStartDate(data.startDate());
        session.setEndDate(data.endDate());
        session.setRequestsOpenFrom(data.requestsOpenFrom());
        session.setRequestsOpenTo(data.requestsOpenTo());
        return sessions.save(session);
    }

    @Transactional(readOnly = true)
    public List<Jury> juriesOf(UUID sessionId) {
        return juries.findAllByDefenseSession_IdOrderByJuryDate(sessionId);
    }

    @Transactional(readOnly = true)
    public List<Jury> juriesWithMember(UUID teacherId) {
        return juries.findAllWithMember(teacherId);
    }

    /** A jury has exactly one president and meets on one day of the session. */
    public Jury createJury(UUID sessionId, String code, LocalDate juryDate, UUID roomId, List<MemberData> members) {
        DefenseSession session = sessions.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("DefenseSession", sessionId));
        if (juries.findByCode(code).isPresent()) {
            throw new BusinessRuleException("jury.code", "Jury " + code + " already exists.");
        }
        if (juryDate.isBefore(session.getStartDate()) || juryDate.isAfter(session.getEndDate())) {
            throw new BusinessRuleException("jury.date", "The jury date must be within the defense session.");
        }
        long presidents = members.stream().filter(m -> m.role() == JuryRole.PRESIDENT).count();
        if (presidents != 1) {
            throw new BusinessRuleException("jury.president", "A jury needs exactly one president.");
        }
        Jury jury = new Jury();
        jury.setDefenseSession(session);
        jury.setCode(code);
        jury.setJuryDate(juryDate);
        jury.setRoomId(roomId);
        for (MemberData data : members) {
            if (data.teacherId() == null && (data.externalName() == null || data.externalName().isBlank())) {
                throw new BusinessRuleException("jury.member", "Each member is a teacher or a named external member.");
            }
            JuryMember member = new JuryMember();
            member.setJury(jury);
            member.setTeacherId(data.teacherId());
            member.setExternalName(data.externalName());
            member.setRole(data.role());
            jury.getMembers().add(member);
        }
        return juries.save(jury);
    }

    @Transactional(readOnly = true)
    public List<Defense> defensesOf(UUID juryId) {
        return defenses.findAllByJury_IdOrderByStartTime(juryId);
    }

    @Transactional(readOnly = true)
    public Optional<Defense> defenseOf(UUID internshipId) {
        return defenses.findByInternship_Id(internshipId);
    }

    /** Plans the defense of an authorized internship on the jury's day, without overlapping another defense. */
    public Defense schedule(UUID internshipId, UUID juryId, LocalTime startTime, LocalTime endTime) {
        Internship internship = internships.internship(internshipId);
        if (internship.getStatus() != InternshipStatus.AUTHORIZED_TO_DEFEND) {
            throw new BusinessRuleException("defense.notAuthorized", "The internship is not authorized to defend yet.");
        }
        if (defenses.findByInternship_Id(internshipId).isPresent()) {
            throw new BusinessRuleException("defense.duplicate", "This internship already has a defense.");
        }
        if (!endTime.isAfter(startTime)) {
            throw new BusinessRuleException("defense.time", "The defense must end after it starts.");
        }
        Jury jury = juries.findById(juryId).orElseThrow(() -> new NotFoundException("Jury", juryId));
        boolean overlaps = defenses.findAllByJury_IdOrderByStartTime(juryId).stream()
                .anyMatch(d -> d.getStartTime().isBefore(endTime) && d.getEndTime().isAfter(startTime));
        if (overlaps) {
            throw new BusinessRuleException("defense.overlap", "The jury already has a defense at this time.");
        }
        Defense defense = new Defense();
        defense.setInternship(internship);
        defense.setJury(jury);
        defense.setDefenseDate(jury.getJuryDate());
        defense.setStartTime(startTime);
        defense.setEndTime(endTime);
        defense.setStatus(DefenseStatus.PLANNED);
        return defenses.save(defense);
    }

    /** Records the grade (out of 20) after the defense; the mention follows from the grade. */
    public Defense recordGrade(UUID defenseId, BigDecimal grade) {
        Defense defense = defense(defenseId);
        if (defense.isPublished()) {
            throw new BusinessRuleException("defense.published", "A published grade cannot be changed.");
        }
        if (grade == null || grade.signum() < 0 || grade.compareTo(TWENTY) > 0) {
            throw new BusinessRuleException("defense.grade", "The grade must be between 0 and 20.");
        }
        defense.setGrade(grade);
        defense.setMention(mentionFor(grade));
        if (defense.getStatus() != DefenseStatus.HELD) {
            defense.setStatus(DefenseStatus.HELD);
            internships.move(defense.getInternship(), InternshipStatus.DEFENDED, null);
        }
        return defense;
    }

    /** Publishes the grade to the student; a passing grade validates the internship. */
    public Defense publish(UUID defenseId) {
        Defense defense = defense(defenseId);
        if (defense.getGrade() == null) {
            throw new BusinessRuleException("defense.noGrade", "Record the grade before publishing it.");
        }
        if (defense.isPublished()) {
            throw new BusinessRuleException("defense.published", "This grade is already published.");
        }
        defense.setGradeValidated(true);
        defense.setPublished(true);
        defense.setPublishedAt(Instant.now());
        if (defense.getGrade().compareTo(TEN) >= 0) {
            internships.move(defense.getInternship(), InternshipStatus.VALIDATED, null);
        }
        return defense;
    }

    /** Tunisian mentions: 10 Passable, 12 Assez bien, 14 Bien, 16 Très bien, 18 Excellent. */
    static Mention mentionFor(BigDecimal grade) {
        if (grade.compareTo(BigDecimal.valueOf(18)) >= 0) {
            return Mention.EXCELLENT;
        }
        if (grade.compareTo(BigDecimal.valueOf(16)) >= 0) {
            return Mention.VERY_GOOD;
        }
        if (grade.compareTo(BigDecimal.valueOf(14)) >= 0) {
            return Mention.GOOD;
        }
        if (grade.compareTo(BigDecimal.valueOf(12)) >= 0) {
            return Mention.FAIRLY_GOOD;
        }
        return grade.compareTo(TEN) >= 0 ? Mention.PASS : null;
    }

    private Defense defense(UUID id) {
        return defenses.findById(id).orElseThrow(() -> new NotFoundException("Defense", id));
    }
}
