package tn.tekup.educated.examens.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.examens.api.ExamCalendarPublished;
import tn.tekup.educated.examens.domain.Exam;
import tn.tekup.educated.examens.domain.ExamClass;
import tn.tekup.educated.examens.domain.ExamRepository;
import tn.tekup.educated.examens.domain.ExamRoomAllocation;
import tn.tekup.educated.examens.domain.ExamSession;
import tn.tekup.educated.examens.domain.ExamSessionRepository;
import tn.tekup.educated.examens.domain.ExamSessionType;
import tn.tekup.educated.examens.domain.Invigilation;
import tn.tekup.educated.examens.domain.InvigilationRepository;
import tn.tekup.educated.examens.domain.InvigilationRole;
import tn.tekup.educated.parametrage.api.ParametrageDirectory;
import tn.tekup.educated.pedagogie.api.TeachingDirectory;
import tn.tekup.educated.shared.domain.AssessmentType;
import tn.tekup.educated.shared.web.BusinessRuleException;
import tn.tekup.educated.shared.web.NotFoundException;

/** Exam sessions and the exam calendar: classes, rooms and invigilators of each exam. */
@Service
@Transactional
public class ExamService {

    public record RoomData(UUID roomId, UUID schoolClassId, Integer seatCount) {
    }

    public record InvigilatorData(UUID teacherId, UUID roomId, InvigilationRole role) {
    }

    public record ExamData(UUID studyPlanSubjectId, AssessmentType assessmentType, LocalDate examDate,
            LocalTime startTime, LocalTime endTime, List<UUID> schoolClassIds, List<RoomData> rooms,
            List<InvigilatorData> invigilators) {
    }

    private final ExamSessionRepository sessions;
    private final ExamRepository exams;
    private final InvigilationRepository invigilations;
    private final ParametrageDirectory parametrage;
    private final TeachingDirectory teaching;
    private final ApplicationEventPublisher events;

