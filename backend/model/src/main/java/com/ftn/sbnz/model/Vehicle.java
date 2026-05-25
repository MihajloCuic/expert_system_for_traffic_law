package com.ftn.sbnz.model;

import java.io.Serializable;

public class Vehicle implements Serializable {

    private static final long serialVersionUID = 1L;

    private String licensePlate;
    private VehicleCategory category;
    private boolean hasLights = true;

    public Vehicle() {}

    public Vehicle(String licensePlate, VehicleCategory category) {
        this.licensePlate = licensePlate;
        this.category = category;
    }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public VehicleCategory getCategory() { return category; }
    public void setCategory(VehicleCategory category) { this.category = category; }

    public boolean isHasLights() { return hasLights; }
    public void setHasLights(boolean hasLights) { this.hasLights = hasLights; }
}
