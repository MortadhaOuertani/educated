package tn.tekup.educated.administration.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tn.tekup.educated.shared.domain.PersonInfo;
import tn.tekup.educated.shared.domain.TenantEntity;

/** A back-office staff member. */
@Entity
@Table(name = "staff_member", schema = "administration")
public class StaffMember extends TenantEntity {

    @Embedded
    private PersonInfo person;

    @Column(length = 40)
    private String employeeNumber;

    private String jobTitle;

    private String service;

    private boolean active;

    public StaffMember() {
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

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
