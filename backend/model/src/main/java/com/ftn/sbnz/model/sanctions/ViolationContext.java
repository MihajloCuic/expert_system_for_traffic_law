package com.ftn.sbnz.model.sanctions;

import java.io.Serializable;

/**
 * Extra context that a single Violation may carry.
 * Only fields with a basis in ZOBS are kept here.
 */
public class ViolationContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private Violation violation;

    /**
     * True when the violation caused (or contributed to) a traffic accident.
     * Triggers the accident-escalation rules (ZOBS art. 329-334, rules 59-65).
     */
    private boolean causedAccident;

    /**
     * True when persons were injured in the accident.
     * Currently informational; can be referenced by future expert heuristics.
     */
    private boolean injuredPersons;

    /**
     * True when the pedestrian involved (e.g. on a crossing) is a child
     * younger than 12 years - aggravates rule 29 (right of way for pedestrian).
     */
    private boolean pedestrianIsChild;

    public ViolationContext() {}

    public ViolationContext(Violation violation) {
        this.violation = violation;
    }

    public Violation getViolation() { return violation; }
    public void setViolation(Violation violation) { this.violation = violation; }

    public boolean isCausedAccident() { return causedAccident; }
    public void setCausedAccident(boolean causedAccident) { this.causedAccident = causedAccident; }

    public boolean isInjuredPersons() { return injuredPersons; }
    public void setInjuredPersons(boolean injuredPersons) { this.injuredPersons = injuredPersons; }

    public boolean isPedestrianIsChild() { return pedestrianIsChild; }
    public void setPedestrianIsChild(boolean pedestrianIsChild) { this.pedestrianIsChild = pedestrianIsChild; }
}
