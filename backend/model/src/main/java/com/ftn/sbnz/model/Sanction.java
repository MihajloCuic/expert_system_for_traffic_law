package com.ftn.sbnz.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Sanction implements Serializable {

    private static final long serialVersionUID = 1L;

    private Violation violation;
    private int baseFine;
    private int basePoints;
    private String lawArticle;
    private Severity severity;

    private int finalFine;
    private int finalPoints;

    private List<String> explanation = new ArrayList<>();

    public Sanction() {}

    public Sanction(Violation violation, int baseFine, int basePoints,
                    String lawArticle, Severity severity) {
        this.violation = violation;
        this.baseFine = baseFine;
        this.basePoints = basePoints;
        this.lawArticle = lawArticle;
        this.severity = severity;
        this.finalFine = baseFine;
        this.finalPoints = basePoints;
    }

    public Violation getViolation() { return violation; }
    public void setViolation(Violation violation) { this.violation = violation; }

    public int getBaseFine() { return baseFine; }
    public void setBaseFine(int baseFine) { this.baseFine = baseFine; }

    public int getBasePoints() { return basePoints; }
    public void setBasePoints(int basePoints) { this.basePoints = basePoints; }

    public String getLawArticle() { return lawArticle; }
    public void setLawArticle(String lawArticle) { this.lawArticle = lawArticle; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public int getFinalFine() { return finalFine; }
    public void setFinalFine(int finalFine) { this.finalFine = finalFine; }

    public int getFinalPoints() { return finalPoints; }
    public void setFinalPoints(int finalPoints) { this.finalPoints = finalPoints; }

    public List<String> getExplanation() { return explanation; }
    public void setExplanation(List<String> explanation) { this.explanation = explanation; }

    public void addExplanation(String line) { this.explanation.add(line); }
}
