package com.ftn.sbnz.model;

import java.io.Serializable;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "license_revocations")
public class LicenseRevocation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "driver_id", referencedColumnName = "id")
    private Driver driver;

    @Column(name = "revoked_at")
    private LocalDate revokedAt;

    @Column(name = "active_points_at_revocation")
    private int activePointsAtRevocation;

    @Column(name = "probationary")
    private boolean probationary;

    @Column(name = "reason", length = 256)
    private String reason;

    public LicenseRevocation() {}

    public LicenseRevocation(Driver driver, LocalDate revokedAt,
                             int activePointsAtRevocation, boolean probationary,
                             String reason) {
        this.driver = driver;
        this.revokedAt = revokedAt;
        this.activePointsAtRevocation = activePointsAtRevocation;
        this.probationary = probationary;
        this.reason = reason;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }

    public LocalDate getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDate revokedAt) { this.revokedAt = revokedAt; }

    public int getActivePointsAtRevocation() { return activePointsAtRevocation; }
    public void setActivePointsAtRevocation(int activePointsAtRevocation) {
        this.activePointsAtRevocation = activePointsAtRevocation;
    }

    public boolean isProbationary() { return probationary; }
    public void setProbationary(boolean probationary) { this.probationary = probationary; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
