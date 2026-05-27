package com.ftn.sbnz.model.sanctions;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * A person identified in the system by their JMBG (Serbian national ID).
 *
 * A Driver may or may not currently hold a {@link DrivingLicense} - the
 * {@link #license} reference is intentionally nullable, because the
 * "no license at all" case is legally distinct from "has a license but
 * is currently under a ban"
 */
@Entity
@Table(name = "drivers")
public class Driver implements Serializable {

    /** JMBG (Serbian national ID) - 13 digits. Always present. */
    @Id
    @Column(name = "id", length = 13)
    private String id;

    @Column(name = "first_name", nullable = false, length = 64)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 64)
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    /**
     * The driver's currently issued driving license, if any.
     * NULL means the driver has never been licensed (or the license has been
     * fully invalidated by court order). Different from a currently revoked
     * license, which is modelled via a separate {@link LicenseRevocation} row.
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "license_number")
    private DrivingLicense license;

    public Driver() {}

    public Driver(String id, String firstName, String lastName,
                  LocalDate dateOfBirth, DrivingLicense license) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.license = license;
    }

    // --- getters / setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public DrivingLicense getLicense() { return license; }
    public void setLicense(DrivingLicense license) { this.license = license; }

    @Transient
    public String getFullName() {
        if (firstName == null && lastName == null) return null;
        return (firstName == null ? "" : firstName)
                + (firstName != null && lastName != null ? " " : "")
                + (lastName == null ? "" : lastName);
    }

    public boolean isProbationary() {
        return license != null && license.getType() == LicenseType.PROBATIONARY;
    }

    /**
     * Equality by primary key (JMBG). Required for Drools rules like
     *   PointPenalty(driver == $d)
     * to work when the PointPenalty was loaded from the database
     * (Hibernate returns a different Java instance for the same row).
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Driver)) return false;
        Driver other = (Driver) obj;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Driver{" + getFullName() + ", id=" + id
                + ", license=" + (license == null ? "NONE" : license.getLicenseNumber()) + "}";
    }
}
