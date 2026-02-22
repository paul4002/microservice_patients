package nur.edu.nurtricenter_patient.domain.patient.events;

import java.util.UUID;

import nur.edu.nurtricenter_patient.core.abstractions.DomainEvent;

public class PatientCreatedEvent extends DomainEvent {
  private final UUID patientId;
  private final String nombre;
  private final String documento;
  private final UUID suscripcionId;

  public PatientCreatedEvent(UUID patientId, String nombre, String documento, UUID suscripcionId) {
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
    return "paciente.paciente-creado";
  }

  @Override
  public Object getPayload() {
    return new Payload(patientId.toString(), nombre, documento, suscripcionId != null ? suscripcionId.toString() : null);
  }

  private record Payload(String pacienteId, String nombre, String documento, String suscripcionId) {}
}
