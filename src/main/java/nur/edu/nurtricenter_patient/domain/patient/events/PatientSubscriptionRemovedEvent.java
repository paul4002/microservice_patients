package nur.edu.nurtricenter_patient.domain.patient.events;

import java.util.UUID;

import nur.edu.nurtricenter_patient.core.abstractions.DomainEvent;

public class PatientSubscriptionRemovedEvent extends DomainEvent {
  private final UUID patientId;
  private final UUID previousSubscriptionId;
  private final String reason;
  private final String sourceEvent;

  public PatientSubscriptionRemovedEvent(UUID patientId, UUID previousSubscriptionId, String reason, String sourceEvent) {
    this.patientId = patientId;
    this.previousSubscriptionId = previousSubscriptionId;
    this.reason = reason;
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
    return "paciente.suscripcion-eliminada";
  }

  @Override
  public Object getPayload() {
    return new Payload(
      patientId.toString(),
      previousSubscriptionId != null ? previousSubscriptionId.toString() : null,
      reason,
      sourceEvent
    );
  }

  private record Payload(
    String pacienteId,
    String suscripcionIdAnterior,
    String motivo,
    String eventoOrigen
  ) {}
}

