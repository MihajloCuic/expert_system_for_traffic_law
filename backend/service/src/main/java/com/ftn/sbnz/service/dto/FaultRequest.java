package com.ftn.sbnz.service.dto;

import java.util.ArrayList;
import java.util.List;

import com.ftn.sbnz.model.Accident;
import com.ftn.sbnz.model.Fault;
import com.ftn.sbnz.model.ParticipantAction;

public class FaultRequest {

    private Accident accident;
    private List<Fault> declaredFaults = new ArrayList<>();
    private List<ParticipantAction> actions = new ArrayList<>();

    public FaultRequest() {}

    public Accident getAccident() { return accident; }
    public void setAccident(Accident accident) { this.accident = accident; }

    public List<Fault> getDeclaredFaults() { return declaredFaults; }
    public void setDeclaredFaults(List<Fault> declaredFaults) { this.declaredFaults = declaredFaults; }

    public List<ParticipantAction> getActions() { return actions; }
    public void setActions(List<ParticipantAction> actions) { this.actions = actions; }
}
