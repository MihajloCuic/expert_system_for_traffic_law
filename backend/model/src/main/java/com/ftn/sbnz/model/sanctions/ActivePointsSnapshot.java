package com.ftn.sbnz.model.sanctions;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Materialised result of the 24-month sliding-window accumulate over PointPenalty events.
 * Tells how many penalty points a driver currently has active.
 *
 * Produced by points_window.drl (Layer 4 CEP rule), consumed by revocation.drl (Layer 5).
 */
public class ActivePointsSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    private Driver driver;
    private int activePoints;
    private LocalDate computedAt;

    public ActivePointsSnapshot() {}

    public ActivePointsSnapshot(Driver driver, int activePoints, LocalDate computedAt) {
        this.driver = driver;
        this.activePoints = activePoints;
        this.computedAt = computedAt;
    }

    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }

    public int getActivePoints() { return activePoints; }
    public void setActivePoints(int activePoints) { this.activePoints = activePoints; }

    public LocalDate getComputedAt() { return computedAt; }
    public void setComputedAt(LocalDate computedAt) { this.computedAt = computedAt; }
}
