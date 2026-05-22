package com.ftn.sbnz.service.dto;

import java.util.ArrayList;
import java.util.List;

import com.ftn.sbnz.model.Sanction;
import com.ftn.sbnz.model.Severity;

public class FineResponse {

    private int finalFine;
    private int finalPoints;
    private int baseFine;
    private int basePoints;
    private String lawArticle;
    private Severity severity;
    private List<String> explanation = new ArrayList<>();

    public FineResponse() {}

    public static FineResponse from(Sanction s) {
        FineResponse r = new FineResponse();
        r.finalFine = s.getFinalFine();
        r.finalPoints = s.getFinalPoints();
        r.baseFine = s.getBaseFine();
        r.basePoints = s.getBasePoints();
        r.lawArticle = s.getLawArticle();
        r.severity = s.getSeverity();
        r.explanation = new ArrayList<>(s.getExplanation());
        return r;
    }

    public int getFinalFine() { return finalFine; }
    public void setFinalFine(int finalFine) { this.finalFine = finalFine; }

    public int getFinalPoints() { return finalPoints; }
    public void setFinalPoints(int finalPoints) { this.finalPoints = finalPoints; }

    public int getBaseFine() { return baseFine; }
    public void setBaseFine(int baseFine) { this.baseFine = baseFine; }

    public int getBasePoints() { return basePoints; }
    public void setBasePoints(int basePoints) { this.basePoints = basePoints; }

    public String getLawArticle() { return lawArticle; }
    public void setLawArticle(String lawArticle) { this.lawArticle = lawArticle; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public List<String> getExplanation() { return explanation; }
    public void setExplanation(List<String> explanation) { this.explanation = explanation; }
}
