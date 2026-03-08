package nur.edu.nurtricenter_patient.domain.patient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import nur.edu.nurtricenter_patient.core.results.DomainException;
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

class PatientTest {

  private static final LocalDate VALID_BIRTH_DATE = LocalDate.of(1990, 5, 20);
  private static final Email VALID_EMAIL = new Email("ana@example.com");
  private static final Cellphone VALID_PHONE = new Cellphone("75123456");
  private static final Coordinate VALID_COORD = new Coordinate(-17.7833, -63.1821);

  // ─── create ───────────────────────────────────────────────────────────────

  @Test
  void create_shouldEmitPatientCreatedEvent() {
    Patient patient = Patient.create("Ana", "Perez", VALID_BIRTH_DATE, VALID_EMAIL, VALID_PHONE, "DOC-001", null);
    assertEquals(1, patient.getDomainEvents().size());
    assertTrue(patient.getDomainEvents().get(0) instanceof PatientCreatedEvent);
    PatientCreatedEvent event = (PatientCreatedEvent) patient.getDomainEvents().get(0);
    assertEquals("Ana Perez", event.getNombre());
    assertEquals("DOC-001", event.getDocumento());
    assertNull(event.getSuscripcionId());
  }

  @Test
  void create_shouldSetSubscriptionStatusActiveWhenSubscriptionProvided() {
    UUID subId = UUID.randomUUID();
    Patient patient = Patient.create("Ana", "Perez", VALID_BIRTH_DATE, VALID_EMAIL, VALID_PHONE, "DOC-001", subId);
    assertEquals(SubscriptionStatus.ACTIVE, patient.getSubscriptionStatus());
    assertEquals(subId, patient.getSubscriptionId());
  }

  @Test
  void create_shouldSetSubscriptionStatusInactiveWhenNoSubscription() {
    Patient patient = Patient.create("Ana", "Perez", VALID_BIRTH_DATE, VALID_EMAIL, VALID_PHONE, "DOC-001", null);
    assertEquals(SubscriptionStatus.INACTIVE, patient.getSubscriptionStatus());
    assertNull(patient.getSubscriptionId());
  }

  @Test
  void create_shouldThrowWhenNameIsBlank() {
    DomainException ex = assertThrows(DomainException.class,
        () -> Patient.create("  ", "Perez", VALID_BIRTH_DATE, VALID_EMAIL, VALID_PHONE, "DOC-001", null));
    assertEquals("Patient.NameIsRequired", ex.getError().getCode());
  }

  @Test
  void create_shouldThrowWhenLastnameIsNull() {
    DomainException ex = assertThrows(DomainException.class,
        () -> Patient.create("Ana", null, VALID_BIRTH_DATE, VALID_EMAIL, VALID_PHONE, "DOC-001", null));
    assertEquals("Patient.LastnameIsRequired", ex.getError().getCode());
  }

  @Test
  void create_shouldThrowWhenBirthDateIsNull() {
    DomainException ex = assertThrows(DomainException.class,
        () -> Patient.create("Ana", "Perez", null, VALID_EMAIL, VALID_PHONE, "DOC-001", null));
    assertEquals("Patient.BirthDateIsRequired", ex.getError().getCode());
  }

  @Test
  void create_shouldThrowWhenBirthDateIsInFuture() {
    Clock fixedClock = Clock.fixed(Instant.parse("2026-01-15T00:00:00Z"), ZoneOffset.UTC);
    LocalDate futureDate = LocalDate.of(2026, 1, 16);
    DomainException ex = assertThrows(DomainException.class,
        () -> Patient.create("Ana", "Perez", futureDate, VALID_EMAIL, VALID_PHONE, "DOC-001", null, fixedClock));
    assertEquals("Patient.BirthDateInFuture", ex.getError().getCode());
  }

  @Test
  void create_shouldThrowWhenDocumentIsBlank() {
    DomainException ex = assertThrows(DomainException.class,
        () -> Patient.create("Ana", "Perez", VALID_BIRTH_DATE, VALID_EMAIL, VALID_PHONE, "", null));
    assertEquals("Patient.DocumentIsRequired", ex.getError().getCode());
  }

  // ─── update ───────────────────────────────────────────────────────────────

  @Test
  void update_shouldEmitPatientUpdatedEvent() {
    Patient patient = Patient.create("Ana", "Perez", VALID_BIRTH_DATE, VALID_EMAIL, VALID_PHONE, "DOC-001", null);
    patient.clearDomainEvents();

    patient.update("Luis", "Gomez", LocalDate.of(1985, 3, 10),
        new Email("luis@example.com"), new Cellphone("76543210"), "DOC-002", null);

    assertEquals(1, patient.getDomainEvents().size());
    assertTrue(patient.getDomainEvents().get(0) instanceof PatientUpdatedEvent);
    PatientUpdatedEvent event = (PatientUpdatedEvent) patient.getDomainEvents().get(0);
    assertEquals("Luis Gomez", event.getNombre());
    assertEquals("DOC-002", event.getDocumento());
  }

