package nur.edu.nurtricenter_patient.domain.patient.events;

import java.util.UUID;

import nur.edu.nurtricenter_patient.core.abstractions.DomainEvent;

public class AddressGeocodedEvent extends DomainEvent {
  private final UUID patientId;
  private final UUID addressId;
  private final Double lat;
  private final Double lng;

  public AddressGeocodedEvent(UUID patientId, UUID addressId, Double lat, Double lng) {
    this.patientId = patientId;
    this.addressId = addressId;
    this.lat = lat;
    this.lng = lng;
  }

  public UUID getPatientId() {
    return patientId;
  }

  public UUID getAddressId() {
    return addressId;
  }

  public Double getLat() {
    return lat;
  }

  public Double getLng() {
    return lng;
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
    return "paciente.direccion-geocodificada";
  }

  @Override
  public Object getPayload() {
    return new Payload(addressId.toString(), new Geo(lat, lng));
  }

  private record Payload(String direccionId, Geo geo) {}
  private record Geo(Double lat, Double lng) {}
}
