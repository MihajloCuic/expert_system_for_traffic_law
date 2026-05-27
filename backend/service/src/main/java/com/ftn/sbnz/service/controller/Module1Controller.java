package com.ftn.sbnz.service.controller;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.ftn.sbnz.model.ActivePointsSnapshot;
import com.ftn.sbnz.model.Driver;
import com.ftn.sbnz.model.LicenseRevocation;
import com.ftn.sbnz.model.Sanction;
import com.ftn.sbnz.model.SanctionSummary;
import com.ftn.sbnz.model.Vehicle;
import com.ftn.sbnz.model.VehicleCategory;
import com.ftn.sbnz.model.Violation;
import com.ftn.sbnz.model.ViolationContext;
import com.ftn.sbnz.service.dto.FineRequest;
import com.ftn.sbnz.service.dto.FineResponse;
import com.ftn.sbnz.service.dto.IssueSanctionRequest;
import com.ftn.sbnz.service.dto.IssueSanctionResponse;
import com.ftn.sbnz.service.repository.DriverRepository;
import com.ftn.sbnz.service.service.DroolsSessionService;

@RestController
@RequestMapping("/api/module1")
public class Module1Controller {

    private final DroolsSessionService droolsSessionService;
    private final DriverRepository driverRepository;

    public Module1Controller(DroolsSessionService droolsSessionService,
                             DriverRepository driverRepository) {
        this.droolsSessionService = droolsSessionService;
        this.driverRepository = driverRepository;
    }

    @PostMapping("/issue-sanction")
    public ResponseEntity<IssueSanctionResponse> issueSanction(
            @RequestBody IssueSanctionRequest request) {

        Optional<Driver> maybeDriver = driverRepository.findById(request.getDriverId());
        if (maybeDriver.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Driver driver = maybeDriver.get();

        KieSession session = droolsSessionService.openSessionFor(driver);

        Vehicle vehicle = request.getVehicle() != null
                ? request.getVehicle()
                : new Vehicle(VehicleCategory.CAR);

        try {
            for (IssueSanctionRequest.ViolationInput vi : request.getViolations()) {
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

            session.fireAllRules();
            droolsSessionService.persistNew(session, driver);

            return ResponseEntity.ok(buildResponse(driver, session));
        } finally {
            session.dispose();
        }
    }

    // -----------------------------------------------------------------
    // Convenience read endpoint - returns the driver's current snapshot
    // without changing anything in the database. Useful for the UI.
    // -----------------------------------------------------------------
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

    // -----------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------

    private IssueSanctionResponse buildResponse(Driver driver, KieSession session) {
        IssueSanctionResponse resp = new IssueSanctionResponse();
        resp.setDriverId(driver.getId());
        resp.setDriverFullName(driver.getFullName());
        resp.setProbationary(driver.isProbationary(LocalDate.now()));

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
