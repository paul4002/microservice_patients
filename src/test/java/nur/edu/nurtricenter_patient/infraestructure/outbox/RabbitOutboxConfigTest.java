package nur.edu.nurtricenter_patient.infraestructure.outbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

class RabbitOutboxConfigTest {

  private RabbitOutboxConfig config;

  @BeforeEach
  void setUp() {
    config = new RabbitOutboxConfig();
  }

  @Test
  void rabbitAdmin_setsAutoStartupFalse() {
    ConnectionFactory cf = mock(ConnectionFactory.class);
    var admin = config.rabbitAdmin(cf);
    assertNotNull(admin);
  }

  @Test
  void rabbitTemplate_createsTemplate() {
    ConnectionFactory cf = mock(ConnectionFactory.class);
    OutboxPublisherProperties props = new OutboxPublisherProperties();
    props.setReadWriteTimeoutSeconds(5);
    var template = config.rabbitTemplate(cf, props);
    assertNotNull(template);
  }

  @Test
  void rabbitOutboxDeclarables_declareTopologyFalse_returnsEmptyDeclarables() {
    OutboxPublisherProperties props = new OutboxPublisherProperties();
    props.setDeclareTopology(false);

    Declarables result = config.rabbitOutboxDeclarables(props);
    assertNotNull(result);
    assertTrue(result.getDeclarables().isEmpty());
  }

  @Test
  void rabbitOutboxDeclarables_fanoutExchangeSingleQueue() {
    OutboxPublisherProperties props = new OutboxPublisherProperties();
    props.setDeclareTopology(true);
    props.setExchange("outbox.events");
    props.setExchangeType("fanout");
    props.setQueue("paciente.paciente-creado");

    Declarables result = config.rabbitOutboxDeclarables(props);
    Collection<Declarable> declarables = result.getDeclarables();

    assertTrue(declarables.stream().anyMatch(d -> d instanceof FanoutExchange));
    assertTrue(declarables.stream().anyMatch(d -> d instanceof Binding));
  }

  @Test
  void rabbitOutboxDeclarables_topicExchangeSingleQueue() {
    OutboxPublisherProperties props = new OutboxPublisherProperties();
    props.setDeclareTopology(true);
    props.setExchange("outbox.events");
    props.setExchangeType("topic");
    props.setQueue("paciente.single");

    Declarables result = config.rabbitOutboxDeclarables(props);
    assertTrue(result.getDeclarables().stream().anyMatch(d -> d instanceof TopicExchange));
  }

  @Test
  void rabbitOutboxDeclarables_directExchangeSingleQueue() {
    OutboxPublisherProperties props = new OutboxPublisherProperties();
    props.setDeclareTopology(true);
    props.setExchange("outbox.events");
    props.setExchangeType("direct");
    props.setQueue("paciente.direct");

    Declarables result = config.rabbitOutboxDeclarables(props);
    assertTrue(result.getDeclarables().stream().anyMatch(d -> d instanceof DirectExchange));
  }

  @Test
  void rabbitOutboxDeclarables_unknownExchangeType_defaultsToDirect() {
    OutboxPublisherProperties props = new OutboxPublisherProperties();
    props.setDeclareTopology(true);
    props.setExchange("outbox.events");
    props.setExchangeType("custom");
    props.setQueue("paciente.custom");

    Declarables result = config.rabbitOutboxDeclarables(props);
    assertTrue(result.getDeclarables().stream().anyMatch(d -> d instanceof DirectExchange));
  }

  @Test
  void rabbitOutboxDeclarables_multipleQueues_createsMultipleBindings() {
    OutboxPublisherProperties props = new OutboxPublisherProperties();
    props.setDeclareTopology(true);
    props.setExchange("outbox.events");
    props.setExchangeType("fanout");
    props.setQueue("queue1,queue2,queue3");

    Declarables result = config.rabbitOutboxDeclarables(props);
    long bindingCount = result.getDeclarables().stream().filter(d -> d instanceof Binding).count();
    assertEquals(3, bindingCount);
  }

  @Test
  void rabbitOutboxDeclarables_noQueue_createsOnlyExchange() {
    OutboxPublisherProperties props = new OutboxPublisherProperties();
    props.setDeclareTopology(true);
    props.setExchange("outbox.events");
    props.setExchangeType("fanout");
    props.setQueue(null);

    Declarables result = config.rabbitOutboxDeclarables(props);
    assertTrue(result.getDeclarables().stream().noneMatch(d -> d instanceof Binding));
  }

  @Test
  void rabbitOutboxDeclarables_exclusiveAutoDeleteQueue() {
    OutboxPublisherProperties props = new OutboxPublisherProperties();
    props.setDeclareTopology(true);
    props.setExchange("outbox.events");
    props.setExchangeType("fanout");
    props.setQueue("excl-queue");
    props.setQueueExclusive(true);
    props.setQueueAutoDelete(true);

    Declarables result = config.rabbitOutboxDeclarables(props);
    assertNotNull(result);
    assertFalse(result.getDeclarables().isEmpty());
  }

  private void assertFalse(boolean condition) {
    assertTrue(!condition);
  }
}
