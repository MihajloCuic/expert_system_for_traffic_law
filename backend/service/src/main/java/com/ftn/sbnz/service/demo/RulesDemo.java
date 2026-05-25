package com.ftn.sbnz.service.demo;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.EntryPoint;

import com.ftn.sbnz.model.ActivePointsSnapshot;
import com.ftn.sbnz.model.Driver;
import com.ftn.sbnz.model.LicenseRevocation;
import com.ftn.sbnz.model.Location;
import com.ftn.sbnz.model.PointPenalty;
import com.ftn.sbnz.model.Sanction;
import com.ftn.sbnz.model.SanctionSummary;
import com.ftn.sbnz.model.Vehicle;
import com.ftn.sbnz.model.VehicleCategory;
import com.ftn.sbnz.model.Violation;
import com.ftn.sbnz.model.ViolationContext;
import com.ftn.sbnz.model.ViolationType;

/**
 * Standalone demo for Module 1 - Fine calculation.
 * Right-click -> Run 'RulesDemo.main()' in the IDE.
 *
 * Three-layer forward chaining:
 *   Layer 1 (qualification_*.drl) - one rule per ZOBS sub-article; each
 *                                    matching violation produces one Sanction.
 *   Layer 2 (accident_escalations.drl) - if ViolationContext.causedAccident
 *                                         is true, the sanction is upgraded
 *                                         per ZOBS art. 329-334.
 *   Layer 3 (accumulation.drl) - accumulate(sum, max) over all Sanctions of
 *                                 the same Driver -> SanctionSummary.
 */
public class RulesDemo {

    public static void main(String[] args) {
        KieServices ks = KieServices.Factory.get();
        KieContainer kc = ks.getKieClasspathContainer();

        System.out.println("\n========== EXPERT SYSTEM FOR TRAFFIC LAW ==========");
        System.out.println("Module 1 - Fine calculation\n");

        scenarioSingleViolation(kc);
        scenarioMultipleViolations(kc);
        scenarioRecklessDrivingAlone(kc);
        scenarioMultipleViolationsWithAccident(kc);
        scenarioContinuingOffense(kc);
        scenarioStickajCap(kc);
        scenarioActivePointsBelowThreshold(kc);
        scenarioStandardLicenseRevocation(kc);
        scenarioProbationaryLicenseRevocation(kc);
        scenarioOldPointsExpire(kc);

        System.out.println("\n=====================================================");
    }

    // -----------------------------------------------------------------
    // Scenario A: a single typical violation
    //   Speeding URBAN 35 km/h over (matches rule 3 -> MAJOR / art. 331)
    // -----------------------------------------------------------------
    private static void scenarioSingleViolation(KieContainer kc) {
        System.out.println("\n--- SCENARIO A: Single violation (Speeding URBAN 35 km/h over) ---");
        KieSession session = kc.newKieSession("sanctionsKsession");

        Driver d = new Driver("D001", "Marko Markovic", 8, 0);
        Vehicle car = new Vehicle("NS-123-AB", VehicleCategory.CAR);

        Violation v = new Violation(d, car, ViolationType.SPEEDING, Location.URBAN);
        v.setSpeedOverLimitKmH(35);

        session.insert(d);
        session.insert(v);
        session.fireAllRules();

        printOutcome(session);
        session.dispose();
    }

    // -----------------------------------------------------------------
    // Scenario B: three violations committed at the same time by the same driver
    //   Speeding URBAN 35 over   -> rule 3   (10-20k + 4 points + 30 days ban)
    //   Crossing solid line      -> rule 33  (20-40k + 6 points + 90 days ban)
    //   No seatbelt              -> rule 40  (10k flat)
    // Layer 3 accumulates: sum of fines and points, max of ban days.
    // -----------------------------------------------------------------
    private static void scenarioMultipleViolations(KieContainer kc) {
        System.out.println("\n--- SCENARIO B: Multiple simultaneous violations ---");
        KieSession session = kc.newKieSession("sanctionsKsession");

        Driver d = new Driver("D002", "Petar Petrovic", 12, 2);
        Vehicle car = new Vehicle("NS-456-CD", VehicleCategory.CAR);

        Violation v1 = new Violation(d, car, ViolationType.SPEEDING, Location.URBAN);
        v1.setSpeedOverLimitKmH(35);

        Violation v2 = new Violation(d, car, ViolationType.CROSSING_SOLID_LINE, Location.ANY);
        Violation v3 = new Violation(d, car, ViolationType.NO_SEATBELT, Location.ANY);

        session.insert(d);
        session.insert(v1);
        session.insert(v2);
        session.insert(v3);
        session.fireAllRules();

        printOutcome(session);
        session.dispose();
    }

