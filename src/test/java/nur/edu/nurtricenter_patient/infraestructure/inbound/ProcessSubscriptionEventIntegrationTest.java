package nur.edu.nurtricenter_patient.infraestructure.inbound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import nur.edu.nurtricenter_patient.application.subscriptions.processSubscriptionEvent.ProcessSubscriptionEventCommand;
import nur.edu.nurtricenter_patient.application.subscriptions.processSubscriptionEvent.ProcessSubscriptionEventHandler;
import nur.edu.nurtricenter_patient.core.results.Result;
import nur.edu.nurtricenter_patient.domain.patient.Cellphone;
import nur.edu.nurtricenter_patient.domain.patient.Email;
import nur.edu.nurtricenter_patient.domain.patient.IPatientRepository;
import nur.edu.nurtricenter_patient.domain.patient.Patient;
import nur.edu.nurtricenter_patient.infraestructure.outbox.OutboxEventEntity;
import nur.edu.nurtricenter_patient.infraestructure.outbox.OutboxEventRepository;
import nur.edu.nurtricenter_patient.infraestructure.repositories.PatientEntityRepository;

@SpringBootTest
class ProcessSubscriptionEventIntegrationTest {

  @Autowired
  private ProcessSubscriptionEventHandler handler;

  @Autowired
  private IPatientRepository patientRepository;

  @Autowired
  private PatientEntityRepository patientEntityRepository;

  @Autowired
  private InboundEventRepository inboundEventRepository;

  @Autowired
  private OutboxEventRepository outboxEventRepository;

  @BeforeEach
  void clean() {
    outboxEventRepository.deleteAll();
    inboundEventRepository.deleteAll();
    patientEntityRepository.deleteAll();
  }

  @Test
  void idempotency_shouldProcessSingleTimeForSameEventId() {
    Patient patient = createPatient(null);
    patientRepository.add(patient);

    UUID eventId = UUID.randomUUID();
    UUID correlationId = UUID.randomUUID();
    UUID contractId = UUID.randomUUID();
    ProcessSubscriptionEventCommand command = new ProcessSubscriptionEventCommand(
      "contrato.creado",
      Map.of(
        "contratoId", contractId.toString(),
        "pacienteId", patient.getId().toString(),
        "tipoServicio", "asesoramiento",
        "fechaInicio", "2026-02-23",
        "fechaFin", "2026-03-10"
      ),
      "contrato.creado",
      eventId,
      correlationId,
      1,
      "2026-02-23T00:00:00Z",
      "{\"event\":\"contrato.creado\"}"
    );

    Result first = handler.handle(command);
    Result second = handler.handle(command);

    assertTrue(first.isSuccess());
    assertTrue(second.isSuccess());
    assertEquals(1L, countInboundByEventId(eventId));
    InboundEventEntity inbound = inboundEventRepository.findByEventId(eventId).orElseThrow();
    assertEquals("PROCESSED", inbound.getStatus());
    assertEquals(1L, countOutboxByEventName("paciente.suscripcion-actualizada"));
  }

  @Test
  void contratoCreado_withUnknownPaciente_shouldBeProcessedWithoutOutbox() {
    UUID eventId = UUID.randomUUID();
    UUID missingPatientId = UUID.randomUUID();
    UUID contractId = UUID.randomUUID();

    ProcessSubscriptionEventCommand command = new ProcessSubscriptionEventCommand(
      "contrato.creado",
      Map.of(
        "contratoId", contractId.toString(),
        "pacienteId", missingPatientId.toString(),
        "tipoServicio", "asesoramiento",
        "fechaInicio", "2026-02-23",
        "fechaFin", "2026-03-10"
      ),
      "contrato.creado",
      eventId,
      UUID.randomUUID(),
      1,
      "2026-02-23T00:00:00Z",
      "{\"event\":\"contrato.creado\"}"
    );

    Result result = handler.handle(command);

    assertTrue(result.isSuccess());
    InboundEventEntity inbound = inboundEventRepository.findByEventId(eventId).orElseThrow();
    assertEquals("PROCESSED", inbound.getStatus());
    assertEquals(0L, countOutboxByEventName("paciente.suscripcion-actualizada"));
  }

