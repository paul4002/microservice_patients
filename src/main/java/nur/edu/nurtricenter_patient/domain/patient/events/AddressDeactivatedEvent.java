package nur.edu.nurtricenter_patient.domain.patient.events;

import java.util.UUID;

import nur.edu.nurtricenter_patient.core.abstractions.DomainEvent;

public class AddressDeactivatedEvent extends DomainEvent {
  private final UUID patientId;
  private final UUID addressId;

  public AddressDeactivatedEvent(UUID patientId, UUID addressId) {
    this.patientId = patientId;
    this.addressId = addressId;
  }

  public UUID getPatientId() { return patientId; }
  public UUID getAddressId() { return addressId; }

  @Override public String getAggregateType() { return "Patient"; }
  @Override public String getAggregateId() { return patientId.toString(); }
  @Override public String getEventName() { return "paciente.direccion-desactivada"; }

  @Override
  public Object getPayload() {
    return new Payload(patientId.toString(), addressId.toString());
  }

  private record Payload(String pacienteId, String direccionId) {}
}
