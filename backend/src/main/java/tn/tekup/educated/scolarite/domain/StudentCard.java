package tn.tekup.educated.scolarite.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Carte étudiant for one academic year. */
@Entity
@Table(name = "student_card", schema = "scolarite")
public class StudentCard extends TenantEntity {

    @Column(nullable = false)
    private UUID academicYearId;

    @Column(nullable = false, length = 40)
    private String cardNumber;

    @Column(nullable = false, length = 100)
    private String qrToken;

    @Column(nullable = false)
    private LocalDate issuedOn;

    @Column(nullable = false)
    private LocalDate validUntil;

    private UUID pdfFileId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    public StudentCard() {
    }

    public UUID getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(UUID academicYearId) {
        this.academicYearId = academicYearId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }

    public LocalDate getIssuedOn() {
        return issuedOn;
    }

    public void setIssuedOn(LocalDate issuedOn) {
        this.issuedOn = issuedOn;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    public UUID getPdfFileId() {
        return pdfFileId;
    }

    public void setPdfFileId(UUID pdfFileId) {
        this.pdfFileId = pdfFileId;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

}
