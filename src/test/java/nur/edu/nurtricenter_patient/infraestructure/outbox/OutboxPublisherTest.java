package nur.edu.nurtricenter_patient.infraestructure.outbox;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class OutboxPublisherTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void publishSingle_shouldPublishEnvelopeForPatientCreated() throws IOException {
    OutboxEventRepository repository = mock(OutboxEventRepository.class);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    RabbitAdmin rabbitAdmin = mock(RabbitAdmin.class);
    OutboxPublisherProperties props = new OutboxPublisherProperties();
    props.setExchange("outbox.events");

    OutboxPublisher publisher = new OutboxPublisher(repository, rabbitTemplate, rabbitAdmin, props, objectMapper, stubTransactionManager());

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
    verify(rabbitTemplate).convertAndSend(eq("outbox.events"), eq("paciente.paciente-creado"), messageCaptor.capture());
    verify(repository).save(event);

    JsonNode envelope = objectMapper.readTree(messageCaptor.getValue());
    assertEquals("paciente.paciente-creado", envelope.get("event_name").asText());
    assertEquals("paciente.paciente-creado", envelope.get("event").asText());
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
    RabbitAdmin rabbitAdmin = mock(RabbitAdmin.class);
    OutboxPublisherProperties props = new OutboxPublisherProperties();
    props.setExchange("outbox.events");

    OutboxPublisher publisher = new OutboxPublisher(repository, rabbitTemplate, rabbitAdmin, props, objectMapper, stubTransactionManager());

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
    verify(rabbitTemplate).convertAndSend(eq("outbox.events"), eq("paciente.paciente-actualizado"), messageCaptor.capture());
    verify(repository).save(event);

    JsonNode envelope = objectMapper.readTree(messageCaptor.getValue());
    assertEquals("paciente.paciente-actualizado", envelope.get("event_name").asText());
    assertEquals(1, envelope.get("schema_version").asInt());
    JsonNode payload = envelope.get("payload");
    assertEquals(patientId.toString(), payload.get("pacienteId").asText());
    assertEquals(subscriptionId.toString(), payload.get("suscripcionId").asText());
  }

  @Test
  void publishSingle_shouldPublishEnvelopeForPatientDeletedWithMinimalPayload() throws IOException {
    OutboxEventRepository repository = mock(OutboxEventRepository.class);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    RabbitAdmin rabbitAdmin = mock(RabbitAdmin.class);
    OutboxPublisherProperties props = new OutboxPublisherProperties();
    props.setExchange("outbox.events");

    OutboxPublisher publisher = new OutboxPublisher(repository, rabbitTemplate, rabbitAdmin, props, objectMapper, stubTransactionManager());

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
    verify(rabbitTemplate).convertAndSend(eq("outbox.events"), eq("paciente.paciente-eliminado"), messageCaptor.capture());
    verify(repository).save(event);

    JsonNode envelope = objectMapper.readTree(messageCaptor.getValue());
    assertEquals("paciente.paciente-eliminado", envelope.get("event_name").asText());
    JsonNode payload = envelope.get("payload");
    assertEquals(1, payload.size());
    assertEquals(patientId.toString(), payload.get("pacienteId").asText());
  }

  @Test
  void publishSingle_rabbitTemplateFails_recordsFailureAndDoesNotMarkProcessed() {
    OutboxEventRepository repository = mock(OutboxEventRepository.class);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    RabbitAdmin rabbitAdmin = mock(RabbitAdmin.class);
    OutboxPublisherProperties props = new OutboxPublisherProperties();
    props.setExchange("outbox.events");
    props.setPublishRetries(3);
    props.setPublishBackoffMs(100);

    OutboxPublisher publisher = new OutboxPublisher(repository, rabbitTemplate, rabbitAdmin, props, objectMapper, stubTransactionManager());

    UUID patientId = UUID.randomUUID();
    OutboxEventEntity event = buildOutboxEvent(
      "PatientCreatedEvent", "paciente.paciente-creado",
      patientId, UUID.randomUUID(), UUID.randomUUID(), "{\"pacienteId\":\"" + patientId + "\"}"
    );
    event.setAttempts(0);

    doThrow(new RuntimeException("rabbit down"))
        .when(rabbitTemplate).convertAndSend(any(), any(String.class), any(String.class));

    publisher.publishSingle(event);

    verify(repository).save(event);
    assertNull(event.getProcessedOn());
  }

  @Test
  void publishSingle_noRoutingKey_fallsBackToPropsRoutingKey() throws IOException {
    OutboxEventRepository repository = mock(OutboxEventRepository.class);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    RabbitAdmin rabbitAdmin = mock(RabbitAdmin.class);
    OutboxPublisherProperties props = new OutboxPublisherProperties();
    props.setExchange("outbox.events");
    props.setRoutingKey("default.routing.key");

    OutboxPublisher publisher = new OutboxPublisher(repository, rabbitTemplate, rabbitAdmin, props, objectMapper, stubTransactionManager());

    UUID patientId = UUID.randomUUID();
    OutboxEventEntity event = new OutboxEventEntity();
    event.setId(UUID.randomUUID());
    event.setEventId(UUID.randomUUID());
    event.setEventName("SomeEvent");
    event.setRoutingKey("");
    event.setEventType("");
    event.setSchemaVersion(1);
    event.setCorrelationId(UUID.randomUUID());
    event.setAggregateId(patientId.toString());
    event.setOccurredOn(LocalDateTime.now());
    event.setPayload("{\"id\":\"" + patientId + "\"}");
    event.setAttempts(0);
    event.setNextAttemptAt(LocalDateTime.now());

    publisher.publishSingle(event);

    ArgumentCaptor<String> routingCaptor = ArgumentCaptor.forClass(String.class);
    verify(rabbitTemplate).convertAndSend(any(), routingCaptor.capture(), any(String.class));
    assertEquals("default.routing.key", routingCaptor.getValue());
  }

  @Test
  void publishSingle_eventNameWithoutDot_usesEventTypeFallback() {
    OutboxEventRepository repository = mock(OutboxEventRepository.class);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    RabbitAdmin rabbitAdmin = mock(RabbitAdmin.class);
    OutboxPublisherProperties props = new OutboxPublisherProperties();
    props.setExchange("outbox.events");

    OutboxPublisher publisher = new OutboxPublisher(repository, rabbitTemplate, rabbitAdmin, props, objectMapper, stubTransactionManager());

    OutboxEventEntity event = new OutboxEventEntity();
    event.setId(UUID.randomUUID());
    event.setEventId(UUID.randomUUID());
    event.setEventName("EventWithoutDot");
    event.setRoutingKey(null);
    event.setEventType("paciente.via-type");
    event.setSchemaVersion(1);
    event.setCorrelationId(UUID.randomUUID());
    event.setAggregateId("some-id");
    event.setOccurredOn(LocalDateTime.now());
    event.setPayload("{\"id\":\"test\"}");
    event.setAttempts(0);
    event.setNextAttemptAt(LocalDateTime.now());

    publisher.publishSingle(event);

    ArgumentCaptor<String> routingCaptor = ArgumentCaptor.forClass(String.class);
    verify(rabbitTemplate).convertAndSend(any(), routingCaptor.capture(), any(String.class));
    assertEquals("paciente.via-type", routingCaptor.getValue());
  }

  @Test
  void publishSingle_nullPayload_usesNullNode() throws IOException {
    OutboxEventRepository repository = mock(OutboxEventRepository.class);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    RabbitAdmin rabbitAdmin = mock(RabbitAdmin.class);
    OutboxPublisherProperties props = new OutboxPublisherProperties();
    props.setExchange("outbox.events");

    OutboxPublisher publisher = new OutboxPublisher(repository, rabbitTemplate, rabbitAdmin, props, objectMapper, stubTransactionManager());

    OutboxEventEntity event = buildOutboxEvent(
      "PatientCreatedEvent", "paciente.paciente-creado",
      UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null
    );

    publisher.publishSingle(event);

    ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
    verify(rabbitTemplate).convertAndSend(any(), any(String.class), messageCaptor.capture());
    JsonNode envelope = objectMapper.readTree(messageCaptor.getValue());
    assertTrue(envelope.get("payload").isNull());
  }

  @Test
  void publishSingle_nullEventIdAndCorrelationId_generatesRandomOnes() throws IOException {
    OutboxEventRepository repository = mock(OutboxEventRepository.class);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    RabbitAdmin rabbitAdmin = mock(RabbitAdmin.class);
    OutboxPublisherProperties props = new OutboxPublisherProperties();
    props.setExchange("outbox.events");

    OutboxPublisher publisher = new OutboxPublisher(repository, rabbitTemplate, rabbitAdmin, props, objectMapper, stubTransactionManager());

    OutboxEventEntity event = buildOutboxEvent(
      "PatientCreatedEvent", "paciente.paciente-creado",
      UUID.randomUUID(), null, null, "{\"id\":\"test\"}"
    );

    publisher.publishSingle(event);

    ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
    verify(rabbitTemplate).convertAndSend(any(), any(String.class), messageCaptor.capture());
    JsonNode envelope = objectMapper.readTree(messageCaptor.getValue());
    assertDoesNotThrow(() -> UUID.fromString(envelope.get("event_id").asText()));
    assertDoesNotThrow(() -> UUID.fromString(envelope.get("correlation_id").asText()));
  }

  @Test
  void publishSingle_exchangeTypeTopicBinding() {
    OutboxEventRepository repository = mock(OutboxEventRepository.class);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    RabbitAdmin rabbitAdmin = mock(RabbitAdmin.class);
    OutboxPublisherProperties props = new OutboxPublisherProperties();
    props.setExchange("outbox.events");
    props.setExchangeType("topic");

    OutboxPublisher publisher = new OutboxPublisher(repository, rabbitTemplate, rabbitAdmin, props, objectMapper, stubTransactionManager());

    OutboxEventEntity event = buildOutboxEvent(
      "PatientCreatedEvent", "paciente.paciente-creado",
      UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "{\"id\":\"test\"}"
    );

    publisher.publishSingle(event);

    verify(rabbitTemplate).convertAndSend(any(), any(String.class), any(String.class));
  }

  @Test
  void publishSingle_exchangeTypeDirectBinding() {
    OutboxEventRepository repository = mock(OutboxEventRepository.class);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    RabbitAdmin rabbitAdmin = mock(RabbitAdmin.class);
    OutboxPublisherProperties props = new OutboxPublisherProperties();
    props.setExchange("outbox.events");
    props.setExchangeType("direct");

    OutboxPublisher publisher = new OutboxPublisher(repository, rabbitTemplate, rabbitAdmin, props, objectMapper, stubTransactionManager());

    OutboxEventEntity event = buildOutboxEvent(
      "PatientCreatedEvent", "paciente.paciente-creado",
      UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "{\"id\":\"test\"}"
    );

    publisher.publishSingle(event);

    verify(rabbitTemplate).convertAndSend(any(), any(String.class), any(String.class));
  }

  @Test
  void publishSingle_unknownExchangeType_usesDefaultFanout() {
    OutboxEventRepository repository = mock(OutboxEventRepository.class);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    RabbitAdmin rabbitAdmin = mock(RabbitAdmin.class);
    OutboxPublisherProperties props = new OutboxPublisherProperties();
    props.setExchange("outbox.events");
    props.setExchangeType("custom");

    OutboxPublisher publisher = new OutboxPublisher(repository, rabbitTemplate, rabbitAdmin, props, objectMapper, stubTransactionManager());

    OutboxEventEntity event = buildOutboxEvent(
      "PatientCreatedEvent", "paciente.paciente-creado",
      UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "{\"id\":\"test\"}"
    );

    publisher.publishSingle(event);

    verify(rabbitTemplate).convertAndSend(any(), any(String.class), any(String.class));
  }

  @Test
  void publishPending_emptyList_doesNothing() {
    OutboxEventRepository repository = mock(OutboxEventRepository.class);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    RabbitAdmin rabbitAdmin = mock(RabbitAdmin.class);
    OutboxPublisherProperties props = new OutboxPublisherProperties();
    props.setExchange("outbox.events");
    when(repository.findByProcessedOnIsNullAndNextAttemptAtLessThanEqualOrderByOccurredOnAsc(any(), any()))
        .thenReturn(java.util.List.of());

    OutboxPublisher publisher = new OutboxPublisher(repository, rabbitTemplate, rabbitAdmin, props, objectMapper, stubTransactionManager());
    publisher.publishPending();

    org.mockito.Mockito.verifyNoInteractions(rabbitTemplate);
  }

  @Test
  void publishPending_withOneEvent_publishesIt() {
    OutboxEventRepository repository = mock(OutboxEventRepository.class);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    RabbitAdmin rabbitAdmin = mock(RabbitAdmin.class);
    OutboxPublisherProperties props = new OutboxPublisherProperties();
    props.setExchange("outbox.events");

    OutboxEventEntity event = buildOutboxEvent(
        "PatientCreatedEvent", "paciente.paciente-creado",
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "{\"id\":\"test\"}"
    );
    when(repository.findByProcessedOnIsNullAndNextAttemptAtLessThanEqualOrderByOccurredOnAsc(any(), any()))
        .thenReturn(java.util.List.of(event));

    OutboxPublisher publisher = new OutboxPublisher(repository, rabbitTemplate, rabbitAdmin, props, objectMapper, stubTransactionManager());
    publisher.publishPending();

    verify(rabbitTemplate).convertAndSend(any(), any(String.class), any(String.class));
  }

  @Test
  void ensureEventQueueBinding_exception_removesFromCacheAndContinues() {
    OutboxEventRepository repository = mock(OutboxEventRepository.class);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    RabbitAdmin rabbitAdmin = mock(RabbitAdmin.class);
    OutboxPublisherProperties props = new OutboxPublisherProperties();
    props.setExchange("outbox.events");
    doThrow(new RuntimeException("rabbit admin error")).when(rabbitAdmin).declareExchange(any());

    OutboxPublisher publisher = new OutboxPublisher(repository, rabbitTemplate, rabbitAdmin, props, objectMapper, stubTransactionManager());

    OutboxEventEntity event = buildOutboxEvent(
        "PatientCreatedEvent", "paciente.test-event",
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "{\"id\":\"test\"}"
    );

    publisher.publishSingle(event);

    verify(rabbitTemplate).convertAndSend(any(), any(String.class), any(String.class));
  }

  @Test
  void publishSingle_invalidPayloadJson_recordsFailureAndDoesNotSend() {
    OutboxEventRepository repository = mock(OutboxEventRepository.class);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    RabbitAdmin rabbitAdmin = mock(RabbitAdmin.class);
    OutboxPublisherProperties props = new OutboxPublisherProperties();
    props.setExchange("outbox.events");
    props.setPublishRetries(3);
    props.setPublishBackoffMs(0);

    OutboxPublisher publisher = new OutboxPublisher(repository, rabbitTemplate, rabbitAdmin, props, objectMapper, stubTransactionManager());

    OutboxEventEntity event = buildOutboxEvent(
        "PatientCreatedEvent", "paciente.paciente-creado",
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "{invalid-json"
    );
    event.setAttempts(0);

    publisher.publishSingle(event);

    org.mockito.Mockito.verifyNoInteractions(rabbitTemplate);
    verify(repository).save(event);
  }

  private PlatformTransactionManager stubTransactionManager() {
    PlatformTransactionManager txManager = mock(PlatformTransactionManager.class);
    when(txManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
    return txManager;
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