  @Test
  void handle_nullEventName_returnsFailure() {
    ProcessSubscriptionEventCommand command = new ProcessSubscriptionEventCommand(
      null, Map.of(), null, UUID.randomUUID(), UUID.randomUUID(), 1, "2026-02-23T00:00:00Z", "{}"
    );
    Result result = handler.handle(command);
    assertTrue(result.isFailure());
  }

  @Test
  void handle_nullEventId_returnsFailure() {
    ProcessSubscriptionEventCommand command = new ProcessSubscriptionEventCommand(
      "contrato.creado", Map.of(), "contrato.creado", null, UUID.randomUUID(), 1, "2026-02-23T00:00:00Z", "{}"
    );
    Result result = handler.handle(command);
    assertTrue(result.isFailure());
  }

  @Test
  void handle_unknownEventName_returnsSuccess() {
    ProcessSubscriptionEventCommand command = new ProcessSubscriptionEventCommand(
      "unknown.event", Map.of(), "unknown.event",
      UUID.randomUUID(), UUID.randomUUID(), 1, "2026-02-23T00:00:00Z", "{\"event\":\"unknown\"}"
    );
    Result result = handler.handle(command);
    assertTrue(result.isSuccess());
  }

  @Test
  void contratoCancelado_withPatient_removesSubscription() {
    UUID subscriptionId = UUID.randomUUID();
    Patient patient = createPatient(subscriptionId);
    patientRepository.add(patient);

    UUID eventId = UUID.randomUUID();
    ProcessSubscriptionEventCommand command = new ProcessSubscriptionEventCommand(
      "contrato.cancelado",
      Map.of("contratoId", subscriptionId.toString(), "motivoCancelacion", "test"),
      "contrato.cancelado", eventId, UUID.randomUUID(), 1, "2026-02-23T00:00:00Z", "{\"event\":\"contrato.cancelado\"}"
    );

    Result result = handler.handle(command);
    assertTrue(result.isSuccess());
  }

  @Test
  void suscripcionEliminada_withPatient_removesSubscription() {
    UUID subscriptionId = UUID.randomUUID();
    Patient patient = createPatient(subscriptionId);
    patientRepository.add(patient);

    UUID eventId = UUID.randomUUID();
    ProcessSubscriptionEventCommand command = new ProcessSubscriptionEventCommand(
      "suscripciones.suscripcion-eliminada",
      Map.of("suscripcionId", subscriptionId.toString()),
      "suscripciones.suscripcion-eliminada", eventId, UUID.randomUUID(), 1, "2026-02-23T00:00:00Z", "{}"
    );

    Result result = handler.handle(command);
    assertTrue(result.isSuccess());
  }

  @Test
  void suscripcionActualizada_withPatient_updatesSubscription() {
    UUID subscriptionId = UUID.randomUUID();
    Patient patient = createPatient(subscriptionId);
    patientRepository.add(patient);

    UUID eventId = UUID.randomUUID();
    ProcessSubscriptionEventCommand command = new ProcessSubscriptionEventCommand(
      "suscripciones.suscripcion-actualizada",
      Map.of("suscripcionId", subscriptionId.toString(), "fechaFin", "2027-01-01", "estado", "ACTIVE"),
      "suscripciones.suscripcion-actualizada", eventId, UUID.randomUUID(), 1, "2026-02-23T00:00:00Z", "{}"
    );

    Result result = handler.handle(command);
    assertTrue(result.isSuccess());
  }

  @Test
  void applyContractCreated_missingContractId_returnsFailure() {
    UUID eventId = UUID.randomUUID();
    ProcessSubscriptionEventCommand command = new ProcessSubscriptionEventCommand(
      "contrato.creado",
      Map.of("pacienteId", UUID.randomUUID().toString()),
      "contrato.creado", eventId, UUID.randomUUID(), 1, "2026-02-23T00:00:00Z", "{}"
    );

    Result result = handler.handle(command);
    assertTrue(result.isFailure());
  }

