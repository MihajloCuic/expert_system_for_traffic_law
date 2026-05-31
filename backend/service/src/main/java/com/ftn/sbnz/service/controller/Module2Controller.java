package com.ftn.sbnz.service.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.EntryPoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ftn.sbnz.model.fault.Accident;
import com.ftn.sbnz.model.fault.Blame;
import com.ftn.sbnz.model.fault.Fault;
import com.ftn.sbnz.model.fault.Participant;
import com.ftn.sbnz.model.fault.ParticipantAction;
import com.ftn.sbnz.service.dto.FaultRequest;
import com.ftn.sbnz.service.dto.FaultResponse;

@RestController
@RequestMapping("/api/module2")
public class Module2Controller {

    private final KieContainer kieContainer;

    public Module2Controller(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    @PostMapping("/determine-fault")
    public ResponseEntity<FaultResponse> determineFault(@RequestBody FaultRequest request) {
        KieSession session = kieContainer.newKieSession("faultKsession");
        try {
            Accident accident = request.getAccident();

            // Build canonical participant map (by ID) for reference normalization
            Map<String, Participant> participantMap = new HashMap<>();
            if (accident != null && accident.getParticipants() != null) {
                for (Participant p : accident.getParticipants()) {
                    if (p.getId() != null) participantMap.put(p.getId(), p);
                }
            }

            // Normalize participant references in actions to canonical objects
            if (request.getActions() != null) {
                for (ParticipantAction action : request.getActions()) {
                    if (action.getParticipant() != null
                            && action.getParticipant().getId() != null) {
                        Participant canonical = participantMap.get(
                                action.getParticipant().getId());
                        if (canonical != null) action.setParticipant(canonical);
                    }
                }
            }

            // Normalize participant references in declared faults
            if (request.getDeclaredFaults() != null) {
                for (Fault fault : request.getDeclaredFaults()) {
                    if (fault.getParticipant() != null
                            && fault.getParticipant().getId() != null) {
                        Participant canonical = participantMap.get(
                                fault.getParticipant().getId());
                        if (canonical != null) fault.setParticipant(canonical);
                    }
                }
            }

            // Insert AccidentScene
            if (accident != null && accident.getScene() != null) {
                session.insert(accident.getScene());
            }

            // Insert Participants
            if (accident != null && accident.getParticipants() != null) {
                for (Participant p : accident.getParticipants()) {
                    session.insert(p);
                }
            }

            // Insert pre-declared faults (if any)
            if (request.getDeclaredFaults() != null) {
                for (Fault fault : request.getDeclaredFaults()) {
                    session.insert(fault);
                }
            }

            // Insert ParticipantAction events into the CEP entry-point
            // Insert ParticipantAction events into the CEP entry-point
            EntryPoint manevriStream = session.getEntryPoint("manevriStream");
            if (manevriStream != null && request.getActions() != null) {
                for (ParticipantAction action : request.getActions()) {
                    System.out.println("[CONTROLLER] inserting: participant="
                            + action.getParticipant().getId()
                            + " type=" + action.getType()
                            + " tsMs=" + action.getTimestampMs());
                    manevriStream.insert(action);
                }
            }

            session.fireAllRules();

            // Collect all Blame objects from working memory
            List<FaultResponse.BlameDto> blameDtos = new ArrayList<>();
            for (Object obj : session.getObjects(new ClassObjectFilter(Blame.class))) {
                blameDtos.add(FaultResponse.BlameDto.from((Blame) obj));
            }

            FaultResponse response = new FaultResponse();
            response.setBlames(blameDtos);
            return ResponseEntity.ok(response);
        } finally {
            session.dispose();
        }
    }
}
