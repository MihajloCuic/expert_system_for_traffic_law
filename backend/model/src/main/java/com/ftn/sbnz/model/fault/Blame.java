package com.ftn.sbnz.model.fault;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Blame implements Serializable {

    private static final long serialVersionUID = 1L;

    private Participant participant;
    private int percentage;
    private BlameType type;
    private List<String> reasoning = new ArrayList<>();

    public Blame() {}

    public Blame(Participant participant, int percentage, BlameType type) {
        this.participant = participant;
        this.percentage = percentage;
        this.type = type;
    }

    public Participant getParticipant() { return participant; }
    public void setParticipant(Participant participant) { this.participant = participant; }

    public int getPercentage() { return percentage; }
    public void setPercentage(int percentage) { this.percentage = percentage; }

    public BlameType getType() { return type; }
    public void setType(BlameType type) { this.type = type; }

    public List<String> getReasoning() { return reasoning; }
    public void setReasoning(List<String> reasoning) { this.reasoning = reasoning; }

    public void addReasoning(String line) { this.reasoning.add(line); }
}
