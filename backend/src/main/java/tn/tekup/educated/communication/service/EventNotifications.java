package tn.tekup.educated.communication.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import tn.tekup.educated.assiduite.api.AbsencesRecorded;
import tn.tekup.educated.communication.domain.NotificationType;
import tn.tekup.educated.emploidutemps.api.SessionCancelled;
import tn.tekup.educated.emploidutemps.api.SessionScheduled;
import tn.tekup.educated.examens.api.ExamCalendarPublished;
import tn.tekup.educated.examens.api.GradesPublished;
import tn.tekup.educated.examens.api.ReportCardsPublished;
import tn.tekup.educated.finances.api.PaymentReceived;
import tn.tekup.educated.finances.api.PaymentReminderSent;
import tn.tekup.educated.pedagogie.api.MakeUpConfirmed;
import tn.tekup.educated.pedagogie.api.TeachingAssignmentInfo;
import tn.tekup.educated.pedagogie.api.TeachingDirectory;
import tn.tekup.educated.scolarite.api.DocumentRequestStatusChanged;
import tn.tekup.educated.scolarite.api.SanctionIssued;
import tn.tekup.educated.scolarite.api.StudentDirectory;
import tn.tekup.educated.shared.domain.PersonType;
import tn.tekup.educated.shared.tenant.TenantContext;
import tn.tekup.educated.stages.api.InternshipStatusChanged;

/**
 * Turns what happens in the other modules into notifications. Each listener runs after the other module's
 * transaction is committed, in its own transaction, inside the establishment the event belongs to.
 */
@Component
class EventNotifications {

    private final NotificationService notifications;
    private final StudentDirectory students;
    private final TeachingDirectory teaching;

    EventNotifications(NotificationService notifications, StudentDirectory students, TeachingDirectory teaching) {
        this.notifications = notifications;
        this.students = students;
        this.teaching = teaching;
    }

    @TransactionalEventListener
    void on(DocumentRequestStatusChanged e) {
        toStudents(e.establishmentId(), List.of(e.studentId()), NotificationType.DOCUMENT, "Demande de document",
                "Votre demande est maintenant : " + documentStatus(e.status()) + ".", "DocumentRequest", e.requestId());
    }

    @TransactionalEventListener
    void on(SanctionIssued e) {
        toStudents(e.establishmentId(), List.of(e.studentId()), NotificationType.OTHER, "Sanction",
                "Une sanction a été enregistrée dans votre dossier.", "Sanction", e.sanctionId());
    }

    @TransactionalEventListener
    void on(AbsencesRecorded e) {
        String subject = subjectName(e.establishmentId(), e.teachingAssignmentId());
        toStudents(e.establishmentId(), e.absentStudentIds(), NotificationType.ABSENCE, "Absence",
                "Vous avez été marqué(e) absent(e) en " + subject + " le " + e.date() + ".", "Session", e.sessionId());
        toStudents(e.establishmentId(), e.lateStudentIds(), NotificationType.ABSENCE, "Retard",
                "Vous avez été marqué(e) en retard en " + subject + " le " + e.date() + ".", "Session", e.sessionId());
    }

    @TransactionalEventListener
    void on(SessionScheduled e) {
        String subject = subjectName(e.establishmentId(), e.teachingAssignmentId());
        toClass(e.establishmentId(), e.schoolClassId(), NotificationType.OTHER, "Séance de rattrapage",
                subject + " : séance le " + e.date() + " à " + e.startTime() + ".", "Session", e.sessionId());
    }

    @TransactionalEventListener
    void on(SessionCancelled e) {
        String subject = subjectName(e.establishmentId(), e.teachingAssignmentId());
        String reason = e.reason() == null || e.reason().isBlank() ? "" : " (" + e.reason() + ")";
        toClass(e.establishmentId(), e.schoolClassId(), NotificationType.OTHER, "Séance annulée",
                subject + " : la séance du " + e.date() + " est annulée" + reason + ".", "Session", e.sessionId());
    }

    @TransactionalEventListener
    void on(MakeUpConfirmed e) {
        TenantContext.runAs(e.establishmentId(), () -> teaching.findAssignment(e.teachingAssignmentId())
                .map(TeachingAssignmentInfo::teacherId)
                .ifPresent(teacherId -> notifyInNewTransaction(PersonType.TEACHER, List.of(teacherId),
                        NotificationType.REQUEST, "Rattrapage confirmé",
                        "Votre séance de rattrapage du " + e.date() + " est confirmée.", "MakeUpRequest",
                        e.makeUpRequestId())));
    }

