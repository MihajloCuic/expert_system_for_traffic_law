package com.ftn.sbnz.model.sanctions;

import java.io.Serializable;

/**
 * Pomocni fakt (intermediate / interni) koji nastaje u Layer 5 (accumulation.drl)
 * i sluzi kao izvor sirovih agregata za kasnija pravila u istom sloju (cap, ban,
 * prison). Ne ulazi u javni response - kontroler ga ignorise.
 *
 * Flag polja (fineMinCapped, fineMaxCapped, prisonExplained) su idempotency
 * guards: cap / explanation pravila ih cekiraju u LHS-u i postavljaju u RHS-u
 * preko modify(), pa se ne aktiviraju vise od jednom.
 */
public class RawAggregation implements Serializable {

    private static final long serialVersionUID = 1L;

    private Driver driver;
    private int sumFineMin;
    private int sumFineMax;
    private int maxIndFineMin;
    private int maxIndFineMax;
    private int sumPrison;
    private int maxBan;

    private boolean fineMinCapped;
    private boolean fineMaxCapped;
    private boolean prisonExplained;

    public RawAggregation() {}

    public RawAggregation(Driver driver,
                          int sumFineMin, int sumFineMax,
                          int maxIndFineMin, int maxIndFineMax,
                          int sumPrison, int maxBan) {
        this.driver = driver;
        this.sumFineMin = sumFineMin;
        this.sumFineMax = sumFineMax;
        this.maxIndFineMin = maxIndFineMin;
        this.maxIndFineMax = maxIndFineMax;
        this.sumPrison = sumPrison;
        this.maxBan = maxBan;
        this.fineMinCapped = false;
        this.fineMaxCapped = false;
        this.prisonExplained = false;
    }

    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }

    public int getSumFineMin() { return sumFineMin; }
    public void setSumFineMin(int v) { this.sumFineMin = v; }

    public int getSumFineMax() { return sumFineMax; }
    public void setSumFineMax(int v) { this.sumFineMax = v; }

    public int getMaxIndFineMin() { return maxIndFineMin; }
    public void setMaxIndFineMin(int v) { this.maxIndFineMin = v; }

    public int getMaxIndFineMax() { return maxIndFineMax; }
    public void setMaxIndFineMax(int v) { this.maxIndFineMax = v; }

    public int getSumPrison() { return sumPrison; }
    public void setSumPrison(int v) { this.sumPrison = v; }

    public int getMaxBan() { return maxBan; }
    public void setMaxBan(int v) { this.maxBan = v; }

    public boolean isFineMinCapped() { return fineMinCapped; }
    public void setFineMinCapped(boolean v) { this.fineMinCapped = v; }

    public boolean isFineMaxCapped() { return fineMaxCapped; }
    public void setFineMaxCapped(boolean v) { this.fineMaxCapped = v; }

    public boolean isPrisonExplained() { return prisonExplained; }
    public void setPrisonExplained(boolean v) { this.prisonExplained = v; }
}
