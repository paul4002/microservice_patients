package nur.edu.nurtricenter_patient.domain.patient;

import java.time.LocalDate;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
  private String document;
  private UUID subscriptionId;
  private SubscriptionStatus subscriptionStatus;
  private LocalDate subscriptionEndsOn;
  private final List<Address> addresses = new ArrayList<>();
  
  public Patient(String name, String lastname, LocalDate birthDate, Email email, Cellphone cellphone, String document, UUID subscriptionId) {
    this(UUID.randomUUID(), name, lastname, birthDate, email, cellphone, document, subscriptionId, Clock.systemUTC());
  }

  public Patient(UUID id, String name, String lastname, LocalDate birthDate, Email email, Cellphone cellphone, String document, UUID subscriptionId) {
    this(id, name, lastname, birthDate, email, cellphone, document, subscriptionId, Clock.systemUTC());
  }

  public Patient(UUID id, String name, String lastname, LocalDate birthDate, Email email, Cellphone cellphone, String document, UUID subscriptionId, SubscriptionStatus subscriptionStatus, LocalDate subscriptionEndsOn) {
    this(id, name, lastname, birthDate, email, cellphone, document, subscriptionId, Clock.systemUTC());
    this.subscriptionStatus = subscriptionStatus != null ? subscriptionStatus : defaultStatus(subscriptionId);
    this.subscriptionEndsOn = subscriptionEndsOn;
  }

  private Patient(UUID id, String name, String lastname, LocalDate birthDate, Email email, Cellphone cellphone, String document, UUID subscriptionId, Clock clock) {
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
    if (document == null || document.isBlank()) {
      throw new DomainException(PatientErrors.DocumentIsRequired());
    }
    this.name = name;
    this.lastname = lastname;
    this.birthDate = birthDate;
    this.email = email;
    this.cellphone = cellphone;
    this.document = document;
    this.subscriptionId = subscriptionId;
    this.subscriptionStatus = defaultStatus(subscriptionId);
    this.subscriptionEndsOn = null;
  }

  public void addAddress(String label, String line1, String line2, String country, String province, String city, Coordinate coordinate) {
    Address address = new Address(label, line1, line2, country, province, city, coordinate);
    this.addresses.add(address);
  }

  public void restoreAddress(Address address) {
    if (address != null) {
      this.addresses.add(address);
    }
  }

  public UUID addAddressWithId(String label, String line1, String line2, String country, String province, String city, Coordinate coordinate) {
    Address address = new Address(label, line1, line2, country, province, city, coordinate);
    this.addresses.add(address);
    return address.getId();
  }

  public void updateAddress(UUID addressId, String label, String line1, String line2, String country, String province, String city, Coordinate coordinate) {
    Address address = getAddressById(addressId);
    if (address == null) {
      throw new DomainException(PatientErrors.AddressNotFound(addressId.toString()));
    }
    address.update(label, line1, line2, country, province, city, coordinate);
  }

  public void deactivateAddress(UUID addressId) {
    Address address = getAddressById(addressId);
    if (address == null) {
      throw new DomainException(PatientErrors.AddressNotFound(addressId.toString()));
    }
    address.deactivate();
  }

  public void updateAddressGeo(UUID addressId, Coordinate coordinate) {
    Address address = getAddressById(addressId);
    if (address == null) {
      throw new DomainException(PatientErrors.AddressNotFound(addressId.toString()));
    }
    address.updateGeo(coordinate);
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
  public String getDocument() {
    return document;
  }
  public UUID getSubscriptionId() {
    return subscriptionId;
  }

  public SubscriptionStatus getSubscriptionStatus() {
    return subscriptionStatus;
  }

  public LocalDate getSubscriptionEndsOn() {
    return subscriptionEndsOn;
  }

  public List<Address> getAddresses() {
    return Collections.unmodifiableList(addresses);
  }

  public void update(String name, String lastname, LocalDate birthDate, Email email, Cellphone cellphone, String document, UUID subscriptionId) {
    if (name == null || name.isBlank()) {
      throw new DomainException(PatientErrors.NameIsRequired());
    }
    if (lastname == null || lastname.isBlank()) {
      throw new DomainException(PatientErrors.LastnameIsRequired());
    }
    if (birthDate == null) {
      throw new DomainException(PatientErrors.BirthDateIsRequired());
    }
    if (birthDate.isAfter(LocalDate.now(Clock.systemUTC()))) {
      throw new DomainException(PatientErrors.BirthDateInFuture());
    }
    if (document == null || document.isBlank()) {
      throw new DomainException(PatientErrors.DocumentIsRequired());
    }
    this.name = name;
    this.lastname = lastname;
    this.birthDate = birthDate;
    this.email = email;
    this.cellphone = cellphone;
    this.document = document;
    this.subscriptionId = subscriptionId;
    this.subscriptionStatus = defaultStatus(subscriptionId);
    this.subscriptionEndsOn = null;
  }

  public boolean syncSubscription(UUID subscriptionId, SubscriptionStatus status, LocalDate endsOn) {
    SubscriptionStatus nextStatus = status != null ? status : defaultStatus(subscriptionId);
    boolean changed = !Objects.equals(this.subscriptionId, subscriptionId)
      || this.subscriptionStatus != nextStatus
      || !Objects.equals(this.subscriptionEndsOn, endsOn);
    this.subscriptionId = subscriptionId;
    this.subscriptionStatus = nextStatus;
    this.subscriptionEndsOn = endsOn;
    return changed;
  }

  public boolean removeSubscription(SubscriptionStatus finalStatus, LocalDate endsOn) {
    SubscriptionStatus nextStatus = finalStatus != null ? finalStatus : SubscriptionStatus.CANCELLED;
    boolean changed = this.subscriptionId != null
      || this.subscriptionStatus != nextStatus
      || !Objects.equals(this.subscriptionEndsOn, endsOn);
    this.subscriptionId = null;
    this.subscriptionStatus = nextStatus;
    this.subscriptionEndsOn = endsOn;
    return changed;
  }

  private Address getAddressById(UUID addressId) {
    for (Address address : this.addresses) {
      if (address.getId().equals(addressId)) {
        return address;
      }
    }
    return null;
  }

  private SubscriptionStatus defaultStatus(UUID subscriptionId) {
    return subscriptionId != null ? SubscriptionStatus.ACTIVE : SubscriptionStatus.INACTIVE;
  }
}
