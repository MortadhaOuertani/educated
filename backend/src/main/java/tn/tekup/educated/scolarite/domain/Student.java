package tn.tekup.educated.scolarite.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import tn.tekup.educated.shared.domain.PersonInfo;
import tn.tekup.educated.shared.domain.TenantEntity;

/** A learner (apprenant). */
@Entity
@Table(name = "student", schema = "scolarite")
public class Student extends TenantEntity {

    @Embedded
    private PersonInfo person;

    @Column(nullable = false, length = 40)
    private String matricule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private StudentStatus status;

    private LocalDate admissionDate;

    @Column(length = 100)
    private String baccalaureateSection;

    @Column(precision = 5, scale = 2)
    private BigDecimal baccalaureateAverage;

    private Integer baccalaureateYear;

    private String previousInstitution;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentGuardian> guardians = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("enrollmentDate")
    private List<Enrollment> enrollments = new ArrayList<>();

    public Student() {
    }

    public PersonInfo getPerson() {
        return person;
    }

    public void setPerson(PersonInfo person) {
        this.person = person;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public StudentStatus getStatus() {
        return status;
    }

    public void setStatus(StudentStatus status) {
        this.status = status;
    }

    public LocalDate getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(LocalDate admissionDate) {
        this.admissionDate = admissionDate;
    }

    public String getBaccalaureateSection() {
        return baccalaureateSection;
    }

    public void setBaccalaureateSection(String baccalaureateSection) {
        this.baccalaureateSection = baccalaureateSection;
    }

    public BigDecimal getBaccalaureateAverage() {
        return baccalaureateAverage;
    }

    public void setBaccalaureateAverage(BigDecimal baccalaureateAverage) {
        this.baccalaureateAverage = baccalaureateAverage;
    }

    public Integer getBaccalaureateYear() {
        return baccalaureateYear;
    }

    public void setBaccalaureateYear(Integer baccalaureateYear) {
        this.baccalaureateYear = baccalaureateYear;
    }

    public String getPreviousInstitution() {
        return previousInstitution;
    }

    public void setPreviousInstitution(String previousInstitution) {
        this.previousInstitution = previousInstitution;
    }

    public List<StudentGuardian> getGuardians() {
        return guardians;
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

}
