package nur.edu.nurtricenter_patient.domain.patient;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import nur.edu.nurtricenter_patient.core.abstractions.AggregateRoot;
import nur.edu.nurtricenter_patient.domain.address.Address;
import nur.edu.nurtricenter_patient.domain.address.Coordinate;

public class Patient extends AggregateRoot {
  private String name;
  private String lastname;
  private LocalDate birthDate;
  private Email email;
  private Cellphone cellphone;
  List<Address> addresses;
  
  public Patient(String name, String lastname, LocalDate birthDate, Email email, Cellphone cellphone) {
    super(UUID.randomUUID());
    this.name = name;
    this.lastname = lastname;
    this.birthDate = birthDate;
    this.email = email;
    this.cellphone = cellphone;
  }

  public void addAddress(String label, String line1, String line2, String country, String province, String city, Coordinate coordinate) {
    Address address = new Address(label, line1, line2, country, province, city, coordinate);
    this.addresses.add(address);
  }

  public String getName() {
    return name;
  }
  public String getLastname() {
    return lastname;
  }
  public LocalDate getBirthDate() {
    return birthDate;
  }
  public Email getEmail() {
    return email;
  }
  public Cellphone getCellphone() {
    return cellphone;
  }
}
