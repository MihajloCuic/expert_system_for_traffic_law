package com.ftn.sbnz.service.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.EntryPoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ftn.sbnz.model.Driver;
import com.ftn.sbnz.model.LicenseRevocation;
import com.ftn.sbnz.model.PointPenalty;
import com.ftn.sbnz.service.repository.LicenseRevocationRepository;
import com.ftn.sbnz.service.repository.PointPenaltyRepository;

/**
 * Bridges the JPA database and the Drools knowledge base.
 *
 *   1. {@link #openSessionFor(Driver)} - creates a fresh sanctionsKsession
 *      and pre-loads every PointPenalty issued to the driver in the last
 *      24 months into the "pointsStream" entry-point.
 *   2. {@link #persistNew(KieSession, Driver)} - after rule firing, scans
 *      the entry-point AND the main working memory for any newly inserted
 *      PointPenalty / LicenseRevocation facts and persists them to the DB.
 */
@Service
public class DroolsSessionService {

    /** 24 months in days - the legal life of an active penalty point. */
    private static final int POINT_WINDOW_DAYS = 730;

    private final KieContainer kieContainer;
    private final PointPenaltyRepository pointPenaltyRepository;
    private final LicenseRevocationRepository licenseRevocationRepository;

    public DroolsSessionService(KieContainer kieContainer,
                                PointPenaltyRepository pointPenaltyRepository,
                                LicenseRevocationRepository licenseRevocationRepository) {
        this.kieContainer = kieContainer;
        this.pointPenaltyRepository = pointPenaltyRepository;
        this.licenseRevocationRepository = licenseRevocationRepository;
    }

    @Transactional(readOnly = true)
    public KieSession openSessionFor(Driver driver) {
        KieSession session = kieContainer.newKieSession("sanctionsKsession");
        session.insert(driver);

        EntryPoint pointsStream = session.getEntryPoint("pointsStream");
        Date cutoff = Date.from(LocalDate.now().minusDays(POINT_WINDOW_DAYS)
            .atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<PointPenalty> history =
            pointPenaltyRepository.findActiveForDriver(driver.getId(), cutoff);
        for (PointPenalty pp : history) {
            pointsStream.insert(pp);
        }

        Optional<LicenseRevocation> revocation =
            licenseRevocationRepository.findFirstByDriverIdOrderByRevokedAtDesc(driver.getId());
        revocation.ifPresent(session::insert);

        System.out.println("[DB] Loaded " + history.size()
            + " active PointPenalty + " + (revocation.isPresent() ? "1" : "0")
            + " LicenseRevocation for driver " + driver.getFullName());

        return session;
    }

    /**
     * After rule firing, persist any new facts produced this session:
     *   - PointPenalty events emitted by Layer 3.5 into the "pointsStream"
     *     entry-point (these are NOT in the main working memory, so we must
     *     scan the entry-point separately).
     *   - LicenseRevocation facts produced by Layer 5 in the main memory.
     */
    @Transactional
    public void persistNew(KieSession session, Driver driver) {
        int newPoints = 0;
        int newRevocations = 0;

        // --- PointPenalty events live in the "pointsStream" entry-point ---
        EntryPoint pointsStream = session.getEntryPoint("pointsStream");
        for (Object o : pointsStream.getObjects()) {
            if (o instanceof PointPenalty pp && pp.getId() == null) {
                pointPenaltyRepository.save(pp);
                newPoints++;
            }
        }

        // --- LicenseRevocation lives in main working memory ---
        for (Object o : session.getObjects()) {
            if (o instanceof LicenseRevocation rev && rev.getId() == null) {
                licenseRevocationRepository.save(rev);
                newRevocations++;
            }
        }

        if (newPoints > 0 || newRevocations > 0) {
            System.out.println("[DB] Persisted " + newPoints + " new PointPenalty + "
                + newRevocations + " new LicenseRevocation for "
                + driver.getFullName());
        }
    }
}
