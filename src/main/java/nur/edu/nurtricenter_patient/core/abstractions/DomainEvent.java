package nur.edu.nurtricenter_patient.core.abstractions;

import java.time.LocalDateTime;
import java.time.Clock;
import java.util.UUID;

import an.awesome.pipelinr.Notification;

public abstract class DomainEvent implements Notification {
    private final UUID id;
    private final LocalDateTime occurredOn;

    protected DomainEvent() {
        this.id = UUID.randomUUID();
        this.occurredOn = LocalDateTime.now(Clock.systemUTC());
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }

    public abstract String getAggregateType();

    public abstract String getAggregateId();

    public String getEventType() {
        return getClass().getSimpleName();
    }

    public abstract String getEventName();

    public abstract Object getPayload();
}
