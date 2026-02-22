package nur.edu.nurtricenter_patient.domain.patient.events;

import java.util.UUID;

import nur.edu.nurtricenter_patient.core.abstractions.DomainEvent;

public class AddressUpdatedEvent extends DomainEvent {
  private final UUID patientId;
  private final UUID addressId;
  private final String nombre;
  private final String linea1;
  private final String linea2;
  private final String ciudad;
  private final String provincia;
  private final String pais;
  private final Double lat;
  private final Double lng;

  public AddressUpdatedEvent(UUID patientId, UUID addressId, String nombre, String linea1, String linea2, String ciudad, String provincia, String pais, Double lat, Double lng) {
    this.patientId = patientId;
    this.addressId = addressId;
    this.nombre = nombre;
    this.linea1 = linea1;
    this.linea2 = linea2;
    this.ciudad = ciudad;
    this.provincia = provincia;
    this.pais = pais;
    this.lat = lat;
    this.lng = lng;
  }

  public UUID getPatientId() {
    return patientId;
  }

  public UUID getAddressId() {
    return addressId;
  }

  public String getNombre() {
    return nombre;
  }

  public String getLinea1() {
    return linea1;
  }

  public String getLinea2() {
    return linea2;
  }

  public String getCiudad() {
    return ciudad;
  }

  public String getProvincia() {
    return provincia;
  }

  public String getPais() {
    return pais;
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
    return "paciente.direccion-actualizada";
  }

  @Override
  public Object getPayload() {
    return new Payload(
      addressId.toString(),
      nombre,
      linea1,
      linea2,
      ciudad,
      provincia,
      pais,
      new Geo(lat, lng)
    );
  }

  private record Payload(String direccionId, String nombre, String linea1, String linea2, String ciudad, String provincia, String pais, Geo geo) {}
  private record Geo(Double lat, Double lng) {}
}
