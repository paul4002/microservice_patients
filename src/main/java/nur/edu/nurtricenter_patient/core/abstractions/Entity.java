package nur.edu.nurtricenter_patient.core.abstractions;

import java.util.UUID;

public abstract class Entity {

    protected UUID id;

    protected Entity(UUID id) {
        if (id == null || id.equals(new UUID(0, 0))) {
            throw new IllegalArgumentException("Id cannot be empty");
        }
        this.id = id;
    }

    protected Entity() {}

    public UUID getId() {
        return id;
    }
}