    // -----------------------------------------------------------------
    // Scenario C: an extreme single violation that on its own qualifies as
    //   reckless driving (art. 329).
    //   Alcohol 2.20 per mille  -> rule 23 (VIOLENT severity)
    // -----------------------------------------------------------------
    private static void scenarioRecklessDrivingAlone(KieContainer kc) {
        System.out.println("\n--- SCENARIO C: Single reckless-driving violation (alcohol 2.20) ---");
        KieSession session = kc.newKieSession("sanctionsKsession");

        Driver d = new Driver("D003", "Jovan Jovanovic", 4, 0);
        Vehicle car = new Vehicle("NS-789-EF", VehicleCategory.CAR);

        Violation v = new Violation(d, car, ViolationType.ALCOHOL, Location.ANY);
        v.setBloodAlcoholLevel(2.20);

        session.insert(d);
        session.insert(v);
        session.fireAllRules();

        printOutcome(session);
        session.dispose();
    }

    // -----------------------------------------------------------------
    // Scenario D: multiple violations where one causes a traffic accident.
    //   Speeding URBAN 35 over (rule 3, MAJOR)
    //   No seatbelt              (rule 40, MODERATE)
    //   ViolationContext for the speeding marks causedAccident=true.
    // Expected:
    //   Layer 1 produces 2 Sanctions
    //   Layer 2 escalates the speeding sanction (MAJOR -> art. 331 + accident: 40-60k or prison <= 60d)
    //   Layer 3 sums into SanctionSummary
    // -----------------------------------------------------------------
    private static void scenarioMultipleViolationsWithAccident(KieContainer kc) {
        System.out.println("\n--- SCENARIO D: Multiple violations + speeding caused accident ---");
        KieSession session = kc.newKieSession("sanctionsKsession");

        Driver d = new Driver("D004", "Ana Anic", 6, 4);
        Vehicle car = new Vehicle("NS-321-GH", VehicleCategory.CAR);

        Violation vSpeed = new Violation(d, car, ViolationType.SPEEDING, Location.URBAN);
        vSpeed.setSpeedOverLimitKmH(35);

        Violation vBelt = new Violation(d, car, ViolationType.NO_SEATBELT, Location.ANY);

        ViolationContext ctxSpeed = new ViolationContext(vSpeed);
        ctxSpeed.setCausedAccident(true);
        ctxSpeed.setInjuredPersons(true);

        session.insert(d);
        session.insert(vSpeed);
        session.insert(vBelt);
        session.insert(ctxSpeed);
        session.fireAllRules();

        printOutcome(session);
        session.dispose();
    }

    // -----------------------------------------------------------------
    // Scenario E: same offense recorded twice (continuing offense / dedup)
    //   Two SPEEDING URBAN 35 km/h over violations -> Layer 1 produces 2 Sanctions
    //   Layer 1.5 dedup recognises the continuing offense and merges them into 1
    //   Layer 3 sums (now a single Sanction)
    // -----------------------------------------------------------------
    private static void scenarioContinuingOffense(KieContainer kc) {
        System.out.println("\n--- SCENARIO E: Continuing offense (same article submitted twice) ---");
        KieSession session = kc.newKieSession("sanctionsKsession");

        Driver d = new Driver("D005", "Nikola Nikolic", 10, 0);
        Vehicle car = new Vehicle("NS-555-IJ", VehicleCategory.CAR);

        Violation v1 = new Violation(d, car, ViolationType.SPEEDING, Location.URBAN);
        v1.setSpeedOverLimitKmH(35);
        Violation v2 = new Violation(d, car, ViolationType.SPEEDING, Location.URBAN);
        v2.setSpeedOverLimitKmH(38);  // Both fall in 31-50 km/h range -> same article

        session.insert(d);
        session.insert(v1);
        session.insert(v2);
        session.fireAllRules();

        printOutcome(session);
        session.dispose();
    }

    // -----------------------------------------------------------------
    // Scenario F: shows the "fine cannot reach 2 x max" cap.
    //   Three distinct violations whose fines would naively sum past the cap.
    //     Speeding URBAN 35 over   (rule 3:  10-20k)
    //     Crossing solid line       (rule 33: 20-40k)
    //     Red light                 (rule 25: 20-40k)
    //   Sum of finalFine (lower bounds): 10k + 20k + 20k = 50k
    //   Max single finalFine = 20k, 2 x max = 40k
    //   50k >= 40k -> capped to 39 999 RSD (per Law on Misdemeanors)
    // -----------------------------------------------------------------
    private static void scenarioStickajCap(KieContainer kc) {
        System.out.println("\n--- SCENARIO F: Sticaj cap demonstration (total < 2 x max single fine) ---");
        KieSession session = kc.newKieSession("sanctionsKsession");

        Driver d = new Driver("D006", "Stefan Stefanovic", 7, 0);
        Vehicle car = new Vehicle("NS-666-KL", VehicleCategory.CAR);

        Violation v1 = new Violation(d, car, ViolationType.SPEEDING, Location.URBAN);
        v1.setSpeedOverLimitKmH(35);  // rule 3 -> 10-20k

        Violation v2 = new Violation(d, car, ViolationType.CROSSING_SOLID_LINE, Location.ANY);  // rule 33 -> 20-40k
        Violation v3 = new Violation(d, car, ViolationType.RED_LIGHT, Location.ANY);            // rule 25 -> 20-40k

        session.insert(d);
        session.insert(v1);
        session.insert(v2);
        session.insert(v3);
        session.fireAllRules();

        printOutcome(session);
        session.dispose();
    }

