package com.ftn.sbnz.service.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.ftn.sbnz.model.sanctions.LicenseCategory;
import com.ftn.sbnz.model.sanctions.LicenseType;
import com.ftn.sbnz.model.sanctions.Location;
import com.ftn.sbnz.model.shared.Vehicle;
import com.ftn.sbnz.model.sanctions.ViolationType;

/**
 * Input for POST /api/module1/issue-sanction.
 *
 * The officer fills in:
 *   - the driver they pulled over ({@link #driver}) - identified by JMBG,
 *     with optional license info (null license means "driver has never
 *     been licensed"),
 *   - the vehicle the driver was operating ({@link #vehicle}),
 *   - the blood-alcohol level measured at the stop ({@link #bacAtStop});
 *     when greater than 0.20, an ALCOHOL violation is auto-generated,
 *   - the explicit list of violations the officer is recording
 *     ({@link #violations}).
 *
 * The backend performs find-or-create on the driver / license (so the
 * driver's historical points are preserved across visits).
 */
public class IssueSanctionRequest {

    private DriverInput driver;
    private Vehicle vehicle;

    /**
     * Blood-alcohol level (per mille) measured at the roadside.
     * When > 0.20, the backend auto-adds an ALCOHOL violation with this
     * value so the officer does not have to remember to log it manually.
     */
    private Double bacAtStop;

    /**
     * Whether the violation(s) being reported caused a traffic accident.
     * Top-level flag: when true, the controller marks EVERY violation
     * in this submission (including the auto-added ALCOHOL one) with
     * causedAccident=true so the accident-escalation rules apply to all.
     */
    private boolean causedAccident;

    private List<ViolationInput> violations = new ArrayList<>();

    public IssueSanctionRequest() {}

    public DriverInput getDriver() { return driver; }
    public void setDriver(DriverInput driver) { this.driver = driver; }

    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    public Double getBacAtStop() { return bacAtStop; }
    public void setBacAtStop(Double bacAtStop) { this.bacAtStop = bacAtStop; }

    public boolean isCausedAccident() { return causedAccident; }
    public void setCausedAccident(boolean causedAccident) { this.causedAccident = causedAccident; }

    public List<ViolationInput> getViolations() { return violations; }
    public void setViolations(List<ViolationInput> violations) { this.violations = violations; }

    // ============================================================
    // Nested DTOs
    // ============================================================

    /** Identifies the driver (JMBG) plus the bare-minimum personal info needed
     *  to create a new Driver record when this JMBG is not yet in the DB. */
    public static class DriverInput {
        private String id;                  // JMBG (13 digits)
        private String firstName;
        private String lastName;
        private LocalDate dateOfBirth;
        private LicenseInput license;       // null = driver has no license at all

        public DriverInput() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public LocalDate getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

        public LicenseInput getLicense() { return license; }
        public void setLicense(LicenseInput license) { this.license = license; }
    }

    public static class LicenseInput {
        private String licenseNumber;
        private LocalDate issuedAt;
        private LicenseType type;
        private Set<LicenseCategory> categories;

        public LicenseInput() {}

        public String getLicenseNumber() { return licenseNumber; }
        public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

        public LocalDate getIssuedAt() { return issuedAt; }
        public void setIssuedAt(LocalDate issuedAt) { this.issuedAt = issuedAt; }

        public LicenseType getType() { return type; }
        public void setType(LicenseType type) { this.type = type; }

        public Set<LicenseCategory> getCategories() { return categories; }
        public void setCategories(Set<LicenseCategory> categories) { this.categories = categories; }
    }

    public static class ViolationInput {
        private ViolationType type;
        private Location location;
        private Integer speedOverLimitKmH;
        private Double bloodAlcoholLevel;

        private boolean causedAccident;
        private boolean injuredPersons;
        private boolean pedestrianIsChild;

        public ViolationInput() {}

        public ViolationType getType() { return type; }
        public void setType(ViolationType type) { this.type = type; }

        public Location getLocation() { return location; }
        public void setLocation(Location location) { this.location = location; }

        public Integer getSpeedOverLimitKmH() { return speedOverLimitKmH; }
        public void setSpeedOverLimitKmH(Integer speedOverLimitKmH) {
            this.speedOverLimitKmH = speedOverLimitKmH;
        }

        public Double getBloodAlcoholLevel() { return bloodAlcoholLevel; }
        public void setBloodAlcoholLevel(Double bloodAlcoholLevel) {
            this.bloodAlcoholLevel = bloodAlcoholLevel;
        }

        public boolean isCausedAccident() { return causedAccident; }
        public void setCausedAccident(boolean causedAccident) {
            this.causedAccident = causedAccident;
        }

        public boolean isInjuredPersons() { return injuredPersons; }
        public void setInjuredPersons(boolean injuredPersons) {
            this.injuredPersons = injuredPersons;
        }

        public boolean isPedestrianIsChild() { return pedestrianIsChild; }
        public void setPedestrianIsChild(boolean pedestrianIsChild) {
            this.pedestrianIsChild = pedestrianIsChild;
        }
    }
}
