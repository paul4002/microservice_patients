package nur.edu.nurtricenter_patient.domain.patient;

import java.time.LocalDate;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import nur.edu.nurtricenter_patient.core.abstractions.AggregateRoot;
import nur.edu.nurtricenter_patient.core.results.DomainException;
import nur.edu.nurtricenter_patient.domain.address.Address;
import nur.edu.nurtricenter_patient.domain.address.Coordinate;

public class Patient extends AggregateRoot {
  private String name;
  private String lastname;
  private LocalDate birthDate;
  private Email email;
  private Cellphone cellphone;
  private final List<Address> addresses = new ArrayList<>();
  
  public Patient(String name, String lastname, LocalDate birthDate, Email email, Cellphone cellphone) {
    this(UUID.randomUUID(), name, lastname, birthDate, email, cellphone, Clock.systemUTC());
  }

  public Patient(UUID id, String name, String lastname, LocalDate birthDate, Email email, Cellphone cellphone) {
    this(id, name, lastname, birthDate, email, cellphone, Clock.systemUTC());
  }

  private Patient(UUID id, String name, String lastname, LocalDate birthDate, Email email, Cellphone cellphone, Clock clock) {
    super(id);
    if (name == null || name.isBlank()) {
      throw new DomainException(PatientErrors.NameIsRequired());
    }
    if (lastname == null || lastname.isBlank()) {
      throw new DomainException(PatientErrors.LastnameIsRequired());
    }
    if (birthDate == null) {
      throw new DomainException(PatientErrors.BirthDateIsRequired());
    }
    if (birthDate.isAfter(LocalDate.now(clock))) {
      throw new DomainException(PatientErrors.BirthDateInFuture());
    }
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

  public List<Address> getAddresses() {
    return Collections.unmodifiableList(addresses);
  }
}
