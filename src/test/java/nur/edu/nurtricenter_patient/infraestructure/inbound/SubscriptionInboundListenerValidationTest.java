package nur.edu.nurtricenter_patient.infraestructure.inbound;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import com.fasterxml.jackson.databind.ObjectMapper;

import an.awesome.pipelinr.Pipeline;

class SubscriptionInboundListenerValidationTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void shouldRejectWhenEventIdIsInvalidUuid() {
    Pipeline pipeline = org.mockito.Mockito.mock(Pipeline.class);
    InboundSubscriptionProperties props = new InboundSubscriptionProperties();
    props.setSchemaVersions(List.of(1));
    SubscriptionInboundListener listener = new SubscriptionInboundListener(
      pipeline,
      objectMapper,
      props,
      new InboundEventMetrics(new SimpleMeterRegistry())
    );

    Message message = buildMessage("""
      {
        "event":"suscripciones.suscripcion-actualizada",
        "event_id":"not-a-uuid",
        "correlation_id":"00000000-0000-0000-0000-000000000111",
        "schema_version":1,
        "occurred_on":"2026-02-23T00:00:00Z",
        "payload":{"suscripcionId":"3fa85f64-5717-4562-b3fc-2c963f66afa6","nombre":"Plan 15 dias"}
      }
      """, "suscripciones.suscripcion-actualizada");

    assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.onMessage(message));
    verifyNoInteractions(pipeline);
  }

  @Test
  void shouldRejectWhenSchemaVersionIsUnsupported() {
    Pipeline pipeline = org.mockito.Mockito.mock(Pipeline.class);
    InboundSubscriptionProperties props = new InboundSubscriptionProperties();
    props.setSchemaVersions(List.of(1));
    SubscriptionInboundListener listener = new SubscriptionInboundListener(
      pipeline,
      objectMapper,
      props,
      new InboundEventMetrics(new SimpleMeterRegistry())
    );

    Message message = buildMessage("""
      {
        "event":"contrato.cancelar",
        "event_id":"00000000-0000-0000-0000-000000000777",
        "correlation_id":"00000000-0000-0000-0000-000000000111",
        "schema_version":99,
        "occurred_on":"2026-02-23T00:00:00Z",
        "payload":{"contratoId":"3fa85f64-5717-4562-b3fc-2c963f66afa6","motivoCancelacion":"prueba"}
      }
      """, "contrato.cancelar");

    assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.onMessage(message));
    verifyNoInteractions(pipeline);
  }

  @Test
  void shouldRejectWhenEventIdIsMissing() {
    Pipeline pipeline = org.mockito.Mockito.mock(Pipeline.class);
    InboundSubscriptionProperties props = new InboundSubscriptionProperties();
    props.setSchemaVersions(List.of(1));
    SubscriptionInboundListener listener = new SubscriptionInboundListener(
      pipeline,
      objectMapper,
      props,
      new InboundEventMetrics(new SimpleMeterRegistry())
    );

    Message message = buildMessage("""
      {
        "event":"contrato.creado",
        "correlation_id":"00000000-0000-0000-0000-000000000111",
        "schema_version":1,
        "occurred_on":"2026-02-23T00:00:00Z",
        "payload":{"contratoId":"3fa85f64-5717-4562-b3fc-2c963f66afa6","pacienteId":"f2d06f1a-f6f6-495e-a8a5-b0153f0d13eb","fechaFin":"2026-03-10"}
      }
      """, "contrato.creado");

    assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.onMessage(message));
    verifyNoInteractions(pipeline);
  }

  @Test
  void shouldRejectWhenPayloadObjectIsMissing() {
    Pipeline pipeline = org.mockito.Mockito.mock(Pipeline.class);
    InboundSubscriptionProperties props = new InboundSubscriptionProperties();
    props.setSchemaVersions(List.of(1));
    SubscriptionInboundListener listener = new SubscriptionInboundListener(
      pipeline,
      objectMapper,
      props,
      new InboundEventMetrics(new SimpleMeterRegistry())
    );

    Message message = buildMessage("""
      {
        "event":"contrato.creado",
        "event_id":"00000000-0000-0000-0000-000000000777",
        "correlation_id":"00000000-0000-0000-0000-000000000111",
        "schema_version":1,
        "occurred_on":"2026-02-23T00:00:00Z"
      }
      """, "contrato.creado");

    assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.onMessage(message));
    verifyNoInteractions(pipeline);
  }

  private Message buildMessage(String body, String routingKey) {
    MessageProperties props = new MessageProperties();
    props.setReceivedRoutingKey(routingKey);
    return new Message(body.getBytes(StandardCharsets.UTF_8), props);
  }
}