    // -----------------------------------------------------------------
    // Scenario G: driver with 11 active points (below 18 threshold) - no revocation
    // -----------------------------------------------------------------
    private static void scenarioActivePointsBelowThreshold(KieContainer kc) {
        System.out.println("\n--- SCENARIO G: Standard driver, 11 active points (below 18 -> no revocation) ---");
        KieSession session = kc.newKieSession("sanctionsKsession");

        Driver d = new Driver("D101", "Marko Markovic", 8, 0,
            LocalDate.of(1995, 5, 10), LocalDate.of(2013, 6, 1)); // standard license

        EntryPoint pointsStream = session.getEntryPoint("pointsStream");
        // History within the 24-month window
        pointsStream.insert(new PointPenalty(d, 4, daysAgo(400), "VIO-001"));
        pointsStream.insert(new PointPenalty(d, 7, daysAgo(180), "VIO-002"));

        session.insert(d);
        session.fireAllRules();

        printCepOutcome(session);
        session.dispose();
    }

    // -----------------------------------------------------------------
    // Scenario H: standard license driver crosses 18 points threshold -> REVOCATION
    // -----------------------------------------------------------------
    private static void scenarioStandardLicenseRevocation(KieContainer kc) {
        System.out.println("\n--- SCENARIO H: Standard driver crosses 18-point threshold -> REVOCATION ---");
        KieSession session = kc.newKieSession("sanctionsKsession");

        Driver d = new Driver("D102", "Petar Petrovic", 15, 0,
            LocalDate.of(1985, 3, 20), LocalDate.of(2005, 8, 15));

        EntryPoint pointsStream = session.getEntryPoint("pointsStream");
        pointsStream.insert(new PointPenalty(d, 6,  daysAgo(600), "VIO-101"));
        pointsStream.insert(new PointPenalty(d, 7,  daysAgo(300), "VIO-102"));
        pointsStream.insert(new PointPenalty(d, 6,  daysAgo(60),  "VIO-103"));
        // Total active: 6+7+6 = 19 >= 18 -> revocation

        session.insert(d);
        session.fireAllRules();

        printCepOutcome(session);
        session.dispose();
    }

    // -----------------------------------------------------------------
    // Scenario I: probationary license driver crosses 9 points threshold -> REVOCATION
    // -----------------------------------------------------------------
    private static void scenarioProbationaryLicenseRevocation(KieContainer kc) {
        System.out.println("\n--- SCENARIO I: Probationary driver crosses 9-point threshold -> REVOCATION ---");
        KieSession session = kc.newKieSession("sanctionsKsession");

        // Young driver, got license 6 months ago -> still in probation
        Driver d = new Driver("D103", "Ana Anic", 1, 0,
            LocalDate.now().minusYears(20),
            LocalDate.now().minusMonths(6));

        EntryPoint pointsStream = session.getEntryPoint("pointsStream");
        pointsStream.insert(new PointPenalty(d, 4, daysAgo(120), "VIO-201"));
        pointsStream.insert(new PointPenalty(d, 6, daysAgo(30),  "VIO-202"));
        // Total active: 4+6 = 10 >= 9 -> probationary revocation

        session.insert(d);
        session.fireAllRules();

        printCepOutcome(session);
        session.dispose();
    }

    // -----------------------------------------------------------------
    // Scenario J: old points (older than 24 months) are auto-expired -> no revocation
    //   even though they would total >= 18 if NOT expired.
    // Demonstrates the CEP @Expires("730d") behaviour.
    // -----------------------------------------------------------------
    private static void scenarioOldPointsExpire(KieContainer kc) {
        System.out.println("\n--- SCENARIO J: Old points (>24mo) auto-expire -> no revocation ---");
        KieSession session = kc.newKieSession("sanctionsKsession");

        Driver d = new Driver("D104", "Nikola Nikolic", 10, 0,
            LocalDate.of(1980, 1, 1), LocalDate.of(2000, 1, 1));

        EntryPoint pointsStream = session.getEntryPoint("pointsStream");
        // Old (expired) penalties - 800+ days old, outside the 24mo window
        pointsStream.insert(new PointPenalty(d, 10, daysAgo(800), "VIO-OLD-1"));
        pointsStream.insert(new PointPenalty(d, 10, daysAgo(900), "VIO-OLD-2"));
        // Current (active) penalty
        pointsStream.insert(new PointPenalty(d, 4,  daysAgo(60),  "VIO-CURRENT"));
        // Active total: only 4 (the two 10-pt entries are outside the 730d window)

        session.insert(d);
        session.fireAllRules();

        printCepOutcome(session);
        session.dispose();
    }

