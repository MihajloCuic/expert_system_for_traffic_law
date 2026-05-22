package com.ftn.sbnz.service.controller;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ftn.sbnz.model.Sanction;
import com.ftn.sbnz.service.dto.FineRequest;
import com.ftn.sbnz.service.dto.FineResponse;

@RestController
@RequestMapping("/api/module1")
public class Module1Controller {

    private final KieContainer kieContainer;

    public Module1Controller(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    @PostMapping("/calculate-fine")
    public ResponseEntity<FineResponse> calculateFine(@RequestBody FineRequest request) {
        KieSession session = kieContainer.newKieSession("sanctionsKsession");
        try {
            if (request.getViolation() != null) {
                if (request.getDriver() != null) {
                    request.getViolation().setDriver(request.getDriver());
                }
                if (request.getVehicle() != null) {
                    request.getViolation().setVehicle(request.getVehicle());
                }
                session.insert(request.getViolation());
            }
            if (request.getContext() != null) {
                if (request.getContext().getViolation() == null) {
                    request.getContext().setViolation(request.getViolation());
                }
                session.insert(request.getContext());
            }
            session.fireAllRules();

            for (Object o : session.getObjects()) {
                if (o instanceof Sanction) {
                    return ResponseEntity.ok(FineResponse.from((Sanction) o));
                }
            }
            return ResponseEntity.ok(new FineResponse());
        } finally {
            session.dispose();
        }
    }
}