  @Test
  void update_shouldThrowWhenNameIsNull() {
    Patient patient = Patient.create("Ana", "Perez", VALID_BIRTH_DATE, VALID_EMAIL, VALID_PHONE, "DOC-001", null);
    DomainException ex = assertThrows(DomainException.class,
        () -> patient.update(null, "Perez", VALID_BIRTH_DATE, VALID_EMAIL, VALID_PHONE, "DOC-001", null));
    assertEquals("Patient.NameIsRequired", ex.getError().getCode());
  }

  // ─── markAsDeleted ────────────────────────────────────────────────────────

  @Test
  void markAsDeleted_shouldEmitPatientDeletedEvent() {
    Patient patient = Patient.create("Ana", "Perez", VALID_BIRTH_DATE, VALID_EMAIL, VALID_PHONE, "DOC-001", null);
    patient.clearDomainEvents();

    patient.markAsDeleted();

    assertEquals(1, patient.getDomainEvents().size());
    assertTrue(patient.getDomainEvents().get(0) instanceof PatientDeletedEvent);
    PatientDeletedEvent event = (PatientDeletedEvent) patient.getDomainEvents().get(0);
    assertEquals(patient.getId().toString(), event.getAggregateId());
  }

  // ─── addAddress ───────────────────────────────────────────────────────────

  @Test
  void addAddress_shouldEmitAddressAddedEventAndReturnId() {
    Patient patient = Patient.create("Ana", "Perez", VALID_BIRTH_DATE, VALID_EMAIL, VALID_PHONE, "DOC-001", null);
    patient.clearDomainEvents();

    UUID addressId = patient.addAddress("Casa", "Calle 1", "Depto 2", "Bolivia", "Santa Cruz", "Santa Cruz", VALID_COORD);

    assertNotNull(addressId);
    assertEquals(1, patient.getDomainEvents().size());
    assertTrue(patient.getDomainEvents().get(0) instanceof AddressAddedEvent);
    assertEquals(1, patient.getAddresses().size());
  }

  // ─── updateAddress ────────────────────────────────────────────────────────

  @Test
  void updateAddress_shouldEmitAddressUpdatedEvent() {
    Patient patient = Patient.create("Ana", "Perez", VALID_BIRTH_DATE, VALID_EMAIL, VALID_PHONE, "DOC-001", null);
    UUID addressId = patient.addAddress("Casa", "Calle 1", null, "Bolivia", "Santa Cruz", "Santa Cruz", VALID_COORD);
    patient.clearDomainEvents();

    patient.updateAddress(addressId, "Trabajo", "Avenida 2", null, "Bolivia", "La Paz", "La Paz",
        new Coordinate(-16.5, -68.1));

    assertEquals(1, patient.getDomainEvents().size());
    assertTrue(patient.getDomainEvents().get(0) instanceof AddressUpdatedEvent);
  }

  @Test
  void updateAddress_shouldThrowWhenAddressNotFound() {
    Patient patient = Patient.create("Ana", "Perez", VALID_BIRTH_DATE, VALID_EMAIL, VALID_PHONE, "DOC-001", null);
    UUID nonExistentId = UUID.randomUUID();
    DomainException ex = assertThrows(DomainException.class,
        () -> patient.updateAddress(nonExistentId, "X", "Y", null, "Bolivia", "Santa Cruz", "Santa Cruz", VALID_COORD));
    assertEquals("Patient.AddressNotFound", ex.getError().getCode());
  }

  // ─── deactivateAddress ────────────────────────────────────────────────────

  @Test
  void deactivateAddress_shouldEmitAddressDeactivatedEvent() {
    Patient patient = Patient.create("Ana", "Perez", VALID_BIRTH_DATE, VALID_EMAIL, VALID_PHONE, "DOC-001", null);
    UUID addressId = patient.addAddress("Casa", "Calle 1", null, "Bolivia", "Santa Cruz", "Santa Cruz", VALID_COORD);
    patient.clearDomainEvents();

    patient.deactivateAddress(addressId);

    assertEquals(1, patient.getDomainEvents().size());
    assertTrue(patient.getDomainEvents().get(0) instanceof AddressDeactivatedEvent);
    assertFalse(patient.getAddresses().get(0).isActive());
  }

