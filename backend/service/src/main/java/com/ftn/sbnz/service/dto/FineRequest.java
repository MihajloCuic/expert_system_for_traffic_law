package com.ftn.sbnz.service.dto;

import com.ftn.sbnz.model.Driver;
import com.ftn.sbnz.model.Vehicle;
import com.ftn.sbnz.model.Violation;
import com.ftn.sbnz.model.ViolationContext;

public class FineRequest {

    private Driver driver;
    private Vehicle vehicle;
    private Violation violation;
    private ViolationContext context;

    public FineRequest() {}

    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }

    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    public Violation getViolation() { return violation; }
    public void setViolation(Violation violation) { this.violation = violation; }

    public ViolationContext getContext() { return context; }
    public void setContext(ViolationContext context) { this.context = context; }
}