    public ExamService(ExamSessionRepository sessions, ExamRepository exams, InvigilationRepository invigilations,
            ParametrageDirectory parametrage, TeachingDirectory teaching, ApplicationEventPublisher events) {
        this.sessions = sessions;
        this.exams = exams;
        this.invigilations = invigilations;
        this.parametrage = parametrage;
        this.teaching = teaching;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<ExamSession> sessions() {
        return sessions.findAllByOrderByStartDateDesc();
    }

    @Transactional(readOnly = true)
    public ExamSession session(UUID id) {
        return sessions.findById(id).orElseThrow(() -> new NotFoundException("ExamSession", id));
    }

    public ExamSession createSession(String name, ExamSessionType type, UUID academicPeriodId, LocalDate startDate,
            LocalDate endDate) {
        if (parametrage.findAcademicPeriod(academicPeriodId).isEmpty()) {
            throw new NotFoundException("AcademicPeriod", academicPeriodId);
        }
        if (endDate.isBefore(startDate)) {
            throw new BusinessRuleException("examSession.dates", "The session must end after it starts.");
        }
        ExamSession session = new ExamSession();
        session.setName(name);
        session.setSessionType(type);
        session.setAcademicPeriodId(academicPeriodId);
        session.setStartDate(startDate);
        session.setEndDate(endDate);
        return sessions.save(session);
    }

    @Transactional(readOnly = true)
    public List<Exam> examsOf(UUID sessionId) {
        return exams.findAllByExamSession_IdOrderByExamDateAscStartTimeAsc(sessionId);
    }

    /** Adds an exam. A room or an invigilator cannot be used by two exams at the same time. */
    public Exam createExam(UUID sessionId, ExamData data) {
        ExamSession session = session(sessionId);
        if (session.isPublished()) {
            throw new BusinessRuleException("examSession.published", "The calendar of this session is already published.");
        }
        if (data.examDate().isBefore(session.getStartDate()) || data.examDate().isAfter(session.getEndDate())) {
            throw new BusinessRuleException("exam.date", "The exam date must be within the session.");
        }
        if (!data.endTime().isAfter(data.startTime())) {
            throw new BusinessRuleException("exam.time", "The exam must end after it starts.");
        }
        if (teaching.findStudyPlanSubject(data.studyPlanSubjectId()).isEmpty()) {
            throw new NotFoundException("StudyPlanSubject", data.studyPlanSubjectId());
        }
        for (UUID classId : data.schoolClassIds()) {
            if (parametrage.findSchoolClass(classId).isEmpty()) {
                throw new NotFoundException("SchoolClass", classId);
            }
        }
        List<RoomData> rooms = data.rooms() == null ? List.of() : data.rooms();
        List<InvigilatorData> invigilators = data.invigilators() == null ? List.of() : data.invigilators();
        for (Exam other : exams.findAllByExamDate(data.examDate())) {
            boolean overlaps = other.getStartTime().isBefore(data.endTime()) && other.getEndTime().isAfter(data.startTime());
            if (!overlaps) {
                continue;
            }
            for (RoomData room : rooms) {
                if (other.getRooms().stream().anyMatch(r -> r.getRoomId().equals(room.roomId()))) {
                    throw new BusinessRuleException("exam.roomBusy", "A room is already used by another exam at this time.");
                }
            }
            for (InvigilatorData invigilator : invigilators) {
                if (other.getInvigilations().stream().anyMatch(i -> i.getTeacherId().equals(invigilator.teacherId()))) {
                    throw new BusinessRuleException("exam.invigilatorBusy",
                            "An invigilator already watches another exam at this time.");
                }
            }
        }

        Exam exam = new Exam();
        exam.setExamSession(session);
        exam.setStudyPlanSubjectId(data.studyPlanSubjectId());
        exam.setAssessmentType(data.assessmentType());
        exam.setExamDate(data.examDate());
        exam.setStartTime(data.startTime());
        exam.setEndTime(data.endTime());
        exam.setDurationMinutes((int) Duration.between(data.startTime(), data.endTime()).toMinutes());
        for (UUID classId : new LinkedHashSet<>(data.schoolClassIds())) {
            ExamClass examClass = new ExamClass();
            examClass.setExam(exam);
            examClass.setSchoolClassId(classId);
            exam.getClasses().add(examClass);
        }
        for (RoomData roomData : rooms) {
            if (parametrage.findRoom(roomData.roomId()).isEmpty()) {
                throw new NotFoundException("Room", roomData.roomId());
            }
            ExamRoomAllocation room = new ExamRoomAllocation();
            room.setExam(exam);
            room.setRoomId(roomData.roomId());
            room.setSchoolClassId(roomData.schoolClassId());
            room.setSeatCount(roomData.seatCount());
            exam.getRooms().add(room);
        }
        for (InvigilatorData invigilatorData : invigilators) {
            if (teaching.findTeacher(invigilatorData.teacherId()).isEmpty()) {
                throw new NotFoundException("Teacher", invigilatorData.teacherId());
            }
            Invigilation invigilation = new Invigilation();
            invigilation.setExam(exam);
            invigilation.setTeacherId(invigilatorData.teacherId());
            invigilation.setRoomId(invigilatorData.roomId());
            invigilation.setRole(invigilatorData.role() == null ? InvigilationRole.INVIGILATOR : invigilatorData.role());
            exam.getInvigilations().add(invigilation);
        }
        session.getExams().add(exam);
        return exams.save(exam);
    }

    /** Publishes the calendar, the rooms and the invigilation duties of the whole session. */
    public ExamSession publish(UUID sessionId) {
        ExamSession session = session(sessionId);
        if (session.isPublished()) {
            throw new BusinessRuleException("examSession.published", "This calendar is already published.");
        }
        if (session.getExams().isEmpty()) {
            throw new BusinessRuleException("examSession.empty", "Add exams before publishing the calendar.");
        }
        Set<UUID> classIds = new LinkedHashSet<>();
        Set<UUID> teacherIds = new LinkedHashSet<>();
        session.setPublished(true);
        for (Exam exam : session.getExams()) {
            exam.setPublished(true);
            exam.setRoomsPublished(true);
            exam.getClasses().forEach(c -> classIds.add(c.getSchoolClassId()));
            exam.getInvigilations().forEach(i -> {
                i.setPublished(true);
                teacherIds.add(i.getTeacherId());
            });
        }
        events.publishEvent(new ExamCalendarPublished(session.getEstablishmentId(), session.getId(), session.getName(),
                new ArrayList<>(classIds), new ArrayList<>(teacherIds)));
        return session;
    }

    @Transactional(readOnly = true)
    public List<Exam> publishedExamsOf(UUID schoolClassId) {
        return exams.findPublishedForClass(schoolClassId);
    }

    @Transactional(readOnly = true)
    public List<Invigilation> invigilationsOf(UUID teacherId) {
        return invigilations.findAllByTeacherIdAndPublishedTrueOrderByExam_ExamDateAscExam_StartTimeAsc(teacherId);
    }
}