    /** Helper: returns a java.util.Date for "today minus N days". */
    private static Date daysAgo(int days) {
        return Date.from(LocalDate.now().minusDays(days)
            .atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /** Prints active-points snapshot and any license revocation. */
    private static void printCepOutcome(KieSession s) {
        for (Object o : s.getObjects()) {
            if (o instanceof ActivePointsSnapshot) {
                ActivePointsSnapshot snap = (ActivePointsSnapshot) o;
                System.out.println("\n  >>> CEP ACTIVE POINTS <<<");
                System.out.println("    Driver:          " + snap.getDriver().getFullName());
                System.out.println("    Probationary:    " + snap.getDriver().isProbationary(LocalDate.now()));
                System.out.println("    Active points:   " + snap.getActivePoints()
                                    + " (within last 24 months)");
            }
        }
        boolean hadRevocation = false;
        for (Object o : s.getObjects()) {
            if (o instanceof LicenseRevocation) {
                LicenseRevocation rev = (LicenseRevocation) o;
                hadRevocation = true;
                System.out.println("\n  >>> LICENSE REVOCATION <<<");
                System.out.println("    Driver:           " + rev.getDriver().getFullName());
                System.out.println("    License type:     "
                                    + (rev.isProbationary() ? "probationary (9-point threshold)"
                                                            : "standard (18-point threshold)"));
                System.out.println("    Points at revoke: " + rev.getActivePointsAtRevocation());
                System.out.println("    Reason:           " + rev.getReason());
            }
        }
        if (!hadRevocation) {
            System.out.println("    -> No license revocation (under threshold).");
        }
    }

    // -----------------------------------------------------------------
    // Output helper: print every individual Sanction, then the SanctionSummary.
    // -----------------------------------------------------------------
    private static void printOutcome(KieSession s) {
        int i = 0;
        for (Object o : s.getObjects()) {
            if (o instanceof Sanction) {
                Sanction sanc = (Sanction) o;
                i++;
                System.out.println("\n  Sanction #" + i);
                System.out.println("    Severity:        " + sanc.getSeverity());
                System.out.println("    Law article:     " + sanc.getLawArticle());
                System.out.println("    Prescribed fine: " + sanc.getMinFine() + " - " + sanc.getMaxFine() + " RSD");
                System.out.println("    Final points:    " + sanc.getFinalPoints());
                if (sanc.getDrivingBanDays() > 0) {
                    System.out.println("    Driving ban:     " + sanc.getDrivingBanDays() + " days");
                }
                if (sanc.getPrisonDaysMax() > 0) {
                    System.out.println("    Prison:          " + sanc.getPrisonDaysMin() + "-" + sanc.getPrisonDaysMax() + " days");
                }
                if (sanc.isAlternativePenalty()) {
                    System.out.println("    Penalty type:    alternative (fine OR prison)");
                }
                for (String line : sanc.getExplanation()) {
                    System.out.println("    " + line);
                }
            }
        }

        for (Object o : s.getObjects()) {
            if (o instanceof SanctionSummary) {
                SanctionSummary summary = (SanctionSummary) o;
                System.out.println("\n  >>> SUMMARY <<<");
                System.out.println("    Driver:          " + summary.getDriver().getFullName());
                System.out.println("    # of sanctions:  " + summary.getSanctions().size());
                System.out.println("    TOTAL FINE:      " + summary.getTotalFineMin() + "-"
                                                            + summary.getTotalFineMax() + " RSD");
                System.out.println("    TOTAL POINTS:    " + summary.getTotalPoints());
                if (summary.getDrivingBanMaxDays() > 0) {
                    System.out.println("    Driving ban:     "
                            + summary.getDrivingBanMinDays() + " - "
                            + summary.getDrivingBanMaxDays() + " days"
                            + " (min mandatory -> legal cap 5 years)");
                }
                if (summary.getPrisonMaxDays() > 0) {
                    System.out.println("    Prison:          "
                            + summary.getPrisonMinDays() + " - "
                            + summary.getPrisonMaxDays() + " days"
                            + " (sum/2 -> full sum)");
                }
                for (String line : summary.getOverallExplanation()) {
                    System.out.println("    " + line);
                }
            }
        }
    }
}