    @TransactionalEventListener
    void on(GradesPublished e) {
        toClass(e.establishmentId(), e.schoolClassId(), NotificationType.GRADE, "Nouvelles notes",
                "Des notes viennent d'être publiées.", "GradeGrid", e.gradeGridId());
    }

    @TransactionalEventListener
    void on(ReportCardsPublished e) {
        toStudents(e.establishmentId(), e.studentIds(), NotificationType.GRADE, "Bulletin disponible",
                "Votre bulletin est disponible.", "ReportCard", null);
    }

    @TransactionalEventListener
    void on(ExamCalendarPublished e) {
        List<UUID> studentIds = new ArrayList<>();
        TenantContext.runAs(e.establishmentId(), () -> e.schoolClassIds()
                .forEach(classId -> studentIds.addAll(students.studentIdsInClass(classId))));
        toStudents(e.establishmentId(), studentIds, NotificationType.OTHER, "Calendrier des examens",
                "Le calendrier « " + e.name() + " » est publié.", "ExamSession", e.examSessionId());
        TenantContext.runAs(e.establishmentId(), () -> notifyInNewTransaction(PersonType.TEACHER,
                e.invigilatorTeacherIds(), NotificationType.INVIGILATION, "Surveillances",
                "Vos surveillances de la session « " + e.name() + " » sont publiées.", "ExamSession",
                e.examSessionId()));
    }

    @TransactionalEventListener
    void on(InternshipStatusChanged e) {
        toStudents(e.establishmentId(), List.of(e.studentId()), NotificationType.INTERNSHIP, "Stage",
                "Votre stage est maintenant : " + e.status() + ".", "Internship", e.internshipId());
        if ("DEFENSE_REQUESTED".equals(e.status()) && e.academicSupervisorId() != null) {
            TenantContext.runAs(e.establishmentId(), () -> notifyInNewTransaction(PersonType.TEACHER,
                    List.of(e.academicSupervisorId()), NotificationType.INTERNSHIP, "Demande de soutenance",
                    "Un étudiant que vous encadrez demande à soutenir.", "Internship", e.internshipId()));
        }
    }

    @TransactionalEventListener
    void on(PaymentReceived e) {
        toStudents(e.establishmentId(), List.of(e.studentId()), NotificationType.OTHER, "Paiement reçu",
                "Paiement de " + e.amount() + " TND reçu (reçu " + e.receiptNumber() + ").", "Payment", e.paymentId());
    }

    @TransactionalEventListener
    void on(PaymentReminderSent e) {
        String message = e.message() == null || e.message().isBlank()
                ? "Échéance du " + e.dueDate() + " : " + e.amountDue() + " TND restent à payer." : e.message();
        toStudents(e.establishmentId(), List.of(e.studentId()), NotificationType.REQUEST, "Rappel de paiement",
                message, "Installment", e.installmentId());
    }

    private void toStudents(UUID establishmentId, List<UUID> studentIds, NotificationType type, String title,
            String body, String targetType, UUID targetId) {
        if (studentIds.isEmpty()) {
            return;
        }
        TenantContext.runAs(establishmentId, () -> notifyInNewTransaction(PersonType.STUDENT, studentIds, type, title,
                body, targetType, targetId));
    }

    private void toClass(UUID establishmentId, UUID schoolClassId, NotificationType type, String title, String body,
            String targetType, UUID targetId) {
        List<UUID> studentIds = TenantContext.callAs(establishmentId, () -> students.studentIdsInClass(schoolClassId));
        toStudents(establishmentId, studentIds, type, title, body, targetType, targetId);
    }

    private void notifyInNewTransaction(PersonType type, List<UUID> personIds, NotificationType notificationType,
            String title, String body, String targetType, UUID targetId) {
        notifications.notifyPeopleAfterCommit(type, personIds, notificationType, title, body, targetType, targetId);
    }

    private String subjectName(UUID establishmentId, UUID assignmentId) {
        return TenantContext.callAs(establishmentId, () -> teaching.findAssignment(assignmentId)
                .map(TeachingAssignmentInfo::subjectName).orElse("cours"));
    }

    private static String documentStatus(String status) {
        return switch (status) {
            case "IN_PROGRESS" -> "en traitement";
            case "DONE" -> "traitée";
            case "REJECTED" -> "rejetée";
            default -> "déposée";
        };
    }
}
