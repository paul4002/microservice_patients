package nur.edu.nurtricenter_patient.infraestructure.outbox;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class OutboxPublisher {
  private final OutboxEventRepository repository;
  private final RabbitTemplate rabbitTemplate;
  private final OutboxPublisherProperties props;
  private final ObjectMapper objectMapper;

  public OutboxPublisher(OutboxEventRepository repository, RabbitTemplate rabbitTemplate, OutboxPublisherProperties props, ObjectMapper objectMapper) {
    this.repository = repository;
    this.rabbitTemplate = rabbitTemplate;
    this.props = props;
    this.objectMapper = objectMapper;
  }

  @Scheduled(fixedDelayString = "${rabbitmq.outbox-poll-interval-ms:1000}")
  public void publishPending() {
    List<OutboxEventEntity> events = repository.findByProcessedOnIsNullAndNextAttemptAtLessThanEqualOrderByOccurredOnAsc(
      LocalDateTime.now(),
      PageRequest.of(0, props.getOutboxBatchSize())
    );
    for (OutboxEventEntity event : events) {
      publishSingle(event);
    }
  }

  @Transactional
  protected void publishSingle(OutboxEventEntity event) {
    try {
      String routingKey = resolveRoutingKey(event);
      String envelope = buildEnvelope(event);
      rabbitTemplate.convertAndSend(props.getExchange(), routingKey, envelope);
      event.setProcessedOn(LocalDateTime.now());
      event.setLastError(null);
      repository.save(event);
    } catch (Exception ex) {
      int attempts = event.getAttempts() != null ? event.getAttempts() + 1 : 1;
      event.setAttempts(attempts);
      event.setLastError(ex.getMessage());
      long baseBackoffMs = props.getPublishBackoffMs();
      int cap = props.getPublishRetries() > 0 ? props.getPublishRetries() : 10;
      long delayMs = baseBackoffMs * Math.min(attempts, cap);
      event.setNextAttemptAt(LocalDateTime.now().plusNanos(delayMs * 1_000_000));
      repository.save(event);
    }
  }

  private String resolveRoutingKey(OutboxEventEntity event) {
    if (event.getRoutingKey() != null && !event.getRoutingKey().isBlank()) {
      return event.getRoutingKey();
    }
    if (event.getEventName() != null && event.getEventName().contains(".")) {
      return event.getEventName();
    }
    if (event.getEventType() != null && event.getEventType().contains(".")) {
      return event.getEventType();
    }
    return props.getRoutingKey() != null ? props.getRoutingKey() : "";
  }

  private String buildEnvelope(OutboxEventEntity event) {
    String eventName = resolveEventName(event);
    String occurredOn = resolveOccurredOn(event);
    UUID eventId = event.getEventId() != null ? event.getEventId() : UUID.randomUUID();
    UUID correlationId = event.getCorrelationId() != null ? event.getCorrelationId() : UUID.randomUUID();
    int schemaVersion = event.getSchemaVersion() != null ? event.getSchemaVersion() : 1;
    JsonNode payload = parsePayload(event.getPayload());

    Envelope envelope = new Envelope(
      eventId.toString(),
      eventName,
      eventName,
      occurredOn,
      schemaVersion,
      correlationId.toString(),
      event.getAggregateId(),
      payload
    );

    try {
      return objectMapper.writeValueAsString(envelope);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("Failed to serialize outbox envelope", ex);
    }
  }

  private String resolveEventName(OutboxEventEntity event) {
    if (event.getEventName() != null && !event.getEventName().isBlank() && !event.getEventName().contains(".")) {
      return event.getEventName();
    }
    if (event.getEventType() != null && !event.getEventType().isBlank() && !event.getEventType().contains(".")) {
      return event.getEventType();
    }
    return "UnknownEvent";
  }

  private String resolveOccurredOn(OutboxEventEntity event) {
    LocalDateTime occurredOn = event.getOccurredOn() != null ? event.getOccurredOn() : LocalDateTime.now(ZoneOffset.UTC);
    return occurredOn.toInstant(ZoneOffset.UTC).toString();
  }

  private JsonNode parsePayload(String payload) {
    try {
      return payload == null ? objectMapper.nullNode() : objectMapper.readTree(payload);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("Failed to parse outbox payload", ex);
    }
  }

  private record Envelope(
    String event_id,
    String event_name,
    String event,
    String occurred_on,
    int schema_version,
    String correlation_id,
    String aggregate_id,
    JsonNode payload
  ) {}
}
