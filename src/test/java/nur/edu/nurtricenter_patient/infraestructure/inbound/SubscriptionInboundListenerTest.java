package nur.edu.nurtricenter_patient.infraestructure.inbound;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import an.awesome.pipelinr.Pipeline;
import com.fasterxml.jackson.databind.ObjectMapper;

import nur.edu.nurtricenter_patient.core.results.Error;
import nur.edu.nurtricenter_patient.core.results.ErrorType;
import nur.edu.nurtricenter_patient.core.results.Result;

@SuppressWarnings("unchecked")
class SubscriptionInboundListenerTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private AtomicReference<Result> pipelineResult;
  private Pipeline pipeline;
  private SubscriptionInboundListener listener;

  @BeforeEach
  void setUp() {
    pipelineResult = new AtomicReference<>(Result.success());
    pipeline = mock(Pipeline.class, inv -> {
      if ("send".equals(inv.getMethod().getName())
          && inv.getMethod().getReturnType() != void.class) {
        return pipelineResult.get();
      }
      return null;
    });

    InboundSubscriptionProperties props = new InboundSubscriptionProperties();
    props.setSchemaVersions(List.of(1));
    listener = new SubscriptionInboundListener(
        pipeline, objectMapper, props,
        new InboundEventMetrics(new SimpleMeterRegistry())
    );
  }

  @Test
  void happyPath_validMessage_pipelineSuccess_processes() {
    assertDoesNotThrow(() ->
        listener.onMessage(buildMessage(validBody("contrato.creado"), "contrato.creado")));
  }

  @Test
  void validationFailure_pipelineReturnsValidationError_throwsAmqpReject() {
    pipelineResult.set(Result.failure(new Error("Err", "bad input", ErrorType.VALIDATION)));

    assertThrows(AmqpRejectAndDontRequeueException.class,
        () -> listener.onMessage(buildMessage(validBody("contrato.creado"), "contrato.creado")));
  }

  @Test
  void notFoundFailure_pipelineReturnsNotFoundError_throwsAmqpReject() {
    pipelineResult.set(Result.failure(new Error("Err", "not found", ErrorType.NOT_FOUND)));

    assertThrows(AmqpRejectAndDontRequeueException.class,
        () -> listener.onMessage(buildMessage(validBody("contrato.creado"), "contrato.creado")));
  }

  @Test
  void conflictFailure_pipelineReturnsConflictError_throwsAmqpReject() {
    pipelineResult.set(Result.failure(new Error("Err", "conflict", ErrorType.CONFLICT)));

    assertThrows(AmqpRejectAndDontRequeueException.class,
        () -> listener.onMessage(buildMessage(validBody("contrato.creado"), "contrato.creado")));
  }

  @Test
  void internalFailure_pipelineReturnsFailureError_throwsIllegalState() {
    pipelineResult.set(Result.failure(new Error("Err", "internal error", ErrorType.FAILURE)));

    assertThrows(IllegalStateException.class,
        () -> listener.onMessage(buildMessage(validBody("contrato.creado"), "contrato.creado")));
  }

  @Test
  void invalidJson_throwsAmqpReject() {
    Message message = new Message("not-json".getBytes(StandardCharsets.UTF_8), new MessageProperties());
    assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.onMessage(message));
  }

  @Test
  void invalidOccurredOnFormat_throwsAmqpReject() {
    String body = """
        {
          "event":"contrato.creado",
          "event_id":"00000000-0000-0000-0000-000000000001",
          "correlation_id":"00000000-0000-0000-0000-000000000002",
          "schema_version":1,
          "occurred_on":"not-a-date",
          "payload":{"key":"value"}
        }
        """;
    assertThrows(AmqpRejectAndDontRequeueException.class,
        () -> listener.onMessage(buildMessage(body, "contrato.creado")));
  }

  @Test
  void emptySchemaVersions_anyVersionAllowed_processes() {
    InboundSubscriptionProperties props = new InboundSubscriptionProperties();
    props.setSchemaVersions(List.of());
    SubscriptionInboundListener listenerNoVersionCheck = new SubscriptionInboundListener(
        pipeline, objectMapper, props,
        new InboundEventMetrics(new SimpleMeterRegistry())
    );
    String body = """
        {
          "event":"unknown.event",
          "event_id":"00000000-0000-0000-0000-000000000001",
          "correlation_id":"00000000-0000-0000-0000-000000000002",
          "schema_version":99,
          "occurred_on":"2026-02-23T00:00:00Z",
          "payload":{"key":"value"}
        }
        """;
    assertDoesNotThrow(() ->
        listenerNoVersionCheck.onMessage(buildMessage(body, "unknown.event")));
  }

  @Test
  void messageWithExchangeQueueAndRoutingKey_passedToCommand() {
    MessageProperties props = new MessageProperties();
    props.setReceivedExchange("test-exchange");
    props.setConsumerQueue("test-queue");
    props.setReceivedRoutingKey("contrato.creado");
    Message message = new Message(validBody("contrato.creado").getBytes(StandardCharsets.UTF_8), props);

    assertDoesNotThrow(() -> listener.onMessage(message));
  }

  @Test
  void missingEventName_throwsAmqpReject() {
    String body = """
        {
          "event_id":"00000000-0000-0000-0000-000000000001",
          "correlation_id":"00000000-0000-0000-0000-000000000002",
          "schema_version":1,
          "occurred_on":"2026-02-23T00:00:00Z",
          "payload":{"key":"value"}
        }
        """;
    assertThrows(AmqpRejectAndDontRequeueException.class,
        () -> listener.onMessage(buildMessage(body, "test.event")));
  }

  @Test
  void longErrorMessage_handled() {
    pipelineResult.set(Result.failure(new Error("Err", "x".repeat(300), ErrorType.VALIDATION)));

    assertThrows(AmqpRejectAndDontRequeueException.class,
        () -> listener.onMessage(buildMessage(validBody("contrato.creado"), "contrato.creado")));
  }

  @Test
  void missingCorrelationId_throwsMissingRequired() {
    String body = """
        {
          "event":"contrato.creado",
          "event_id":"00000000-0000-0000-0000-000000000001",
          "schema_version":1,
          "occurred_on":"2026-02-23T00:00:00Z",
          "payload":{"key":"value"}
        }
        """;
    assertThrows(AmqpRejectAndDontRequeueException.class,
        () -> listener.onMessage(buildMessage(body, "contrato.creado")));
  }

  @Test
  void illegalStateException_wrappedAndRethrown() {
    pipelineResult.set(Result.failure(new Error("Err", "fail", ErrorType.FAILURE)));

    assertThrows(IllegalStateException.class,
        () -> listener.onMessage(buildMessage(validBody("contrato.creado"), "contrato.creado")));
  }

  private String validBody(String eventName) {
    return """
        {
          "event":"%s",
          "event_id":"00000000-0000-0000-0000-000000000001",
          "correlation_id":"00000000-0000-0000-0000-000000000002",
          "schema_version":1,
          "occurred_on":"2026-02-23T00:00:00Z",
          "payload":{"key":"value"}
        }
        """.formatted(eventName);
  }

  private Message buildMessage(String body, String routingKey) {
    MessageProperties props = new MessageProperties();
    props.setReceivedRoutingKey(routingKey);
    return new Message(body.getBytes(StandardCharsets.UTF_8), props);
  }
}
