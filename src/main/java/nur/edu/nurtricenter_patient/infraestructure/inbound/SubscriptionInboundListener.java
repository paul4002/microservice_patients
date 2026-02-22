package nur.edu.nurtricenter_patient.infraestructure.inbound;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import an.awesome.pipelinr.Pipeline;
import nur.edu.nurtricenter_patient.application.subscriptions.processSubscriptionEvent.ProcessSubscriptionEventCommand;
import nur.edu.nurtricenter_patient.core.results.Result;

@Component
public class SubscriptionInboundListener {
  private static final Logger log = LoggerFactory.getLogger(SubscriptionInboundListener.class);

  private final Pipeline pipeline;
  private final ObjectMapper objectMapper;

  public SubscriptionInboundListener(Pipeline pipeline, ObjectMapper objectMapper) {
    this.pipeline = pipeline;
    this.objectMapper = objectMapper;
  }

  @RabbitListener(queues = "${inbound.rabbitmq.queue:pacientes.inbound}")
  public void onMessage(Message message) {
    try {
      String body = new String(message.getBody());
      Map<String, Object> parsed = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});

      String routingKey = message.getMessageProperties() != null
        ? message.getMessageProperties().getReceivedRoutingKey()
        : null;

      String eventName = readString(parsed, "event", "event_name", "eventName");
      Map<String, Object> payload = extractPayload(parsed);

      Result result = new ProcessSubscriptionEventCommand(eventName, payload, routingKey).execute(pipeline);
      if (result.isFailure()) {
        log.warn("Inbound subscription event failed: event={} routingKey={} error={}", eventName, routingKey, result.getError().getDescription());
      }
    } catch (Exception ex) {
      log.error("Inbound subscription message parse failure", ex);
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> extractPayload(Map<String, Object> parsed) {
    Object payload = parsed.get("payload");
    if (payload instanceof Map<?, ?> mapPayload) {
      return (Map<String, Object>) mapPayload;
    }
    return parsed;
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

