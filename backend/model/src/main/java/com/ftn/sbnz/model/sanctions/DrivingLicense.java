package com.ftn.sbnz.model.sanctions;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import com.ftn.sbnz.model.shared.VehicleCategory;

/**
 * Serbian driving license issued by MUP.
 *
 * The {@link #licenseNumber} is the unique MUP identifier (used by the
 * officer at the roadside to look the driver up). It serves as the
 * primary key.
 *
 * One Driver may have at most ONE active license; a Driver may also
 * have NO license at all (never been issued, fully invalidated, etc.) -
 * in that case the {@code license} reference on Driver is simply null,
 * which is meaningful for the Drools rule engine (e.g. {@code NO_LICENSE}
 * violation, no revocation thresholds apply).
 */
@Entity
@Table(name = "driving_licenses")
public class DrivingLicense implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "license_number", length = 32)
    private String licenseNumber;

    @Column(name = "issued_at")
    private LocalDate issuedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 16)
    private LicenseType type;

    /**
     * Categories the license entitles the driver to operate
     * (e.g. {B, BE} for a typical driver, {A, B} for a motorcyclist who
     * also drives a car).
     */
    @ElementCollection(targetClass = LicenseCategory.class, fetch = FetchType.EAGER)
    @CollectionTable(
        name = "license_categories",
        joinColumns = @JoinColumn(name = "license_number")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 4, nullable = false)
    private Set<LicenseCategory> categories = new HashSet<>();

    public DrivingLicense() {}

    public DrivingLicense(String licenseNumber, LocalDate issuedAt,
                          LicenseType type, Set<LicenseCategory> categories) {
        this.licenseNumber = licenseNumber;
        this.issuedAt = issuedAt;
        this.type = type;
        if (categories != null) this.categories = new HashSet<>(categories);
    }

    // --- getters / setters ---

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public LocalDate getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDate issuedAt) { this.issuedAt = issuedAt; }

    public LicenseType getType() { return type; }
    public void setType(LicenseType type) { this.type = type; }

    public Set<LicenseCategory> getCategories() { return categories; }
    public void setCategories(Set<LicenseCategory> categories) { this.categories = categories; }

    /** Convenience: does this license entitle the holder to drive the supplied vehicle category? */
    public boolean covers(VehicleCategory vc) {
        if (categories == null) return false;
        for (LicenseCategory lc : categories) {
            if (lc.getVehicleCategory() == vc) return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DrivingLicense)) return false;
        DrivingLicense other = (DrivingLicense) obj;
        return licenseNumber != null && licenseNumber.equals(other.licenseNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(licenseNumber);
    }
}
