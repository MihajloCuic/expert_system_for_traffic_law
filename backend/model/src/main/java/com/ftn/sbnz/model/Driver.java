package com.ftn.sbnz.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "drivers")
public class Driver implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "full_name", nullable = false, length = 128)
    private String fullName;

    @Column(name = "years_of_experience")
    private int yearsOfExperience;

    @Column(name = "penalty_points")
    private int penaltyPoints;

    /** Driver's date of birth - required for probationary license calculation. */
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    /** Date when the driver obtained their license. */
    @Column(name = "license_issued_at")
    private LocalDate licenseIssuedAt;

    public Driver() {}

    public Driver(String id, String fullName, int yearsOfExperience, int penaltyPoints) {
        this.id = id;
        this.fullName = fullName;
        this.yearsOfExperience = yearsOfExperience;
        this.penaltyPoints = penaltyPoints;
    }

    public Driver(String id, String fullName, int yearsOfExperience, int penaltyPoints,
                  LocalDate dateOfBirth, LocalDate licenseIssuedAt) {
        this(id, fullName, yearsOfExperience, penaltyPoints);
        this.dateOfBirth = dateOfBirth;
        this.licenseIssuedAt = licenseIssuedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public int getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(int yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }

    public int getPenaltyPoints() { return penaltyPoints; }
    public void setPenaltyPoints(int penaltyPoints) { this.penaltyPoints = penaltyPoints; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public LocalDate getLicenseIssuedAt() { return licenseIssuedAt; }
    public void setLicenseIssuedAt(LocalDate licenseIssuedAt) { this.licenseIssuedAt = licenseIssuedAt; }

    /**
     * Returns true if the driver currently holds a PROBATIONARY license.
     *
     * Per ZOBS:
     *   - probationary license lasts 2 years from issue date,
     *   - OR until the driver's 21st birthday if the license was obtained
     *     before the driver turned 19.
     */
    public boolean isProbationary(LocalDate now) {
        if (licenseIssuedAt == null || dateOfBirth == null) return false;

        LocalDate nineteenthBirthday = dateOfBirth.plusYears(19);
        LocalDate probationEnd;
        if (licenseIssuedAt.isBefore(nineteenthBirthday)) {
            probationEnd = dateOfBirth.plusYears(21);
        } else {
            probationEnd = licenseIssuedAt.plusYears(2);
        }
        return !now.isAfter(probationEnd);
    }

    public boolean isProbationary() {
        return isProbationary(LocalDate.now());
    }

    /**
     * Equality by primary key (id). REQUIRED for Drools rules like
     *   PointPenalty(driver == $d)
     * to work when the PointPenalty was loaded from the database (where
     * Hibernate returns a different Java instance for the same row).
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
        return "Driver{" + fullName + ", exp=" + yearsOfExperience + "y, points=" + penaltyPoints + "}";
    }
}
