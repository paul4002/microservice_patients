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
    return new Patient(
      UUID.randomUUID(),
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
