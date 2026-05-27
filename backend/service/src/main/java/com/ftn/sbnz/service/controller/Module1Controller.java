package com.ftn.sbnz.service.controller;

import org.kie.api.runtime.KieSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Optional;

import com.ftn.sbnz.model.sanctions.ActivePointsSnapshot;
import com.ftn.sbnz.model.sanctions.Driver;
import com.ftn.sbnz.model.sanctions.DrivingLicense;
import com.ftn.sbnz.model.sanctions.LicenseRevocation;
import com.ftn.sbnz.model.sanctions.Sanction;
import com.ftn.sbnz.model.sanctions.SanctionSummary;
import com.ftn.sbnz.model.shared.Vehicle;
import com.ftn.sbnz.model.shared.VehicleCategory;
import com.ftn.sbnz.model.sanctions.Violation;
import com.ftn.sbnz.model.sanctions.ViolationContext;
import com.ftn.sbnz.model.sanctions.ViolationType;
import com.ftn.sbnz.service.dto.IssueSanctionRequest;
import com.ftn.sbnz.service.dto.IssueSanctionResponse;
import com.ftn.sbnz.service.repository.DriverRepository;
import com.ftn.sbnz.service.service.DroolsSessionService;

@RestController
@RequestMapping("/api/module1")
public class Module1Controller {

    /**
     * Threshold above which the breathalyser reading is considered an
     * ALCOHOL violation that the engine should sanction. Below 0.20 per
     * mille is a "trace" reading that the law tolerates.
     */
    private static final double ALCOHOL_VIOLATION_THRESHOLD = 0.20;

    private final DroolsSessionService droolsSessionService;
    private final DriverRepository driverRepository;

    public Module1Controller(DroolsSessionService droolsSessionService,
                             DriverRepository driverRepository) {
        this.droolsSessionService = droolsSessionService;
        this.driverRepository = driverRepository;
    }

    // ============================================================
    // POST /api/module1/issue-sanction
    //
    //   1. Find-or-create Driver by JMBG (and his license by licenseNumber,
    //      if the officer provided one).
    //   2. Load Driver's PointPenalty history + any pre-existing
    //      LicenseRevocation into the Drools session.
    //   3. Insert all explicit Violations + auto-add an ALCOHOL violation
    //      when bacAtStop > 0.20.
    //   4. fireAllRules - the 8 layers run.
    //   5. Persist newly produced PointPenalty (Layer 6) and
    //      LicenseRevocation (Layer 8) to H2.
    //   6. Return the full IssueSanctionResponse.
    // ============================================================
    @PostMapping("/issue-sanction")
    public ResponseEntity<IssueSanctionResponse> issueSanction(
            @RequestBody IssueSanctionRequest request) {

        if (request.getDriver() == null || request.getDriver().getId() == null) {
            return ResponseEntity.badRequest().build();
        }

        Driver driver = findOrCreateDriver(request.getDriver());

        Vehicle vehicle = request.getVehicle() != null
                ? request.getVehicle()
                : new Vehicle(VehicleCategory.CAR);

        KieSession session = droolsSessionService.openSessionFor(driver);
        try {
            // Officer-supplied violations
            for (IssueSanctionRequest.ViolationInput vi : request.getViolations()) {
                insertViolation(session, driver, vehicle, vi);
            }

            // Auto-add ALCOHOL violation when BAC at stop is above the threshold
            if (request.getBacAtStop() != null
                    && request.getBacAtStop() > ALCOHOL_VIOLATION_THRESHOLD) {
                Violation v = new Violation(driver, vehicle,
                        ViolationType.ALCOHOL, null);
                v.setBloodAlcoholLevel(request.getBacAtStop());
                session.insert(v);
                System.out.println("[CTRL] Auto-added ALCOHOL violation"
                        + " (BAC " + request.getBacAtStop() + " > "
                        + ALCOHOL_VIOLATION_THRESHOLD + ")");
            }

            session.fireAllRules();
            droolsSessionService.persistNew(session, driver);

            return ResponseEntity.ok(buildResponse(driver, session));
        } finally {
            session.dispose();
        }
    }

