package nur.edu.nurtricenter_patient.domain.patient.events;

import java.util.UUID;

import nur.edu.nurtricenter_patient.core.abstractions.DomainEvent;

public class PatientUpdatedEvent extends DomainEvent {
  private final UUID patientId;
  private final String nombre;
  private final String documento;
  private final UUID suscripcionId;

  public PatientUpdatedEvent(UUID patientId, String nombre, String documento, UUID suscripcionId) {
    this.patientId = patientId;
    this.nombre = nombre;
    this.documento = documento;
    this.suscripcionId = suscripcionId;
  }

  public UUID getPatientId() {
    return patientId;
  }

  public String getNombre() {
    return nombre;
  }

  public String getDocumento() {
    return documento;
  }

  public UUID getSuscripcionId() {
    return suscripcionId;
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
    return "paciente.paciente-actualizado";
  }

  @Override
  public Object getPayload() {
    return new Payload(patientId.toString(), nombre, documento, suscripcionId.toString());
  }

  private record Payload(String pacienteId, String nombre, String documento, String suscripcionId) {}
}
