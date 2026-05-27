package com.ftn.sbnz.model.fault;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class ParticipantAction implements Serializable {

    private static final long serialVersionUID = 1L;

    private Participant participant;
    private ParticipantActionType type;
    private LocalDateTime timestamp;

    public ParticipantAction() {}

    public ParticipantAction(Participant participant, ParticipantActionType type, LocalDateTime timestamp) {
        this.participant = participant;
        this.type = type;
        this.timestamp = timestamp;
    }

    public Participant getParticipant() { return participant; }
    public void setParticipant(Participant participant) { this.participant = participant; }

    public ParticipantActionType getType() { return type; }
    public void setType(ParticipantActionType type) { this.type = type; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public long getTimestampMs() {
        if (timestamp == null) return System.currentTimeMillis();
        return timestamp.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
