package nur.edu.nurtricenter_patient.core.abstractions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public abstract class Entity {

    protected UUID id;
    private final List<DomainEvent> domainEvents;

    protected Entity(UUID id) {
        if (id == null || id.equals(new UUID(0, 0))) {
            throw new IllegalArgumentException("Id cannot be empty");
        }
        this.id = id;
        this.domainEvents = new ArrayList<>();
    }

    protected Entity() {
        this.domainEvents = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public List<DomainEvent> getDomainEvents() {
        // Devuelve una vista inmutable para proteger la lista interna
        return Collections.unmodifiableList(domainEvents);
    }

    public void addDomainEvent(DomainEvent domainEvent) {
        domainEvents.add(domainEvent);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}

