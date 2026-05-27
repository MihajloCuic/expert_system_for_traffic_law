package com.ftn.sbnz.service.dto;

import java.util.ArrayList;
import java.util.List;

import com.ftn.sbnz.model.fault.Blame;

public class FaultResponse {

    private List<BlameDto> blames = new ArrayList<>();

    public FaultResponse() {}

    public List<BlameDto> getBlames() { return blames; }
    public void setBlames(List<BlameDto> blames) { this.blames = blames; }

    public static class BlameDto {
        private String participantId;
        private int percentage;
        private String type;
        private List<String> reasoning;

        public BlameDto() {}

        public static BlameDto from(Blame b) {
            BlameDto d = new BlameDto();
            d.participantId = b.getParticipant().getId();
            d.percentage = b.getPercentage();
            d.type = b.getType().name();
            d.reasoning = new ArrayList<>(b.getReasoning());
            return d;
        }

        public String getParticipantId() { return participantId; }
        public void setParticipantId(String participantId) { this.participantId = participantId; }

        public int getPercentage() { return percentage; }
        public void setPercentage(int percentage) { this.percentage = percentage; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public List<String> getReasoning() { return reasoning; }
        public void setReasoning(List<String> reasoning) { this.reasoning = reasoning; }
    }
}
