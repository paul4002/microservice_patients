package nur.edu.nurtricenter_patient.domain.patient.events;

import java.time.LocalDate;
import java.util.UUID;

import nur.edu.nurtricenter_patient.core.abstractions.DomainEvent;
import nur.edu.nurtricenter_patient.domain.patient.SubscriptionStatus;

public class PatientSubscriptionUpdatedEvent extends DomainEvent {
  private final UUID patientId;
  private final UUID subscriptionId;
  private final SubscriptionStatus status;
  private final LocalDate subscriptionEndsOn;
  private final String sourceEvent;

  public PatientSubscriptionUpdatedEvent(UUID patientId, UUID subscriptionId, SubscriptionStatus status, LocalDate subscriptionEndsOn, String sourceEvent) {
    this.patientId = patientId;
    this.subscriptionId = subscriptionId;
    this.status = status;
    this.subscriptionEndsOn = subscriptionEndsOn;
    this.sourceEvent = sourceEvent;
  }

  @Override
  public String getAggregateType() {
    return "Patient";
  }

  @Override
  public String getAggregateId() {
    return patientId.toString();
  }

  @Override
  public String getEventName() {
    return "paciente.suscripcion-actualizada";
  }

  @Override
  public Object getPayload() {
    return new Payload(
      patientId.toString(),
      subscriptionId != null ? subscriptionId.toString() : null,
      status != null ? status.name() : null,
      subscriptionEndsOn != null ? subscriptionEndsOn.toString() : null,
      sourceEvent
    );
  }

  private record Payload(
    String pacienteId,
    String suscripcionId,
    String estadoSuscripcion,
    String fechaFinSuscripcion,
    String eventoOrigen
  ) {}
}

