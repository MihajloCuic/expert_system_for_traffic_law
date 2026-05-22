package com.ftn.sbnz.service.controller;

import org.kie.api.runtime.KieContainer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ftn.sbnz.service.dto.FaultRequest;
import com.ftn.sbnz.service.dto.FaultResponse;

@RestController
@RequestMapping("/api/module2")
public class Module2Controller {

    private final KieContainer kieContainer;

    public Module2Controller(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    // Placeholder. Rules for fault determination will be implemented by Milos.
    // Endpoint exposes the API surface so the Angular client can be developed in parallel.
    @PostMapping("/determine-fault")
    public ResponseEntity<FaultResponse> determineFault(@RequestBody FaultRequest request) {
        FaultResponse stub = new FaultResponse();
        return ResponseEntity.ok(stub);
    }
}
