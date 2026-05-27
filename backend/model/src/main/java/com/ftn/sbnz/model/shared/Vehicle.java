package com.ftn.sbnz.model.shared;

import java.io.Serializable;

public class Vehicle implements Serializable {

    private static final long serialVersionUID = 1L;

    private VehicleCategory category;
    private boolean hasLights = true;

    public Vehicle() {}

    public Vehicle(VehicleCategory category) {
        this.category = category;
    }

    public VehicleCategory getCategory() { return category; }
    public void setCategory(VehicleCategory category) { this.category = category; }

    public boolean isHasLights() { return hasLights; }
    public void setHasLights(boolean hasLights) { this.hasLights = hasLights; }
}
