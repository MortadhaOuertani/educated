package tn.tekup.educated.scolarite.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Registre des fiches: one printed attendance or registration sheet. */
@Entity
@Table(name = "print_log", schema = "scolarite")
public class PrintLog extends TenantEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PrintKind kind;

    @Column(nullable = false, length = 40)
    private String registryNumber;

    @Column(nullable = false)
    private String documentName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    public PrintLog() {
    }

    public PrintKind getKind() {
        return kind;
    }

    public void setKind(PrintKind kind) {
        this.kind = kind;
    }

    public String getRegistryNumber() {
        return registryNumber;
    }

    public void setRegistryNumber(String registryNumber) {
        this.registryNumber = registryNumber;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

}
