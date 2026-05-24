package com.ftn.sbnz.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregated outcome across all Sanctions for one driver in a single session.
 *
 * Produced by the Layer 3 accumulate rule (accumulation.drl) using the
 * "sticaj prekrsaja" rules from Serbian Law on Misdemeanors:
 *
 *   FINES   - sum of all sanctions' fines, capped so total < 2 x max single fine.
 *   POINTS  - simple sum.
 *   BAN     - judge sentences between (highest single minimum) and 5 years.
 *   PRISON  - judge sentences between (sum / 2) and (full sum).
 *
 * License revocation (oduzimanje vozacke dozvole) is a separate type of
 * punishment and is not part of this summary (will be handled in a later layer
 * when active penalty points exceed the threshold).
 */
public class SanctionSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    private Driver driver;

    /** Lower bound of the total monetary fine (RSD), after sticaj cap. */
    private int totalFineMin;
    /** Upper bound of the total monetary fine (RSD), after sticaj cap. */
    private int totalFineMax;

    /** Total penalty points (simple sum, no cap). */
    private int totalPoints;

    /** Mandatory minimum driving ban in days (= longest single minimum ban imposed). */
    private int drivingBanMinDays;
    /** Legal maximum driving ban in days. 1825 (5 years) when any ban applies, otherwise 0. */
    private int drivingBanMaxDays;

    /** Minimum prison sentence in days (= total prison sum / 2). */
    private int prisonMinDays;
    /** Maximum prison sentence in days (= total prison sum). */
    private int prisonMaxDays;

    /** All Sanctions that contributed to this summary (after deduplication). */
    private List<Sanction> sanctions = new ArrayList<>();
    private List<String> overallExplanation = new ArrayList<>();

    public SanctionSummary() {}

    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }

    public int getTotalFineMin() { return totalFineMin; }
    public void setTotalFineMin(int totalFineMin) { this.totalFineMin = totalFineMin; }

    public int getTotalFineMax() { return totalFineMax; }
    public void setTotalFineMax(int totalFineMax) { this.totalFineMax = totalFineMax; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    public int getDrivingBanMinDays() { return drivingBanMinDays; }
    public void setDrivingBanMinDays(int drivingBanMinDays) { this.drivingBanMinDays = drivingBanMinDays; }

    public int getDrivingBanMaxDays() { return drivingBanMaxDays; }
    public void setDrivingBanMaxDays(int drivingBanMaxDays) { this.drivingBanMaxDays = drivingBanMaxDays; }

    public int getPrisonMinDays() { return prisonMinDays; }
    public void setPrisonMinDays(int prisonMinDays) { this.prisonMinDays = prisonMinDays; }

    public int getPrisonMaxDays() { return prisonMaxDays; }
    public void setPrisonMaxDays(int prisonMaxDays) { this.prisonMaxDays = prisonMaxDays; }

    public List<Sanction> getSanctions() { return sanctions; }
    public void setSanctions(List<Sanction> sanctions) { this.sanctions = sanctions; }

    public List<String> getOverallExplanation() { return overallExplanation; }
    public void setOverallExplanation(List<String> overallExplanation) {
        this.overallExplanation = overallExplanation;
    }

    public void addExplanation(String line) { this.overallExplanation.add(line); }
}
