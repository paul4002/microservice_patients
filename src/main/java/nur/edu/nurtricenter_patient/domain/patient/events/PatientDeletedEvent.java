package nur.edu.nurtricenter_patient.domain.patient.events;

import java.util.UUID;

import nur.edu.nurtricenter_patient.core.abstractions.DomainEvent;

public class PatientDeletedEvent extends DomainEvent {
  private final UUID patientId;

  public PatientDeletedEvent(UUID patientId) {
    this.patientId = patientId;
  }

  public UUID getPatientId() {
    return patientId;
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
    return "paciente.paciente-eliminado";
  }

  @Override
  public Object getPayload() {
    return new Payload(patientId.toString());
  }

  private record Payload(String pacienteId) {}
}
