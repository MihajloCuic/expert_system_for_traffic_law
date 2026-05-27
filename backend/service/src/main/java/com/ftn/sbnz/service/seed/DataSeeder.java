package com.ftn.sbnz.service.seed;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.ftn.sbnz.model.sanctions.Driver;
import com.ftn.sbnz.model.sanctions.DrivingLicense;
import com.ftn.sbnz.model.sanctions.LicenseCategory;
import com.ftn.sbnz.model.sanctions.LicenseType;
import com.ftn.sbnz.model.sanctions.PointPenalty;
import com.ftn.sbnz.service.repository.DriverRepository;
import com.ftn.sbnz.service.repository.PointPenaltyRepository;

/**
 * Seeds the H2 database with a handful of drivers (with realistic JMBGs +
 * driving licenses) and historical penalty points, so the CEP and
 * revocation rules have something to react to in a fresh checkout.
 *
 * Runs only when the DB is empty (idempotent). To re-seed, delete
 * ./data/traffic-law.mv.db and restart.
 */
@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private final DriverRepository drivers;
    private final PointPenaltyRepository points;

    public DataSeeder(DriverRepository drivers, PointPenaltyRepository points) {
        this.drivers = drivers;
        this.points = points;
    }

    @Override
    public void run(String... args) {
        if (drivers.count() > 0) {
            System.out.println("[SEED] DB already contains drivers - skipping seed.");
            return;
        }

        // --- Driver 1: standard license, history WELL UNDER the 18-point threshold ---
        Driver d1 = new Driver(
                "1005995780013", "Marko", "Markovic",
                LocalDate.of(1995, 5, 10),
                new DrivingLicense("RS-100001",
                        LocalDate.of(2013, 6, 1),
                        LicenseType.PERMANENT,
                        Set.of(LicenseCategory.B, LicenseCategory.BE)));
        drivers.save(d1);
        points.save(new PointPenalty(d1, 4, daysAgo(400), "SEED-001"));
        points.save(new PointPenalty(d1, 7, daysAgo(180), "SEED-002"));
        // total active = 11 (< 18)

        // --- Driver 2: standard license + multiple categories, JUST UNDER 18 ---
        Driver d2 = new Driver(
                "2003985710015", "Petar", "Petrovic",
                LocalDate.of(1985, 3, 20),
                new DrivingLicense("RS-100002",
                        LocalDate.of(2005, 8, 15),
                        LicenseType.PERMANENT,
                        Set.of(LicenseCategory.B, LicenseCategory.C, LicenseCategory.CE)));
        drivers.save(d2);
        points.save(new PointPenalty(d2, 6, daysAgo(600), "SEED-101"));
        points.save(new PointPenalty(d2, 7, daysAgo(300), "SEED-102"));
        // total active = 13 (close to 18 standard threshold)

        // --- Driver 3: PROBATIONARY license, history JUST UNDER the 9-point threshold ---
        Driver d3 = new Driver(
                "1502005715016", "Ana", "Anic",
                LocalDate.now().minusYears(20),
                new DrivingLicense("RS-100003",
                        LocalDate.now().minusMonths(6),
                        LicenseType.PROBATIONARY,
                        Set.of(LicenseCategory.B)));
        drivers.save(d3);
        points.save(new PointPenalty(d3, 4, daysAgo(120), "SEED-201"));
        // total active = 4 (a single 5-point violation would tip it over the 9-point probationary cap)

        // --- Driver 4: clean record, full bus license ---
        Driver d4 = new Driver(
                "0101990780017", "Nikola", "Nikolic",
                LocalDate.of(1990, 1, 1),
                new DrivingLicense("RS-100004",
                        LocalDate.of(2010, 1, 1),
                        LicenseType.PERMANENT,
                        Set.of(LicenseCategory.B, LicenseCategory.D)));
        drivers.save(d4);

        // --- Driver 5: has older points that should EXPIRE, current points trivial ---
        Driver d5 = new Driver(
                "0101980780018", "Jovan", "Jovanovic",
                LocalDate.of(1980, 1, 1),
                new DrivingLicense("RS-100005",
                        LocalDate.of(2000, 1, 1),
                        LicenseType.PERMANENT,
                        Set.of(LicenseCategory.B)));
        drivers.save(d5);
        points.save(new PointPenalty(d5, 10, daysAgo(800), "SEED-OLD-1"));   // EXPIRED
        points.save(new PointPenalty(d5, 10, daysAgo(900), "SEED-OLD-2"));   // EXPIRED
        points.save(new PointPenalty(d5, 4,  daysAgo(60),  "SEED-CURRENT")); // active
        // total active = 4 only

        // --- Driver 6: NO LICENSE at all - useful for NO_LICENSE rule demos ---
        Driver d6 = new Driver(
                "0101997780019", "Stefan", "Stefanovic",
                LocalDate.of(1997, 1, 1),
                null);
        drivers.save(d6);

        System.out.println("[SEED] Inserted 6 drivers (5 licensed + 1 without license) with point histories.");
    }

    private static Date daysAgo(int days) {
        return Date.from(LocalDate.now().minusDays(days)
            .atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
