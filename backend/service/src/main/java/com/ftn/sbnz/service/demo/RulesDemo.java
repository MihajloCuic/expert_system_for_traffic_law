package com.ftn.sbnz.service.demo;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import com.ftn.sbnz.model.Driver;
import com.ftn.sbnz.model.Location;
import com.ftn.sbnz.model.Sanction;
import com.ftn.sbnz.model.Vehicle;
import com.ftn.sbnz.model.VehicleCategory;
import com.ftn.sbnz.model.Violation;
import com.ftn.sbnz.model.ViolationContext;
import com.ftn.sbnz.model.ViolationType;

/**
 * Standalone demo for homework #3.
 * Right-click -> Run 'RulesDemo.main()' in the IDE.
 * Does not start Spring Boot - only loads the Drools knowledge base from the classpath.
 */
public class RulesDemo {

    public static void main(String[] args) {
        KieServices ks = KieServices.Factory.get();
        KieContainer kc = ks.getKieClasspathContainer();

        System.out.println("\n========== EXPERT SYSTEM FOR TRAFFIC LAW ==========");
        System.out.println("Module 1 - Fine calculation (homework #3 demo)\n");

        runExample1(kc);
        runExample2(kc);

        System.out.println("\n=====================================================");
    }

    /**
     * Example 1 - chains 3 rules across 2 layers:
     *   Layer 1: Speeding 21-40 km/h in urban area  -> base fine 15000, 5 points
     *   Layer 2: Aggravating - school zone          -> +20% fine, +1 point
     *   Layer 2: Aggravating - fled from scene      -> +30% fine, +2 points
     */
    private static void runExample1(KieContainer kc) {
        System.out.println("--- EXAMPLE 1: Speeding + school zone + fled from scene ---");
        KieSession session = kc.newKieSession("sanctionsKsession");

        Driver d1 = new Driver("D001", "John Smith", 8, 6);
        Vehicle veh1 = new Vehicle("NS-123-AB", VehicleCategory.CAR);

        Violation v1 = new Violation(d1, veh1, ViolationType.SPEEDING, Location.URBAN);
        v1.setSpeedOverLimitKmH(35);

        ViolationContext ctx1 = new ViolationContext(v1);
        ctx1.setFledFromScene(true);
        ctx1.setSchoolZone(true);

        session.insert(v1);
        session.insert(ctx1);
        session.fireAllRules();

        printSanctions(session);
        session.dispose();
    }

    /**
     * Example 2 - chains 2 rules across 2 layers:
     *   Layer 1: Alcohol 0.5-1.0 per mille  -> base fine 30000, 6 points
     *   Layer 2: Mitigating - self-reported -> -15% fine
     */
    private static void runExample2(KieContainer kc) {
        System.out.println("\n--- EXAMPLE 2: Alcohol 0.7 per mille + self-reported ---");
        KieSession session = kc.newKieSession("sanctionsKsession");

        Driver d2 = new Driver("D002", "Peter Brown", 15, 2);
        Vehicle veh2 = new Vehicle("NS-987-XY", VehicleCategory.CAR);

        Violation v2 = new Violation(d2, veh2, ViolationType.ALCOHOL, Location.OPEN_ROAD);
        v2.setBloodAlcoholLevel(0.7);

        ViolationContext ctx2 = new ViolationContext(v2);
        ctx2.setSelfReported(true);

        session.insert(v2);
        session.insert(ctx2);
        session.fireAllRules();

        printSanctions(session);
        session.dispose();
    }

    private static void printSanctions(KieSession s) {
        for (Object o : s.getObjects()) {
            if (o instanceof Sanction) {
                Sanction sanc = (Sanction) o;
                System.out.println("\n>>> REASONING RESULT <<<");
                System.out.println("  Severity:     " + sanc.getSeverity());
                System.out.println("  Law article:  " + sanc.getLawArticle());
                System.out.println("  Base fine:    " + sanc.getBaseFine() + " RSD");
                System.out.println("  Base points:  " + sanc.getBasePoints());
                System.out.println("  FINAL FINE:   " + sanc.getFinalFine() + " RSD");
                System.out.println("  FINAL POINTS: " + sanc.getFinalPoints());
                System.out.println("  Explanation (chain of fired rules):");
                for (String line : sanc.getExplanation()) {
                    System.out.println("    " + line);
                }
            }
        }
    }
}
