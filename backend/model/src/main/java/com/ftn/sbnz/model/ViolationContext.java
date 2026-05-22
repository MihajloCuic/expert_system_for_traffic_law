package com.ftn.sbnz.model;

import java.io.Serializable;

public class ViolationContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private Violation violation;
    private boolean fledFromScene;
    private boolean selfReported;
    private boolean schoolZone;
    private boolean injuredPersons;

    public ViolationContext() {}

    public ViolationContext(Violation violation) {
        this.violation = violation;
    }

    public Violation getViolation() { return violation; }
    public void setViolation(Violation violation) { this.violation = violation; }

    public boolean isFledFromScene() { return fledFromScene; }
    public void setFledFromScene(boolean fledFromScene) { this.fledFromScene = fledFromScene; }

    public boolean isSelfReported() { return selfReported; }
    public void setSelfReported(boolean selfReported) { this.selfReported = selfReported; }

    public boolean isSchoolZone() { return schoolZone; }
    public void setSchoolZone(boolean schoolZone) { this.schoolZone = schoolZone; }

    public boolean isInjuredPersons() { return injuredPersons; }
    public void setInjuredPersons(boolean injuredPersons) { this.injuredPersons = injuredPersons; }
}
