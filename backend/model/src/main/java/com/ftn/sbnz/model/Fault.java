package com.ftn.sbnz.model;

import java.io.Serializable;

public class Fault implements Serializable {

    private static final long serialVersionUID = 1L;

    private Participant participant;
    private FaultType type;
    private int weight;
    private String lawArticle;
    private boolean causedAccident;

    public Fault() {}

    public Fault(Participant participant, FaultType type, int weight, String lawArticle) {
        this.participant = participant;
        this.type = type;
        this.weight = weight;
        this.lawArticle = lawArticle;
    }

    public Participant getParticipant() { return participant; }
    public void setParticipant(Participant participant) { this.participant = participant; }

    public FaultType getType() { return type; }
    public void setType(FaultType type) { this.type = type; }

    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }

    public String getLawArticle() { return lawArticle; }
    public void setLawArticle(String lawArticle) { this.lawArticle = lawArticle; }

    public boolean isCausedAccident() { return causedAccident; }
    public void setCausedAccident(boolean causedAccident) { this.causedAccident = causedAccident; }
}
