package nur.edu.nurtricenter_patient.domain.patient;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import nur.edu.nurtricenter_patient.core.abstractions.AggregateRoot;
import nur.edu.nurtricenter_patient.core.results.DomainException;
import nur.edu.nurtricenter_patient.domain.address.Address;
import nur.edu.nurtricenter_patient.domain.address.Coordinate;
import nur.edu.nurtricenter_patient.domain.patient.events.AddressAddedEvent;
import nur.edu.nurtricenter_patient.domain.patient.events.AddressDeactivatedEvent;
import nur.edu.nurtricenter_patient.domain.patient.events.AddressGeocodedEvent;
import nur.edu.nurtricenter_patient.domain.patient.events.AddressUpdatedEvent;
import nur.edu.nurtricenter_patient.domain.patient.events.PatientCreatedEvent;
import nur.edu.nurtricenter_patient.domain.patient.events.PatientDeletedEvent;
import nur.edu.nurtricenter_patient.domain.patient.events.PatientSubscriptionRemovedEvent;
import nur.edu.nurtricenter_patient.domain.patient.events.PatientSubscriptionUpdatedEvent;
import nur.edu.nurtricenter_patient.domain.patient.events.PatientUpdatedEvent;

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
  private final Clock clock;

  // ──────────────────────────────────────────────
  // Factory method — crea un paciente NUEVO y emite el evento de dominio
  // ──────────────────────────────────────────────
  public static Patient create(String name, String lastname, LocalDate birthDate,
      Email email, Cellphone cellphone, String document, UUID subscriptionId) {
    return create(name, lastname, birthDate, email, cellphone, document, subscriptionId, Clock.systemUTC());
  }

  public static Patient create(String name, String lastname, LocalDate birthDate,
      Email email, Cellphone cellphone, String document, UUID subscriptionId, Clock clock) {
    Patient patient = new Patient(UUID.randomUUID(), name, lastname, birthDate,
        email, cellphone, document, subscriptionId, clock);
    patient.addDomainEvent(new PatientCreatedEvent(
        patient.getId(),
        patient.getName() + " " + patient.getLastname(),
        patient.getDocument(),
        patient.getSubscriptionId()
    ));
    return patient;
  }

  // ──────────────────────────────────────────────
  // Constructor de reconstitución desde BD — NO emite eventos
  // ──────────────────────────────────────────────
  public Patient(UUID id, String name, String lastname, LocalDate birthDate,
      Email email, Cellphone cellphone, String document, UUID subscriptionId,
      SubscriptionStatus subscriptionStatus, LocalDate subscriptionEndsOn) {
    this(id, name, lastname, birthDate, email, cellphone, document, subscriptionId, Clock.systemUTC());
    this.subscriptionStatus = subscriptionStatus != null ? subscriptionStatus : defaultStatus(subscriptionId);
    this.subscriptionEndsOn = subscriptionEndsOn;
  }

  private Patient(UUID id, String name, String lastname, LocalDate birthDate,
      Email email, Cellphone cellphone, String document, UUID subscriptionId, Clock clock) {
    super(id);
    this.clock = clock;
    validate(name, lastname, birthDate, document, clock);
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

  // ──────────────────────────────────────────────
  // Comportamiento de dominio
  // ──────────────────────────────────────────────

  public void update(String name, String lastname, LocalDate birthDate,
      Email email, Cellphone cellphone, String document, UUID subscriptionId) {
    validate(name, lastname, birthDate, document, clock);
    this.name = name;
    this.lastname = lastname;
    this.birthDate = birthDate;
    this.email = email;
    this.cellphone = cellphone;
    this.document = document;
    this.subscriptionId = subscriptionId;
    // La suscripción (status y endsOn) NO se resetea: se gestiona exclusivamente
    // vía syncSubscription / removeSubscription desde los eventos de integración.
    addDomainEvent(new PatientUpdatedEvent(
        getId(),
        getName() + " " + getLastname(),
        getDocument(),
        getSubscriptionId()
    ));
  }

  public void markAsDeleted() {
    addDomainEvent(new PatientDeletedEvent(getId()));
  }

  public UUID addAddress(String label, String line1, String line2,
      String country, String province, String city, Coordinate coordinate) {
    Address address = new Address(label, line1, line2, country, province, city, coordinate);
    this.addresses.add(address);
    addDomainEvent(new AddressAddedEvent(
        getId(), address.getId(), label, line1, line2, city, province, country,
        coordinate.latitude(), coordinate.longitude()
    ));
    return address.getId();
  }

  /** Solo para reconstitución desde repositorio — no emite evento */
  public void restoreAddress(Address address) {
    if (address != null) {
      this.addresses.add(address);
    }
  }

  public void updateAddress(UUID addressId, String label, String line1, String line2,
      String country, String province, String city, Coordinate coordinate) {
    Address address = getAddressById(addressId);
    if (address == null) {
      throw new DomainException(PatientErrors.AddressNotFound(addressId.toString()));
    }
    address.update(label, line1, line2, country, province, city, coordinate);
    addDomainEvent(new AddressUpdatedEvent(
        getId(), addressId, label, line1, line2, city, province, country,
        coordinate.latitude(), coordinate.longitude()
    ));
  }

  public void deactivateAddress(UUID addressId) {
    Address address = getAddressById(addressId);
    if (address == null) {
      throw new DomainException(PatientErrors.AddressNotFound(addressId.toString()));
    }
    address.deactivate();
    addDomainEvent(new AddressDeactivatedEvent(getId(), addressId));
  }

  public void updateAddressGeo(UUID addressId, Coordinate coordinate) {
    Address address = getAddressById(addressId);
    if (address == null) {
      throw new DomainException(PatientErrors.AddressNotFound(addressId.toString()));
    }
    address.updateGeo(coordinate);
    addDomainEvent(new AddressGeocodedEvent(
        getId(), addressId, coordinate.latitude(), coordinate.longitude()
    ));
  }

  /**
   * Sincroniza el estado de suscripción desde un evento externo.
   * Emite PatientSubscriptionUpdatedEvent solo si hubo cambios reales.
   *
   * @param sourceEvent routing key del evento de integración (para trazabilidad)
   */
  public boolean syncSubscription(UUID subscriptionId, SubscriptionStatus status,
      LocalDate endsOn, String sourceEvent) {
    SubscriptionStatus nextStatus = status != null ? status : defaultStatus(subscriptionId);
    boolean changed = !Objects.equals(this.subscriptionId, subscriptionId)
        || this.subscriptionStatus != nextStatus
        || !Objects.equals(this.subscriptionEndsOn, endsOn);
    this.subscriptionId = subscriptionId;
    this.subscriptionStatus = nextStatus;
    this.subscriptionEndsOn = endsOn;
    if (changed) {
      addDomainEvent(new PatientSubscriptionUpdatedEvent(
          getId(), subscriptionId, nextStatus, endsOn, sourceEvent
      ));
    }
    return changed;
  }

  /**
   * Elimina la suscripción activa del paciente.
   * Emite PatientSubscriptionRemovedEvent solo si hubo cambios reales.
   *
   * @param sourceEvent routing key del evento de integración (para trazabilidad)
   */
  public boolean removeSubscription(SubscriptionStatus finalStatus, LocalDate endsOn,
      String reason, String sourceEvent) {
    SubscriptionStatus nextStatus = finalStatus != null ? finalStatus : SubscriptionStatus.CANCELLED;
    boolean changed = this.subscriptionId != null
        || this.subscriptionStatus != nextStatus
        || !Objects.equals(this.subscriptionEndsOn, endsOn);
    UUID previous = this.subscriptionId;
    this.subscriptionId = null;
    this.subscriptionStatus = nextStatus;
    this.subscriptionEndsOn = endsOn;
    if (changed) {
      addDomainEvent(new PatientSubscriptionRemovedEvent(
          getId(), previous, reason, sourceEvent
      ));
    }
    return changed;
  }

  // ──────────────────────────────────────────────
  // Getters
  // ──────────────────────────────────────────────

  public String getName() { return name; }
  public String getLastname() { return lastname; }
  public LocalDate getBirthDate() { return birthDate; }
  public Email getEmail() { return email; }
  public Cellphone getCellphone() { return cellphone; }
  public String getDocument() { return document; }
  public UUID getSubscriptionId() { return subscriptionId; }
  public SubscriptionStatus getSubscriptionStatus() { return subscriptionStatus; }
  public LocalDate getSubscriptionEndsOn() { return subscriptionEndsOn; }
  public List<Address> getAddresses() { return Collections.unmodifiableList(addresses); }

  // ──────────────────────────────────────────────
  // Privado
  // ──────────────────────────────────────────────

  private Address getAddressById(UUID addressId) {
    for (Address address : this.addresses) {
      if (address.getId().equals(addressId)) return address;
    }
    return null;
  }

  private static SubscriptionStatus defaultStatus(UUID subscriptionId) {
    return subscriptionId != null ? SubscriptionStatus.ACTIVE : SubscriptionStatus.INACTIVE;
  }

  private static void validate(String name, String lastname, LocalDate birthDate,
      String document, Clock clock) {
    if (name == null || name.isBlank())
      throw new DomainException(PatientErrors.NameIsRequired());
    if (lastname == null || lastname.isBlank())
      throw new DomainException(PatientErrors.LastnameIsRequired());
    if (birthDate == null)
      throw new DomainException(PatientErrors.BirthDateIsRequired());
    if (birthDate.isAfter(LocalDate.now(clock)))
      throw new DomainException(PatientErrors.BirthDateInFuture());
    if (document == null || document.isBlank())
      throw new DomainException(PatientErrors.DocumentIsRequired());
  }
}
