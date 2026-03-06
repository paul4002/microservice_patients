package nur.edu.nurtricenter_patient.core.abstractions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public abstract class AggregateRoot extends Entity {

  private final List<DomainEvent> domainEvents = new ArrayList<>();

  protected AggregateRoot(UUID id) {
    super(id);
  }

  public List<DomainEvent> getDomainEvents() {
    return Collections.unmodifiableList(domainEvents);
  }

  // Protected: solo el propio aggregate puede emitir sus eventos
  protected void addDomainEvent(DomainEvent event) {
    domainEvents.add(event);
  }

  public void clearDomainEvents() {
    domainEvents.clear();
  }
}
