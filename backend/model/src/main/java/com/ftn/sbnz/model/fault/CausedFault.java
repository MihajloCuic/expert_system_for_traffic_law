package com.ftn.sbnz.model.fault;

import java.io.Serializable;

public class CausedFault implements Serializable {

    private static final long serialVersionUID = 1L;

    private Participant cause;
    private Fault consequence;
    private String reason;

    public CausedFault() {}

    public CausedFault(Participant cause, Fault consequence, String reason) {
        this.cause = cause;
        this.consequence = consequence;
        this.reason = reason;
    }

    public Participant getCause() { return cause; }
    public void setCause(Participant cause) { this.cause = cause; }

    public Fault getConsequence() { return consequence; }
    public void setConsequence(Fault consequence) { this.consequence = consequence; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
