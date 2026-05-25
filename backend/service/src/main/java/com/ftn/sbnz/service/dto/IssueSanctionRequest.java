package com.ftn.sbnz.service.dto;

import java.util.ArrayList;
import java.util.List;

import com.ftn.sbnz.model.Location;
import com.ftn.sbnz.model.ViolationType;

/**
 * Input for POST /api/module1/issue-sanction.
 *
 * The driver is referenced by id only (must already exist in the H2 database).
 * Each item in {@link #violations} describes one Violation the officer wants
 * to record in this single case.
 */
public class IssueSanctionRequest {

    private String driverId;
    private List<ViolationInput> violations = new ArrayList<>();

    public IssueSanctionRequest() {}

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public List<ViolationInput> getViolations() { return violations; }
    public void setViolations(List<ViolationInput> violations) { this.violations = violations; }

    public static class ViolationInput {
        private ViolationType type;
        private Location location;
        private Integer speedOverLimitKmH;
        private Double bloodAlcoholLevel;

        /** Whether this violation caused (or contributed to) a traffic accident. */
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
