package nur.edu.nurtricenter_patient.infraestructure.domainModel;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import nur.edu.nurtricenter_patient.domain.address.Address;
import nur.edu.nurtricenter_patient.domain.address.Coordinate;

@Entity
@Table(name = "addresses")
public class AddressEntity {
  @Id
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "patient_id", nullable = false)
  private PatientEntity patient;

  private String label;
  private String line1;
  private String line2;
  private String country;
  private String province;
  private String city;

  @Column(name = "latitude")
  private Double latitude;

  @Column(name = "longitude")
  private Double longitude;

  private Boolean state;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public PatientEntity getPatient() {
    return patient;
  }

  public void setPatient(PatientEntity patient) {
    this.patient = patient;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getLine1() {
    return line1;
  }

  public void setLine1(String line1) {
    this.line1 = line1;
  }

  public String getLine2() {
    return line2;
  }

  public void setLine2(String line2) {
    this.line2 = line2;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getProvince() {
    return province;
  }

  public void setProvince(String province) {
    this.province = province;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public Double getLatitude() {
    return latitude;
  }

  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }

  public Boolean getState() {
    return state;
  }

  public void setState(Boolean state) {
    this.state = state;
  }

  public static AddressEntity fromDomain(Address address, PatientEntity patient) {
    AddressEntity entity = new AddressEntity();
    entity.id = address.getId();
    entity.patient = patient;
    entity.label = address.getLabel();
    entity.line1 = address.getLine1();
    entity.line2 = address.getLine2();
    entity.country = address.getCountry();
    entity.province = address.getProvince();
    entity.city = address.getCity();
    entity.latitude = address.getCoordinate().latitude();
    entity.longitude = address.getCoordinate().longitude();
    entity.state = address.isActive();
    return entity;
  }

  public static Address toDomain(AddressEntity entity) {
    if (entity == null) {
      return null;
    }
    Coordinate coordinate = new Coordinate(entity.latitude, entity.longitude);
    return new Address(entity.id, entity.label, entity.line1, entity.line2, entity.country, entity.province, entity.city, coordinate, entity.state);
  }
}
