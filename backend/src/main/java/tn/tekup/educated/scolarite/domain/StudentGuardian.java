package tn.tekup.educated.scolarite.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Links a learner to a guardian. */
@Entity
@Table(name = "student_guardian", schema = "scolarite")
public class StudentGuardian extends TenantEntity {

    private boolean primaryContact;

    private boolean financialResponsible;

    private boolean livesWithStudent;

    private boolean notifyPush;

    private boolean notifySms;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guardian_id", nullable = false)
    private Guardian guardian;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kinship_id", nullable = false)
    private Kinship kinship;

    public StudentGuardian() {
    }

    public boolean isPrimaryContact() {
        return primaryContact;
    }

    public void setPrimaryContact(boolean primaryContact) {
        this.primaryContact = primaryContact;
    }

    public boolean isFinancialResponsible() {
        return financialResponsible;
    }

    public void setFinancialResponsible(boolean financialResponsible) {
        this.financialResponsible = financialResponsible;
    }

    public boolean isLivesWithStudent() {
        return livesWithStudent;
    }

    public void setLivesWithStudent(boolean livesWithStudent) {
        this.livesWithStudent = livesWithStudent;
    }

    public boolean isNotifyPush() {
        return notifyPush;
    }

    public void setNotifyPush(boolean notifyPush) {
        this.notifyPush = notifyPush;
    }

    public boolean isNotifySms() {
        return notifySms;
    }

    public void setNotifySms(boolean notifySms) {
        this.notifySms = notifySms;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Guardian getGuardian() {
        return guardian;
    }

    public void setGuardian(Guardian guardian) {
        this.guardian = guardian;
    }

    public Kinship getKinship() {
        return kinship;
    }

    public void setKinship(Kinship kinship) {
        this.kinship = kinship;
    }

}
