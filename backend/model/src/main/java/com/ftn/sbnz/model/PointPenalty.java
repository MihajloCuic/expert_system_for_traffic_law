package com.ftn.sbnz.model;

import java.io.Serializable;
import java.util.Date;

import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * A single award of penalty points to a driver - JPA entity + Drools CEP event.
 *
 * As a Drools event, it carries @Role(EVENT) + @Timestamp + @Expires("730d")
 * so the engine automatically removes events older than 24 months.
 *
 * As a JPA entity, every PointPenalty is persisted in the H2 database. On
 * application start, the DroolsSessionService re-loads PointPenalty rows for
 * the relevant driver and inserts them into the "pointsStream" entry-point
 * before firing rules.
 */
@Role(Role.Type.EVENT)
@Timestamp("issuedAt")
@Expires("730d")
@Entity
@Table(name = "point_penalties")
public class PointPenalty implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "driver_id", referencedColumnName = "id")
    private Driver driver;

    @Column(name = "points")
    private int points;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "issued_at")
    private Date issuedAt;

    /** Reference back to the violation/sanction that produced these points (audit). */
    @Column(name = "violation_ref", length = 64)
    private String violationRef;

    public PointPenalty() {}

    public PointPenalty(Driver driver, int points, Date issuedAt, String violationRef) {
        this.driver = driver;
        this.points = points;
        this.issuedAt = issuedAt;
        this.violationRef = violationRef;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public Date getIssuedAt() { return issuedAt; }
    public void setIssuedAt(Date issuedAt) { this.issuedAt = issuedAt; }

    public String getViolationRef() { return violationRef; }
    public void setViolationRef(String violationRef) { this.violationRef = violationRef; }
}
