package com.ftn.sbnz.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.ftn.sbnz.model.Driver;
import com.ftn.sbnz.model.LicenseRevocation;
import com.ftn.sbnz.model.Location;
import com.ftn.sbnz.model.PointPenalty;
import com.ftn.sbnz.model.Vehicle;
import com.ftn.sbnz.model.VehicleCategory;
import com.ftn.sbnz.model.Violation;
import com.ftn.sbnz.model.ViolationType;
import com.ftn.sbnz.service.repository.DriverRepository;
import com.ftn.sbnz.service.repository.LicenseRevocationRepository;
import com.ftn.sbnz.service.repository.PointPenaltyRepository;
import com.ftn.sbnz.service.service.DroolsSessionService;

/**
 * Verifies the end-to-end CEP flow:
 *   driver loaded from H2 -> DroolsSessionService loads history into pointsStream
 *   -> fireAllRules runs Layers 1..5 -> session contains the expected facts
 *   -> persistNew writes new PointPenalty / LicenseRevocation back to H2.
 *
 * Uses the "test" Spring profile so the DataSeeder is disabled (we create our
 * own controlled drivers per test).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CepIntegrationTest {

    @Autowired DroolsSessionService droolsSessionService;
    @Autowired DriverRepository driverRepository;
    @Autowired PointPenaltyRepository pointPenaltyRepository;
    @Autowired LicenseRevocationRepository licenseRevocationRepository;

    @BeforeEach
    void resetTables() {
        // Tests are @Transactional so DB is rolled back between them, but make
        // double-sure if running with non-rollback config.
        licenseRevocationRepository.deleteAll();
        pointPenaltyRepository.deleteAll();
        driverRepository.deleteAll();
    }

    // -----------------------------------------------------------------
    // Driver with 11 active points commits a small violation worth 4 points.
    // Total = 15 -> still below 18 -> no revocation expected.
    // -----------------------------------------------------------------
    @Test
    void standardLicense_belowThreshold_noRevocation() {
        Driver d = saveStandardDriver("T-001", "Test Standard 1");
        savePoints(d, 4, daysAgo(400));
        savePoints(d, 7, daysAgo(180));

        Violation v = newViolation(d, ViolationType.SPEEDING, Location.URBAN);
        v.setSpeedOverLimitKmH(35); // -> rule 3, 4 points

        fireAndPersist(d, v, null);

        Optional<LicenseRevocation> rev =
            licenseRevocationRepository.findFirstByDriverIdOrderByRevokedAtDesc(d.getId());
        assertThat(rev).isEmpty();
    }

    // -----------------------------------------------------------------
    // Driver with 13 active points commits a violation worth 6 points.
    // Total = 19 -> >= 18 -> license revocation (standard threshold).
    // -----------------------------------------------------------------
    @Test
    void standardLicense_crossesThreshold_revocation() {
        Driver d = saveStandardDriver("T-002", "Test Standard 2");
        savePoints(d, 6, daysAgo(600));
        savePoints(d, 7, daysAgo(300));

        Violation v = newViolation(d, ViolationType.RED_LIGHT, Location.ANY);
        // rule 25 -> 6 points

        fireAndPersist(d, v, null);

        Optional<LicenseRevocation> rev =
            licenseRevocationRepository.findFirstByDriverIdOrderByRevokedAtDesc(d.getId());
        assertThat(rev).isPresent();
        assertThat(rev.get().isProbationary()).isFalse();
        assertThat(rev.get().getActivePointsAtRevocation()).isGreaterThanOrEqualTo(18);
    }

    // -----------------------------------------------------------------
    // Probationary driver with 4 active points commits a violation worth 6 points.
    // Total = 10 -> >= 9 (probationary threshold) -> revocation.
    // -----------------------------------------------------------------
    @Test
    void probationaryLicense_crossesThreshold_revocation() {
        Driver d = saveProbationaryDriver("T-003", "Test Probationary");
        savePoints(d, 4, daysAgo(120));

        Violation v = newViolation(d, ViolationType.RED_LIGHT, Location.ANY);
        // rule 25 -> 6 points

        fireAndPersist(d, v, null);

        Optional<LicenseRevocation> rev =
            licenseRevocationRepository.findFirstByDriverIdOrderByRevokedAtDesc(d.getId());
        assertThat(rev).isPresent();
        assertThat(rev.get().isProbationary()).isTrue();
        assertThat(rev.get().getActivePointsAtRevocation()).isGreaterThanOrEqualTo(9);
    }

    // -----------------------------------------------------------------
    // Driver has 20 points older than 24 months + 4 active points + a small
    // current violation. Old points must NOT count -> no revocation.
    // -----------------------------------------------------------------
    @Test
    void oldPoints_expired_noRevocation() {
        Driver d = saveStandardDriver("T-004", "Test Expired Points");
        savePoints(d, 10, daysAgo(800));  // expired
        savePoints(d, 10, daysAgo(900));  // expired
        savePoints(d, 4,  daysAgo(60));   // active

        Violation v = newViolation(d, ViolationType.NO_SEATBELT, Location.ANY);
        // rule 40 -> 0 points (just fine)

        fireAndPersist(d, v, null);

        Optional<LicenseRevocation> rev =
            licenseRevocationRepository.findFirstByDriverIdOrderByRevokedAtDesc(d.getId());
        assertThat(rev).isEmpty();
    }

    // -----------------------------------------------------------------
    // Driver already has an active LicenseRevocation. Driving any violation
    // should automatically add a "driving during ban" sanction (auto-derived).
    // -----------------------------------------------------------------
    @Test
    void drivingWithActiveRevocation_autoDetectedAsDuringBan() {
        Driver d = saveStandardDriver("T-005", "Test Banned Driver");
        licenseRevocationRepository.save(new LicenseRevocation(
            d, LocalDate.now().minusDays(30), 19, false,
            "Pre-existing revocation"));

        Violation v = newViolation(d, ViolationType.IMPROPER_DISTANCE, Location.ANY);

        KieSession session = droolsSessionService.openSessionFor(d);
        Vehicle vehicle = new Vehicle("UNKNOWN", VehicleCategory.CAR);
        v.setVehicle(vehicle);
        session.insert(v);
        session.fireAllRules();

        // Find any "330 pt. 8" sanction in the session
        boolean hasDuringBanSanction = session.getObjects().stream()
            .anyMatch(o -> o instanceof com.ftn.sbnz.model.Sanction s
                && s.getLawArticle() != null
                && s.getLawArticle().contains("330")
                && s.getLawArticle().contains("8"));
        assertThat(hasDuringBanSanction)
            .as("Auto-derived 'driving during ban' sanction should have been created")
            .isTrue();

        session.dispose();
    }

    // -----------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------

    private Driver saveStandardDriver(String id, String name) {
        Driver d = new Driver(id, name, 10, 0,
            LocalDate.of(1990, 1, 1), LocalDate.of(2010, 1, 1));
        return driverRepository.save(d);
    }

    private Driver saveProbationaryDriver(String id, String name) {
        // Got license 6 months ago, person is 20 years old -> still on probation
        Driver d = new Driver(id, name, 1, 0,
            LocalDate.now().minusYears(20),
            LocalDate.now().minusMonths(6));
        return driverRepository.save(d);
    }

    private void savePoints(Driver d, int amount, Date issuedAt) {
        pointPenaltyRepository.save(new PointPenalty(d, amount, issuedAt, "TEST"));
    }

    private static Violation newViolation(Driver d, ViolationType type, Location loc) {
        Vehicle vehicle = new Vehicle("TST", VehicleCategory.CAR);
        return new Violation(d, vehicle, type, loc);
    }

    private void fireAndPersist(Driver driver, Violation violation,
                                com.ftn.sbnz.model.ViolationContext ctx) {
        KieSession session = droolsSessionService.openSessionFor(driver);
        try {
            session.insert(violation);
            if (ctx != null) session.insert(ctx);
            session.fireAllRules();
            droolsSessionService.persistNew(session, driver);
        } finally {
            session.dispose();
        }
    }

    private static Date daysAgo(int days) {
        return Date.from(LocalDate.now().minusDays(days)
            .atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
