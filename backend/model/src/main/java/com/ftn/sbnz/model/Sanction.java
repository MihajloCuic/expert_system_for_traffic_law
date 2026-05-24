package com.ftn.sbnz.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A sanction derived from a Violation by the rule engine.
 *
 * Captures the full ZOBS sanction structure:
 *   - monetary fine (single value OR a min-max range)
 *   - penalty points
 *   - driving ban (zabrana upravljanja motornim vozilom)
 *   - prison sentence (zatvor) and/or community service (rad u javnom interesu)
 *   - flag for alternative penalty (fine OR prison)
 *
 * Layer 1 fills the "base" fields. Layers 2 and 3 modify the "final" fields
 * and may set the ban / prison fields.
 */
public class Sanction implements Serializable {

    private static final long serialVersionUID = 1L;

    private Violation violation;

    // === Article and severity ===
    private String lawArticle;
    private Severity severity;

    // === Monetary fine ===
    /** Lower bound of the prescribed fine in RSD. */
    private int minFine;
    /** Upper bound of the prescribed fine in RSD. Equal to minFine when the law prescribes a fixed amount. */
    private int maxFine;
    /** Base fine assigned by Layer 1 (typically the lower bound or fixed value). */
    private int baseFine;
    /** Final fine after Layer 2 / Layer 3 adjustments. */
    private int finalFine;

    // === Penalty points ===
    private int basePoints;
    private int finalPoints;

    // === Driving ban (zabrana upravljanja motornim vozilom) ===
    /** Driving ban in days. 0 if no ban is imposed. */
    private int drivingBanDays;

    // === Prison (zatvor) ===
    private int prisonDaysMin;
    private int prisonDaysMax;

    // === Community service (rad u javnom interesu), in hours ===
    private int communityServiceHoursMin;
    private int communityServiceHoursMax;

    /**
     * True when ZOBS prescribes the fine OR prison as alternative penalties
     * (e.g. "20.000-40.000 RSD ili kazna zatvora do 30 dana").
     */
    private boolean alternativePenalty;

    private List<String> explanation = new ArrayList<>();

    public Sanction() {}

    /**
     * Legacy constructor kept for backward compatibility with the homework #3 demo.
     * For new rules use {@link #builder()}.
     */
    public Sanction(Violation violation, int baseFine, int basePoints,
                    String lawArticle, Severity severity) {
        this.violation = violation;
        this.baseFine = baseFine;
        this.minFine = baseFine;
        this.maxFine = baseFine;
        this.basePoints = basePoints;
        this.lawArticle = lawArticle;
        this.severity = severity;
        this.finalFine = baseFine;
        this.finalPoints = basePoints;
    }

    // --- getters / setters ---

    public Violation getViolation() { return violation; }
    public void setViolation(Violation violation) { this.violation = violation; }

    public String getLawArticle() { return lawArticle; }
    public void setLawArticle(String lawArticle) { this.lawArticle = lawArticle; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public int getMinFine() { return minFine; }
    public void setMinFine(int minFine) { this.minFine = minFine; }

    public int getMaxFine() { return maxFine; }
    public void setMaxFine(int maxFine) { this.maxFine = maxFine; }

    public int getBaseFine() { return baseFine; }
    public void setBaseFine(int baseFine) { this.baseFine = baseFine; }

    public int getFinalFine() { return finalFine; }
    public void setFinalFine(int finalFine) { this.finalFine = finalFine; }

    public int getBasePoints() { return basePoints; }
    public void setBasePoints(int basePoints) { this.basePoints = basePoints; }

    public int getFinalPoints() { return finalPoints; }
    public void setFinalPoints(int finalPoints) { this.finalPoints = finalPoints; }

    public int getDrivingBanDays() { return drivingBanDays; }
    public void setDrivingBanDays(int drivingBanDays) { this.drivingBanDays = drivingBanDays; }

    public int getPrisonDaysMin() { return prisonDaysMin; }
    public void setPrisonDaysMin(int prisonDaysMin) { this.prisonDaysMin = prisonDaysMin; }

    public int getPrisonDaysMax() { return prisonDaysMax; }
    public void setPrisonDaysMax(int prisonDaysMax) { this.prisonDaysMax = prisonDaysMax; }

    public int getCommunityServiceHoursMin() { return communityServiceHoursMin; }
    public void setCommunityServiceHoursMin(int communityServiceHoursMin) {
        this.communityServiceHoursMin = communityServiceHoursMin;
    }

    public int getCommunityServiceHoursMax() { return communityServiceHoursMax; }
    public void setCommunityServiceHoursMax(int communityServiceHoursMax) {
        this.communityServiceHoursMax = communityServiceHoursMax;
    }

    public boolean isAlternativePenalty() { return alternativePenalty; }
    public void setAlternativePenalty(boolean alternativePenalty) {
        this.alternativePenalty = alternativePenalty;
    }

    public List<String> getExplanation() { return explanation; }
    public void setExplanation(List<String> explanation) { this.explanation = explanation; }

    public void addExplanation(String line) { this.explanation.add(line); }

    // --- builder for cleaner construction of richer sanctions ---

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Sanction s = new Sanction();

        public Builder violation(Violation v) { s.violation = v; return this; }
        public Builder lawArticle(String a) { s.lawArticle = a; return this; }
        public Builder severity(Severity sev) { s.severity = sev; return this; }

        public Builder fine(int min, int max) {
            s.minFine = min;
            s.maxFine = max;
            s.baseFine = min;
            s.finalFine = min;
            return this;
        }
        public Builder fixedFine(int amount) { return fine(amount, amount); }

        public Builder points(int p) { s.basePoints = p; s.finalPoints = p; return this; }
        public Builder drivingBanDays(int d) { s.drivingBanDays = d; return this; }
        public Builder prison(int minDays, int maxDays) {
            s.prisonDaysMin = minDays;
            s.prisonDaysMax = maxDays;
            return this;
        }
        public Builder communityService(int minH, int maxH) {
            s.communityServiceHoursMin = minH;
            s.communityServiceHoursMax = maxH;
            return this;
        }
        public Builder alternativePenalty(boolean a) { s.alternativePenalty = a; return this; }

        public Sanction build() { return s; }
    }
}
