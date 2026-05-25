package com.ftn.sbnz.service.seed;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.ftn.sbnz.model.Driver;
import com.ftn.sbnz.model.PointPenalty;
import com.ftn.sbnz.service.repository.DriverRepository;
import com.ftn.sbnz.service.repository.PointPenaltyRepository;

/**
 * Seeds the H2 database with a handful of drivers and historical penalty points
 * so the CEP rules have something to react to in a fresh checkout.
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
        Driver d1 = new Driver("DR-1001", "Marko Markovic", 8, 0,
            LocalDate.of(1995, 5, 10), LocalDate.of(2013, 6, 1));
        drivers.save(d1);
        points.save(new PointPenalty(d1, 4, daysAgo(400), "SEED-001"));
        points.save(new PointPenalty(d1, 7, daysAgo(180), "SEED-002"));
        // total active = 11 (< 18)

        // --- Driver 2: standard license, history JUST UNDER the 18-point threshold ---
        Driver d2 = new Driver("DR-1002", "Petar Petrovic", 15, 0,
            LocalDate.of(1985, 3, 20), LocalDate.of(2005, 8, 15));
        drivers.save(d2);
        points.save(new PointPenalty(d2, 6, daysAgo(600), "SEED-101"));
        points.save(new PointPenalty(d2, 7, daysAgo(300), "SEED-102"));
        // total active = 13 (< 18, but close)

        // --- Driver 3: probationary license, history JUST UNDER the 9-point threshold ---
        Driver d3 = new Driver("DR-1003", "Ana Anic", 1, 0,
            LocalDate.now().minusYears(20),
            LocalDate.now().minusMonths(6));
        drivers.save(d3);
        points.save(new PointPenalty(d3, 4, daysAgo(120), "SEED-201"));
        // total active = 4 (< 9, but a single 5-point violation would tip it over)

        // --- Driver 4: clean record, no history ---
        Driver d4 = new Driver("DR-1004", "Nikola Nikolic", 10, 0,
            LocalDate.of(1990, 1, 1), LocalDate.of(2010, 1, 1));
        drivers.save(d4);

        // --- Driver 5: has older points that should EXPIRE, current points trivial ---
        Driver d5 = new Driver("DR-1005", "Jovan Jovanovic", 20, 0,
            LocalDate.of(1980, 1, 1), LocalDate.of(2000, 1, 1));
        drivers.save(d5);
        points.save(new PointPenalty(d5, 10, daysAgo(800), "SEED-OLD-1"));   // EXPIRED
        points.save(new PointPenalty(d5, 10, daysAgo(900), "SEED-OLD-2"));   // EXPIRED
        points.save(new PointPenalty(d5, 4,  daysAgo(60),  "SEED-CURRENT")); // active
        // total active = 4 only (the two 10-pt rows are outside the 24-month window)

        System.out.println("[SEED] Inserted 5 drivers with point histories.");
    }

    private static Date daysAgo(int days) {
        return Date.from(LocalDate.now().minusDays(days)
            .atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