  @Test
  void deactivateAddress_shouldThrowWhenAddressNotFound() {
    Patient patient = Patient.create("Ana", "Perez", VALID_BIRTH_DATE, VALID_EMAIL, VALID_PHONE, "DOC-001", null);
    DomainException ex = assertThrows(DomainException.class,
        () -> patient.deactivateAddress(UUID.randomUUID()));
    assertEquals("Patient.AddressNotFound", ex.getError().getCode());
  }

  // ─── updateAddressGeo ─────────────────────────────────────────────────────

  @Test
  void updateAddressGeo_shouldEmitAddressGeocodedEvent() {
    Patient patient = Patient.create("Ana", "Perez", VALID_BIRTH_DATE, VALID_EMAIL, VALID_PHONE, "DOC-001", null);
    UUID addressId = patient.addAddress("Casa", "Calle 1", null, "Bolivia", "Santa Cruz", "Santa Cruz", VALID_COORD);
    patient.clearDomainEvents();

    patient.updateAddressGeo(addressId, new Coordinate(-17.80, -63.17));

    assertEquals(1, patient.getDomainEvents().size());
    assertTrue(patient.getDomainEvents().get(0) instanceof AddressGeocodedEvent);
  }

  @Test
  void updateAddressGeo_shouldThrowWhenAddressNotFound() {
    Patient patient = Patient.create("Ana", "Perez", VALID_BIRTH_DATE, VALID_EMAIL, VALID_PHONE, "DOC-001", null);
    DomainException ex = assertThrows(DomainException.class,
        () -> patient.updateAddressGeo(UUID.randomUUID(), VALID_COORD));
    assertEquals("Patient.AddressNotFound", ex.getError().getCode());
  }

  // ─── syncSubscription ─────────────────────────────────────────────────────

  @Test
  void syncSubscription_shouldEmitEventAndReturnTrueWhenChanged() {
    Patient patient = Patient.create("Ana", "Perez", VALID_BIRTH_DATE, VALID_EMAIL, VALID_PHONE, "DOC-001", null);
    patient.clearDomainEvents();

    UUID subId = UUID.randomUUID();
    boolean changed = patient.syncSubscription(subId, SubscriptionStatus.ACTIVE, LocalDate.of(2026, 12, 31), "contrato.creado");

    assertTrue(changed);
    assertEquals(subId, patient.getSubscriptionId());
    assertEquals(SubscriptionStatus.ACTIVE, patient.getSubscriptionStatus());
    assertEquals(1, patient.getDomainEvents().size());
    assertTrue(patient.getDomainEvents().get(0) instanceof PatientSubscriptionUpdatedEvent);
  }

  @Test
  void syncSubscription_shouldReturnFalseAndNotEmitEventWhenUnchanged() {
    UUID subId = UUID.randomUUID();
    Patient patient = Patient.create("Ana", "Perez", VALID_BIRTH_DATE, VALID_EMAIL, VALID_PHONE, "DOC-001", subId);
    // syncSubscription with the same subscriptionId and default ACTIVE status
    patient.syncSubscription(subId, SubscriptionStatus.ACTIVE, null, "source");
    patient.clearDomainEvents();

    boolean changed = patient.syncSubscription(subId, SubscriptionStatus.ACTIVE, null, "source");

    assertFalse(changed);
    assertEquals(0, patient.getDomainEvents().size());
  }

  // ─── removeSubscription ───────────────────────────────────────────────────

  @Test
  void removeSubscription_shouldEmitEventAndReturnTrueWhenChanged() {
    UUID subId = UUID.randomUUID();
    Patient patient = Patient.create("Ana", "Perez", VALID_BIRTH_DATE, VALID_EMAIL, VALID_PHONE, "DOC-001", subId);
    patient.clearDomainEvents();

    boolean changed = patient.removeSubscription(SubscriptionStatus.CANCELLED, null, "motivo", "contrato.cancelado");

    assertTrue(changed);
    assertNull(patient.getSubscriptionId());
    assertEquals(SubscriptionStatus.CANCELLED, patient.getSubscriptionStatus());
    assertEquals(1, patient.getDomainEvents().size());
    assertTrue(patient.getDomainEvents().get(0) instanceof PatientSubscriptionRemovedEvent);
  }

  @Test
  void removeSubscription_shouldDefaultToCancelledWhenStatusIsNull() {
    UUID subId = UUID.randomUUID();
    Patient patient = Patient.create("Ana", "Perez", VALID_BIRTH_DATE, VALID_EMAIL, VALID_PHONE, "DOC-001", subId);
    patient.clearDomainEvents();

    patient.removeSubscription(null, null, "motivo", "source");

    assertEquals(SubscriptionStatus.CANCELLED, patient.getSubscriptionStatus());
  }
}
