package nur.edu.nurtricenter_patient.infraestructure.outbox;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class OutboxPublisherTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void publishSingle_shouldPublishEnvelopeForPatientCreated() throws IOException {
    OutboxEventRepository repository = mock(OutboxEventRepository.class);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    OutboxPublisherProperties props = new OutboxPublisherProperties();
    props.setExchange("pacientes.events");

    OutboxPublisher publisher = new OutboxPublisher(repository, rabbitTemplate, props, objectMapper);

    UUID patientId = UUID.randomUUID();
    UUID eventId = UUID.randomUUID();
    UUID correlationId = UUID.randomUUID();
    OutboxEventEntity event = buildOutboxEvent(
      "PatientCreatedEvent",
      "paciente.paciente-creado",
      patientId,
      eventId,
      correlationId,
      "{\"pacienteId\":\"" + patientId + "\",\"nombre\":\"Juan Perez\",\"documento\":\"1234567\",\"suscripcionId\":null}"
    );

    publisher.publishSingle(event);

    ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
    verify(rabbitTemplate).convertAndSend(eq("pacientes.events"), eq("paciente.paciente-creado"), messageCaptor.capture());
    verify(repository).save(event);

    JsonNode envelope = objectMapper.readTree(messageCaptor.getValue());
    assertEquals("PatientCreatedEvent", envelope.get("event_name").asText());
    assertEquals("PatientCreatedEvent", envelope.get("event").asText());
    assertEquals(1, envelope.get("schema_version").asInt());
    assertEquals(patientId.toString(), envelope.get("aggregate_id").asText());

    assertDoesNotThrow(() -> UUID.fromString(envelope.get("event_id").asText()));
    assertDoesNotThrow(() -> UUID.fromString(envelope.get("correlation_id").asText()));
    assertTrue(envelope.get("occurred_on").asText().endsWith("Z"));

    JsonNode payload = envelope.get("payload");
    assertEquals(patientId.toString(), payload.get("pacienteId").asText());
    assertEquals("Juan Perez", payload.get("nombre").asText());
    assertEquals("1234567", payload.get("documento").asText());
    assertTrue(payload.has("suscripcionId"));
    assertTrue(payload.get("suscripcionId").isNull());
    assertNotNull(event.getProcessedOn());
  }

  @Test
  void publishSingle_shouldPublishEnvelopeForPatientUpdated() throws IOException {
    OutboxEventRepository repository = mock(OutboxEventRepository.class);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    OutboxPublisherProperties props = new OutboxPublisherProperties();
    props.setExchange("pacientes.events");

    OutboxPublisher publisher = new OutboxPublisher(repository, rabbitTemplate, props, objectMapper);

    UUID patientId = UUID.randomUUID();
    UUID subscriptionId = UUID.randomUUID();
    OutboxEventEntity event = buildOutboxEvent(
      "PatientUpdatedEvent",
      "paciente.paciente-actualizado",
      patientId,
      UUID.randomUUID(),
      UUID.randomUUID(),
      "{\"pacienteId\":\"" + patientId + "\",\"nombre\":\"Juan Perez\",\"documento\":\"1234567\",\"suscripcionId\":\"" + subscriptionId + "\"}"
    );

    publisher.publishSingle(event);

    ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
    verify(rabbitTemplate).convertAndSend(eq("pacientes.events"), eq("paciente.paciente-actualizado"), messageCaptor.capture());
    verify(repository).save(event);

    JsonNode envelope = objectMapper.readTree(messageCaptor.getValue());
    assertEquals("PatientUpdatedEvent", envelope.get("event_name").asText());
    assertEquals(1, envelope.get("schema_version").asInt());
    JsonNode payload = envelope.get("payload");
    assertEquals(patientId.toString(), payload.get("pacienteId").asText());
    assertEquals(subscriptionId.toString(), payload.get("suscripcionId").asText());
  }

  @Test
  void publishSingle_shouldPublishEnvelopeForPatientDeletedWithMinimalPayload() throws IOException {
    OutboxEventRepository repository = mock(OutboxEventRepository.class);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    OutboxPublisherProperties props = new OutboxPublisherProperties();
    props.setExchange("pacientes.events");

    OutboxPublisher publisher = new OutboxPublisher(repository, rabbitTemplate, props, objectMapper);

    UUID patientId = UUID.randomUUID();
    OutboxEventEntity event = buildOutboxEvent(
      "PatientDeletedEvent",
      "paciente.paciente-eliminado",
      patientId,
      UUID.randomUUID(),
      UUID.randomUUID(),
      "{\"pacienteId\":\"" + patientId + "\"}"
    );

    publisher.publishSingle(event);

    ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
    verify(rabbitTemplate).convertAndSend(eq("pacientes.events"), eq("paciente.paciente-eliminado"), messageCaptor.capture());
    verify(repository).save(event);

    JsonNode envelope = objectMapper.readTree(messageCaptor.getValue());
    assertEquals("PatientDeletedEvent", envelope.get("event_name").asText());
    JsonNode payload = envelope.get("payload");
    assertEquals(1, payload.size());
    assertEquals(patientId.toString(), payload.get("pacienteId").asText());
  }

  private OutboxEventEntity buildOutboxEvent(
    String eventName,
    String routingKey,
    UUID aggregateId,
    UUID eventId,
    UUID correlationId,
    String payload
  ) {
    OutboxEventEntity event = new OutboxEventEntity();
    event.setId(UUID.randomUUID());
    event.setEventId(eventId);
    event.setEventName(eventName);
    event.setRoutingKey(routingKey);
    event.setSchemaVersion(1);
    event.setCorrelationId(correlationId);
    event.setAggregateId(aggregateId.toString());
    event.setOccurredOn(LocalDateTime.now());
    event.setPayload(payload);
    event.setAttempts(0);
    event.setNextAttemptAt(LocalDateTime.now());
    return event;
  }
}
