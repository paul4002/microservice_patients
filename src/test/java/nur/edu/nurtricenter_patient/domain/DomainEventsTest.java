package nur.edu.nurtricenter_patient.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import nur.edu.nurtricenter_patient.domain.patient.SubscriptionStatus;
import nur.edu.nurtricenter_patient.domain.patient.events.AddressAddedEvent;
import nur.edu.nurtricenter_patient.domain.patient.events.AddressDeactivatedEvent;
import nur.edu.nurtricenter_patient.domain.patient.events.AddressGeocodedEvent;
import nur.edu.nurtricenter_patient.domain.patient.events.AddressUpdatedEvent;
import nur.edu.nurtricenter_patient.domain.patient.events.PatientCreatedEvent;
import nur.edu.nurtricenter_patient.domain.patient.events.PatientDeletedEvent;
import nur.edu.nurtricenter_patient.domain.patient.events.PatientSubscriptionRemovedEvent;
import nur.edu.nurtricenter_patient.domain.patient.events.PatientSubscriptionUpdatedEvent;
import nur.edu.nurtricenter_patient.domain.patient.events.PatientUpdatedEvent;

class DomainEventsTest {

  @Test
  void patientCreatedEvent() {
    UUID id = UUID.randomUUID();
    PatientCreatedEvent e = new PatientCreatedEvent(id, "Ana Perez", "DOC-001", null);
    assertEquals("Patient", e.getAggregateType());
    assertEquals(id.toString(), e.getAggregateId());
    assertEquals("paciente.paciente-creado", e.getEventName());
    assertEquals("PatientCreatedEvent", e.getEventType());
    assertNotNull(e.getId());
    assertNotNull(e.getOccurredOn());
    assertNotNull(e.getPayload());
  }

  @Test
  void patientDeletedEvent() {
    UUID id = UUID.randomUUID();
    PatientDeletedEvent e = new PatientDeletedEvent(id);
    assertEquals("Patient", e.getAggregateType());
    assertEquals("paciente.paciente-eliminado", e.getEventName());
    assertNotNull(e.getPayload());
  }

  @Test
  void patientUpdatedEvent() {
    UUID id = UUID.randomUUID();
    PatientUpdatedEvent e = new PatientUpdatedEvent(id, "Ana Perez", "DOC-001", null);
    assertEquals("paciente.paciente-actualizado", e.getEventName());
    assertNotNull(e.getPayload());
  }

  @Test
  void patientSubscriptionUpdatedEvent() {
    UUID id = UUID.randomUUID();
    PatientSubscriptionUpdatedEvent e = new PatientSubscriptionUpdatedEvent(
        id, UUID.randomUUID(), SubscriptionStatus.ACTIVE, LocalDate.now(), "contrato.creado");
    assertEquals("paciente.suscripcion-actualizada", e.getEventName());
    assertNotNull(e.getPayload());
  }

  @Test
  void patientSubscriptionRemovedEvent() {
    UUID id = UUID.randomUUID();
    PatientSubscriptionRemovedEvent e = new PatientSubscriptionRemovedEvent(
        id, null, "cancelado", "contrato.eliminado");
    assertEquals("paciente.suscripcion-eliminada", e.getEventName());
    assertNotNull(e.getPayload());
  }

  @Test
  void addressAddedEvent() {
    UUID pId = UUID.randomUUID();
    UUID aId = UUID.randomUUID();
    AddressAddedEvent e = new AddressAddedEvent(pId, aId, "Casa", "Calle 1", null,
        "La Paz", "La Paz", "Bolivia", -16.5, -68.15);
    assertEquals("paciente.direccion-creada", e.getEventName());
    assertEquals(aId, e.getAddressId());
    assertNotNull(e.getPayload());
  }

  @Test
  void addressUpdatedEvent() {
    UUID pId = UUID.randomUUID();
    UUID aId = UUID.randomUUID();
    AddressUpdatedEvent e = new AddressUpdatedEvent(pId, aId, "Oficina", "Av. 1", null,
        "La Paz", "La Paz", "Bolivia", -16.4, -68.1);
    assertEquals("paciente.direccion-actualizada", e.getEventName());
    assertNotNull(e.getPayload());
  }

  @Test
  void addressDeactivatedEvent() {
    UUID pId = UUID.randomUUID();
    UUID aId = UUID.randomUUID();
    AddressDeactivatedEvent e = new AddressDeactivatedEvent(pId, aId);
    assertEquals("paciente.direccion-desactivada", e.getEventName());
    assertNotNull(e.getPayload());
  }

  @Test
  void addressGeocodedEvent() {
    UUID pId = UUID.randomUUID();
    UUID aId = UUID.randomUUID();
    AddressGeocodedEvent e = new AddressGeocodedEvent(pId, aId, -16.5, -68.15);
    assertEquals("paciente.direccion-geocodificada", e.getEventName());
    assertNotNull(e.getPayload());
  }
}
