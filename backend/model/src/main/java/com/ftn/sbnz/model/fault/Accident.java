package com.ftn.sbnz.model.fault;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Accident implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private LocalDateTime occurredAt;
    private AccidentScene scene;
    private List<Participant> participants = new ArrayList<>();
    private boolean injuredPersons;
    private boolean fatalOutcome;
    private int materialDamageRsd;

    public Accident() {}

    public Accident(String id, LocalDateTime occurredAt, AccidentScene scene) {
        this.id = id;
        this.occurredAt = occurredAt;
        this.scene = scene;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }

    public AccidentScene getScene() { return scene; }
    public void setScene(AccidentScene scene) { this.scene = scene; }

    public List<Participant> getParticipants() { return participants; }
    public void setParticipants(List<Participant> participants) { this.participants = participants; }

    public boolean isInjuredPersons() { return injuredPersons; }
    public void setInjuredPersons(boolean injuredPersons) { this.injuredPersons = injuredPersons; }

    public boolean isFatalOutcome() { return fatalOutcome; }
    public void setFatalOutcome(boolean fatalOutcome) { this.fatalOutcome = fatalOutcome; }

    public int getMaterialDamageRsd() { return materialDamageRsd; }
    public void setMaterialDamageRsd(int materialDamageRsd) { this.materialDamageRsd = materialDamageRsd; }
}
