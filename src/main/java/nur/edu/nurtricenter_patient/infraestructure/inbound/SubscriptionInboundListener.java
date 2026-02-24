package nur.edu.nurtricenter_patient.infraestructure.inbound;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import an.awesome.pipelinr.Pipeline;
import nur.edu.nurtricenter_patient.application.subscriptions.processSubscriptionEvent.ProcessSubscriptionEventCommand;
import nur.edu.nurtricenter_patient.core.results.ErrorType;
import nur.edu.nurtricenter_patient.core.results.Result;

@Component
public class SubscriptionInboundListener {
  private static final Logger log = LoggerFactory.getLogger(SubscriptionInboundListener.class);
  private static final Logger auditLog = LoggerFactory.getLogger("rabbit.audit.inbound");

  private final Pipeline pipeline;
  private final ObjectMapper objectMapper;
  private final InboundSubscriptionProperties props;
  private final InboundEventMetrics metrics;

  public SubscriptionInboundListener(
    Pipeline pipeline,
    ObjectMapper objectMapper,
    InboundSubscriptionProperties props,
    InboundEventMetrics metrics
  ) {
    this.pipeline = pipeline;
    this.objectMapper = objectMapper;
    this.props = props;
    this.metrics = metrics;
  }

  @RabbitListener(
    queues = "${inbound.rabbitmq.queue:pacientes.inbound}",
    autoStartup = "#{@inboundConsumerToggle.enabled}"
  )
  public void onMessage(Message message) {
    try {
      String body = new String(message.getBody(), StandardCharsets.UTF_8);
      Map<String, Object> parsed = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});

      String exchange = message.getMessageProperties() != null
        ? message.getMessageProperties().getReceivedExchange()
        : null;
      String queue = message.getMessageProperties() != null
        ? message.getMessageProperties().getConsumerQueue()
        : null;
      String routingKey = message.getMessageProperties() != null
        ? message.getMessageProperties().getReceivedRoutingKey()
        : null;

      String eventName = readString(parsed, "event", "event_name", "eventName");
      if (eventName == null || eventName.isBlank()) {
        throw new AmqpRejectAndDontRequeueException("Missing event field");
      }
      Map<String, Object> payload = extractPayloadRequired(parsed);
      UUID eventId = readRequiredUuid(parsed, "event_id", "eventId");
      UUID correlationId = readRequiredUuid(parsed, "correlation_id", "correlationId");
      Integer schemaVersion = readRequiredInt(parsed, "schema_version", "schemaVersion");
      validateSchemaVersion(schemaVersion);
      String occurredOn = readRequiredString(parsed, "occurred_on", "occurredOn");
      validateOccurredOn(occurredOn);
      metrics.incrementReceived(eventName);
      auditLog.info(
        "Rabbit inbound received: event={} eventId={} correlationId={} schemaVersion={} exchange={} routingKey={} queue={}",
        eventName,
        eventId,
        correlationId,
        schemaVersion,
        exchange,
        routingKey,
        queue
      );

      try (
        MDC.MDCCloseable ignoredCorrelation = putMdc("correlation_id", correlationId != null ? correlationId.toString() : null);
        MDC.MDCCloseable ignoredEventId = putMdc("event_id", eventId != null ? eventId.toString() : null)
      ) {
        Result result = new ProcessSubscriptionEventCommand(
          eventName,
          payload,
          routingKey,
          eventId,
          correlationId,
          schemaVersion,
          occurredOn,
          body
        ).execute(pipeline);
        if (result.isFailure()) {
          log.warn("Inbound subscription event failed: event={} routingKey={} error={}", eventName, routingKey, result.getError().getDescription());
          auditLog.warn(
            "Rabbit inbound failed: event={} eventId={} correlationId={} routingKey={} error={}",
            eventName,
            eventId,
            correlationId,
            routingKey,
            result.getError().getDescription()
          );
          if (result.getError().getType() == ErrorType.VALIDATION || result.getError().getType() == ErrorType.CONFLICT || result.getError().getType() == ErrorType.NOT_FOUND) {
            throw new AmqpRejectAndDontRequeueException(result.getError().getDescription());
          }
          throw new IllegalStateException(result.getError().getDescription());
        }
        auditLog.info(
          "Rabbit inbound processed: event={} eventId={} correlationId={} routingKey={}",
          eventName,
          eventId,
          correlationId,
          routingKey
        );
      }
    } catch (AmqpRejectAndDontRequeueException ex) {
      auditLog.warn("Rabbit inbound rejected: reason={}", ex.getMessage());
      throw ex;
    } catch (IllegalStateException ex) {
      auditLog.error("Rabbit inbound processing error: reason={}", ex.getMessage());
      throw ex;
    } catch (Exception ex) {
      log.error("Inbound subscription message parse failure", ex);
      auditLog.error("Rabbit inbound parse failure: reason={}", ex.getMessage());
      throw new AmqpRejectAndDontRequeueException("Inbound message rejected", ex);
    }
  }

  private MDC.MDCCloseable putMdc(String key, String value) {
    if (value == null || value.isBlank()) {
      return MDC.putCloseable(key, "");
    }
    return MDC.putCloseable(key, value);
  }

  private void validateSchemaVersion(Integer schemaVersion) {
    List<Integer> allowed = props.getSchemaVersions();
    if (allowed == null || allowed.isEmpty()) {
      return;
    }
    if (!allowed.contains(schemaVersion)) {
      throw new AmqpRejectAndDontRequeueException("Unsupported schema_version: " + schemaVersion);
    }
  }

  private void validateOccurredOn(String occurredOn) {
    try {
      OffsetDateTime.parse(occurredOn);
    } catch (DateTimeParseException ex) {
      throw new AmqpRejectAndDontRequeueException("Invalid occurred_on format");
    }
  }

  private UUID readRequiredUuid(Map<String, Object> values, String... keys) {
    String raw = readRequiredString(values, keys);
    try {
      return UUID.fromString(raw);
    } catch (IllegalArgumentException ex) {
      throw new AmqpRejectAndDontRequeueException("Invalid UUID field");
    }
  }

  private Integer readRequiredInt(Map<String, Object> values, String... keys) {
    Integer value = readInt(values, keys);
    if (value == null) {
      throw new AmqpRejectAndDontRequeueException("Missing integer field");
    }
    return value;
  }

  private String readRequiredString(Map<String, Object> values, String... keys) {
    String raw = readString(values, keys);
    if (raw == null || raw.isBlank()) {
      throw new AmqpRejectAndDontRequeueException("Missing required field");
    }
    return raw;
  }

  private Integer readInt(Map<String, Object> values, String... keys) {
    for (String key : keys) {
      Object value = values.get(key);
      if (value == null) {
        continue;
      }
      if (value instanceof Integer i) {
        return i;
      }
      if (value instanceof Number n) {
        return n.intValue();
      }
      if (value instanceof String s && !s.isBlank()) {
        try {
          return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
          throw new AmqpRejectAndDontRequeueException("Invalid integer field");
        }
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> extractPayloadRequired(Map<String, Object> parsed) {
    Object payload = parsed.get("payload");
    if (payload instanceof Map<?, ?> mapPayload) {
      return (Map<String, Object>) mapPayload;
    }
    throw new AmqpRejectAndDontRequeueException("Missing payload object");
  }

  private String readString(Map<String, Object> values, String... keys) {
    for (String key : keys) {
      Object value = values.get(key);
      if (value instanceof String str && !str.isBlank()) {
        return str;
      }
    }
    return null;
  }
}
