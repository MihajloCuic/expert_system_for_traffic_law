package com.ftn.sbnz.model;

import java.io.Serializable;

public class Driver implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String fullName;
    private int yearsOfExperience;
    private int penaltyPoints;

    public Driver() {}

    public Driver(String id, String fullName, int yearsOfExperience, int penaltyPoints) {
        this.id = id;
        this.fullName = fullName;
        this.yearsOfExperience = yearsOfExperience;
        this.penaltyPoints = penaltyPoints;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public int getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(int yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }

    public int getPenaltyPoints() { return penaltyPoints; }
    public void setPenaltyPoints(int penaltyPoints) { this.penaltyPoints = penaltyPoints; }

    @Override
    public String toString() {
        return "Driver{" + fullName + ", exp=" + yearsOfExperience + "y, points=" + penaltyPoints + "}";
    }
}
