package nur.edu.nurtricenter_patient.core.abstractions;

import java.time.LocalDateTime;
import java.util.UUID;

import an.awesome.pipelinr.Notification;

public abstract class DomainEvent implements Notification {
    private final UUID id;
    private final LocalDateTime occurredOn;

    protected DomainEvent() {
        this.id = UUID.randomUUID();
        this.occurredOn = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}
