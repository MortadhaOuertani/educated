package tn.tekup.educated.pedagogie.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tn.tekup.educated.shared.domain.Money;
import tn.tekup.educated.shared.domain.PersonInfo;
import tn.tekup.educated.shared.domain.TenantEntity;

/** A teacher (enseignant). */
@Entity
@Table(name = "teacher", schema = "pedagogie")
public class Teacher extends TenantEntity {

    @Embedded
    private PersonInfo person;

    @Column(length = 40)
    private String employeeNumber;

    private UUID departmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EmploymentType employmentType;

    private LocalDate hireDate;

    @Embedded
    private Money hourlyRate;

    private Integer statutoryHours;

    private Integer maxWeeklyHours;

    @Column(length = 40)
    private String bankAccountIban;

    private boolean departmentHead;

    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grade_id", nullable = false)
    private TeacherGrade grade;

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeacherDomain> domains = new ArrayList<>();

    public Teacher() {
    }

    public PersonInfo getPerson() {
        return person;
    }

    public void setPerson(PersonInfo person) {
        this.person = person;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public UUID getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(UUID departmentId) {
        this.departmentId = departmentId;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public Money getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Money hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public Integer getStatutoryHours() {
        return statutoryHours;
    }

    public void setStatutoryHours(Integer statutoryHours) {
        this.statutoryHours = statutoryHours;
    }

    public Integer getMaxWeeklyHours() {
        return maxWeeklyHours;
    }

    public void setMaxWeeklyHours(Integer maxWeeklyHours) {
        this.maxWeeklyHours = maxWeeklyHours;
    }

    public String getBankAccountIban() {
        return bankAccountIban;
    }

    public void setBankAccountIban(String bankAccountIban) {
        this.bankAccountIban = bankAccountIban;
    }

    public boolean isDepartmentHead() {
        return departmentHead;
    }

    public void setDepartmentHead(boolean departmentHead) {
        this.departmentHead = departmentHead;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public TeacherGrade getGrade() {
        return grade;
    }

    public void setGrade(TeacherGrade grade) {
        this.grade = grade;
    }

    public List<TeacherDomain> getDomains() {
        return domains;
    }

}