  @Test
  void applyContractCreated_missingPatientId_returnsFailure() {
    UUID eventId = UUID.randomUUID();
    ProcessSubscriptionEventCommand command = new ProcessSubscriptionEventCommand(
      "contrato.creado",
      Map.of("contratoId", UUID.randomUUID().toString()),
      "contrato.creado", eventId, UUID.randomUUID(), 1, "2026-02-23T00:00:00Z", "{}"
    );

    Result result = handler.handle(command);
    assertTrue(result.isFailure());
  }

  @Test
  void applyContractCreated_expiredEndDate_setsExpiredStatus() {
    Patient patient = createPatient(null);
    patientRepository.add(patient);

    UUID eventId = UUID.randomUUID();
    ProcessSubscriptionEventCommand command = new ProcessSubscriptionEventCommand(
      "contrato.creado",
      Map.of(
        "contratoId", UUID.randomUUID().toString(),
        "pacienteId", patient.getId().toString(),
        "fechaFin", "2020-01-01"
      ),
      "contrato.creado", eventId, UUID.randomUUID(), 1, "2026-02-23T00:00:00Z", "{}"
    );

    Result result = handler.handle(command);
    assertTrue(result.isSuccess());
  }

  @Test
  void applySubscriptionUpdate_missingSubscriptionId_returnsFailure() {
    UUID eventId = UUID.randomUUID();
    ProcessSubscriptionEventCommand command = new ProcessSubscriptionEventCommand(
      "suscripciones.suscripcion-actualizada",
      Map.of("someOtherKey", "value"),
      "suscripciones.suscripcion-actualizada", eventId, UUID.randomUUID(), 1, "2026-02-23T00:00:00Z", "{}"
    );

    Result result = handler.handle(command);
    assertTrue(result.isFailure());
  }

  @Test
  void contratoCancelar_eventName_removesSubscription() {
    UUID subscriptionId = UUID.randomUUID();
    Patient patient = createPatient(subscriptionId);
    patientRepository.add(patient);

    UUID eventId = UUID.randomUUID();
    ProcessSubscriptionEventCommand command = new ProcessSubscriptionEventCommand(
      "contrato.cancelar",
      Map.of("contratoId", subscriptionId.toString()),
      "contrato.cancelar", eventId, UUID.randomUUID(), 1, "2026-02-23T00:00:00Z", "{}"
    );

    Result result = handler.handle(command);
    assertTrue(result.isSuccess());
  }

  @Test
  void handle_nullOccurredOn_usesCurrentTime() {
    ProcessSubscriptionEventCommand command = new ProcessSubscriptionEventCommand(
      "unknown.event", Map.of(), "unknown.event",
      UUID.randomUUID(), UUID.randomUUID(), 1, null, "{}"
    );
    Result result = handler.handle(command);
    assertTrue(result.isSuccess());
  }

  private long countInboundByEventId(UUID eventId) {
    long count = 0;
    for (InboundEventEntity row : inboundEventRepository.findAll()) {
      if (eventId.equals(row.getEventId())) {
        count++;
      }
    }
    return count;
  }

  private long countOutboxByEventName(String eventName) {
    long count = 0;
    for (OutboxEventEntity row : outboxEventRepository.findAll()) {
      if (eventName.equals(row.getEventName())) {
        count++;
      }
    }
    return count;
  }

  private Patient createPatient(UUID subscriptionId) {
    return Patient.create(
      "Ana",
      "Perez",
      LocalDate.of(1995, 1, 10),
      new Email("ana.perez+" + UUID.randomUUID() + "@example.com"),
      new Cellphone(randomCellphone()),
      "DOC-" + UUID.randomUUID(),
      subscriptionId
    );
  }

  private String randomCellphone() {
    int value = 10000000 + (int) (Math.random() * 89999999);
    return Integer.toString(value);
  }
}
