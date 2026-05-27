package com.ftn.sbnz.model.fault;

import java.io.Serializable;

import com.ftn.sbnz.model.sanctions.Driver;
import com.ftn.sbnz.model.shared.Vehicle;

public class Participant implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private Driver driver;
    private Vehicle vehicle;
    private Maneuver maneuver;
    private int speedKmH;
    private double distanceToVehicleAhead;
    private boolean underAlcoholInfluence;
    private boolean ignoredRedLight;

    public Participant() {}

    public Participant(String id, Driver driver, Vehicle vehicle, Maneuver maneuver) {
        this.id = id;
        this.driver = driver;
        this.vehicle = vehicle;
        this.maneuver = maneuver;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }

    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    public Maneuver getManeuver() { return maneuver; }
    public void setManeuver(Maneuver maneuver) { this.maneuver = maneuver; }

    public int getSpeedKmH() { return speedKmH; }
    public void setSpeedKmH(int speedKmH) { this.speedKmH = speedKmH; }

    public double getDistanceToVehicleAhead() { return distanceToVehicleAhead; }
    public void setDistanceToVehicleAhead(double distanceToVehicleAhead) {
        this.distanceToVehicleAhead = distanceToVehicleAhead;
    }

    public boolean isUnderAlcoholInfluence() { return underAlcoholInfluence; }
    public void setUnderAlcoholInfluence(boolean underAlcoholInfluence) {
        this.underAlcoholInfluence = underAlcoholInfluence;
    }

    public boolean isIgnoredRedLight() { return ignoredRedLight; }
    public void setIgnoredRedLight(boolean ignoredRedLight) {
        this.ignoredRedLight = ignoredRedLight;
    }
}