    // ============================================================
    // GET /api/module1/driver/{driverId}/status
    //   Returns the driver's current snapshot without changing anything.
    //   driverId here is the JMBG.
    // ============================================================
    @GetMapping("/driver/{driverId}/status")
    public ResponseEntity<IssueSanctionResponse> driverStatus(
            @PathVariable("driverId") String driverId) {

        Optional<Driver> maybeDriver = driverRepository.findById(driverId);
        if (maybeDriver.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Driver driver = maybeDriver.get();

        KieSession session = droolsSessionService.openSessionFor(driver);
        try {
            session.fireAllRules();
            return ResponseEntity.ok(buildResponse(driver, session));
        } finally {
            session.dispose();
        }
    }

    // ============================================================
    // GET /api/module1/driver/by-license/{licenseNumber}
    //   Roadside lookup: officer types the number printed on the license
    //   card, system returns the driver record (or 404 if unknown).
    //   Used by the frontend to pre-fill the issue-sanction form.
    // ============================================================
    @GetMapping("/driver/by-license/{licenseNumber}")
    public ResponseEntity<IssueSanctionResponse> driverByLicense(
            @PathVariable("licenseNumber") String licenseNumber) {

        Optional<Driver> maybeDriver =
                driverRepository.findByLicense_LicenseNumber(licenseNumber);
        if (maybeDriver.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Driver driver = maybeDriver.get();

        // We open a Drools session only to compute the active-points
        // snapshot + check for a pre-existing LicenseRevocation - the
        // frontend may want to warn the officer up-front ("this driver
        // is already under a ban").
        KieSession session = droolsSessionService.openSessionFor(driver);
        try {
            session.fireAllRules();
            return ResponseEntity.ok(buildResponse(driver, session));
        } finally {
            session.dispose();
        }
    }

    // ============================================================
    // Helpers
    // ============================================================

    /**
     * If a Driver with the supplied JMBG already exists, return that row
     * (preserving past PointPenalty / LicenseRevocation history). Otherwise
     * create + save a new Driver from the request payload. The optional
     * license sub-object is treated the same way - reuse if present in
     * the DB, otherwise create.
     */
    private Driver findOrCreateDriver(IssueSanctionRequest.DriverInput input) {
        Optional<Driver> existing = driverRepository.findById(input.getId());
        if (existing.isPresent()) {
            // Update the license if the officer provided new license info
            // and the driver did not have one before, or has a different one now.
            Driver d = existing.get();
            DrivingLicense newLicense = buildLicenseFromInput(input.getLicense());
            if (newLicense != null
                    && (d.getLicense() == null
                        || !newLicense.getLicenseNumber().equals(
                            d.getLicense().getLicenseNumber()))) {
                d.setLicense(newLicense);
                driverRepository.save(d);
            }
            return d;
        }

        Driver d = new Driver();
        d.setId(input.getId());
        d.setFirstName(input.getFirstName());
        d.setLastName(input.getLastName());
        d.setDateOfBirth(input.getDateOfBirth());
        d.setLicense(buildLicenseFromInput(input.getLicense()));
        return driverRepository.save(d);
    }

    private DrivingLicense buildLicenseFromInput(IssueSanctionRequest.LicenseInput li) {
        if (li == null || li.getLicenseNumber() == null) return null;
        DrivingLicense l = new DrivingLicense();
        l.setLicenseNumber(li.getLicenseNumber());
        l.setIssuedAt(li.getIssuedAt());
        l.setType(li.getType());
        l.setCategories(li.getCategories() != null
                ? new HashSet<>(li.getCategories())
                : new HashSet<>());
        return l;
    }

    private void insertViolation(KieSession session, Driver driver, Vehicle vehicle,
                                 IssueSanctionRequest.ViolationInput vi) {
        Violation v = new Violation(driver, vehicle, vi.getType(), vi.getLocation());
        if (vi.getSpeedOverLimitKmH() != null) {
            v.setSpeedOverLimitKmH(vi.getSpeedOverLimitKmH());
        }
        if (vi.getBloodAlcoholLevel() != null) {
            v.setBloodAlcoholLevel(vi.getBloodAlcoholLevel());
        }
        session.insert(v);

        if (vi.isCausedAccident() || vi.isInjuredPersons() || vi.isPedestrianIsChild()) {
            ViolationContext ctx = new ViolationContext(v);
            ctx.setCausedAccident(vi.isCausedAccident());
            ctx.setInjuredPersons(vi.isInjuredPersons());
            ctx.setPedestrianIsChild(vi.isPedestrianIsChild());
            session.insert(ctx);
        }
    }

    private IssueSanctionResponse buildResponse(Driver driver, KieSession session) {
        IssueSanctionResponse resp = new IssueSanctionResponse();
        resp.setDriver(IssueSanctionResponse.DriverDto.from(driver));
        resp.setProbationary(driver.isProbationary());

        for (Object o : session.getObjects()) {
            if (o instanceof Sanction sanc) {
                resp.getSanctions().add(IssueSanctionResponse.SanctionDto.from(sanc));
            } else if (o instanceof SanctionSummary summary
                    && summary.getDriver() != null
                    && summary.getDriver().getId().equals(driver.getId())) {
                resp.setSummary(IssueSanctionResponse.SummaryDto.from(summary));
            } else if (o instanceof ActivePointsSnapshot snap
                    && snap.getDriver() != null
                    && snap.getDriver().getId().equals(driver.getId())) {
                resp.setActivePoints(IssueSanctionResponse.ActivePointsDto.from(snap));
            } else if (o instanceof LicenseRevocation rev
                    && rev.getDriver() != null
                    && rev.getDriver().getId().equals(driver.getId())) {
                resp.setRevocation(IssueSanctionResponse.RevocationDto.from(rev));
            }
        }
        return resp;
    }
}
