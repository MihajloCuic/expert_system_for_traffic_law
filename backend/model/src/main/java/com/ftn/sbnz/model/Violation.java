package com.ftn.sbnz.model;

import java.io.Serializable;

public class Violation implements Serializable {

    private static final long serialVersionUID = 1L;

    private Driver driver;
    private Vehicle vehicle;
    private ViolationType type;
    private Location location;

    private int speedOverLimitKmH;
    private double bloodAlcoholLevel;

    public Violation() {}

    public Violation(Driver driver, Vehicle vehicle, ViolationType type, Location location) {
        this.driver = driver;
        this.vehicle = vehicle;
        this.type = type;
        this.location = location;
    }

    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }

    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    public ViolationType getType() { return type; }
    public void setType(ViolationType type) { this.type = type; }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }

    public int getSpeedOverLimitKmH() { return speedOverLimitKmH; }
    public void setSpeedOverLimitKmH(int speedOverLimitKmH) { this.speedOverLimitKmH = speedOverLimitKmH; }

    public double getBloodAlcoholLevel() { return bloodAlcoholLevel; }
    public void setBloodAlcoholLevel(double bloodAlcoholLevel) { this.bloodAlcoholLevel = bloodAlcoholLevel; }
}
