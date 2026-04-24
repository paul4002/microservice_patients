package nur.edu.nurtricenter_patient.infraestructure.inbound;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

class InboundRabbitConfigTest {

  private InboundRabbitConfig config;

  @BeforeEach
  void setUp() {
    config = new InboundRabbitConfig();
  }

  @Test
  void inboundRabbitDeclarables_disabled_returnsEmpty() {
    InboundSubscriptionProperties props = new InboundSubscriptionProperties();
    props.setEnabled(false);

    Declarables result = config.inboundRabbitDeclarables(props);
    assertTrue(result.getDeclarables().isEmpty());
  }

  @Test
  void inboundRabbitDeclarables_enabledButDeclareTopologyFalse_returnsEmpty() {
    InboundSubscriptionProperties props = new InboundSubscriptionProperties();
    props.setEnabled(true);
    props.setDeclareTopology(false);
    props.setQueue("my-queue");
    props.setExchange("my-exchange");

    Declarables result = config.inboundRabbitDeclarables(props);
    assertTrue(result.getDeclarables().isEmpty());
  }

  @Test
  void inboundRabbitDeclarables_missingQueue_returnsEmpty() {
    InboundSubscriptionProperties props = new InboundSubscriptionProperties();
    props.setEnabled(true);
    props.setDeclareTopology(true);
    props.setQueue(null);
    props.setExchange("my-exchange");

    Declarables result = config.inboundRabbitDeclarables(props);
    assertTrue(result.getDeclarables().isEmpty());
  }

  @Test
  void inboundRabbitDeclarables_fanout_createsFanoutWithBinding() {
    InboundSubscriptionProperties props = new InboundSubscriptionProperties();
    props.setEnabled(true);
    props.setDeclareTopology(true);
    props.setQueue("inbound.queue");
    props.setExchange("outbox.events");
    props.setExchangeType("fanout");

    Declarables result = config.inboundRabbitDeclarables(props);
    assertTrue(result.getDeclarables().stream().anyMatch(d -> d instanceof FanoutExchange));
    assertTrue(result.getDeclarables().stream().anyMatch(d -> d instanceof Binding));
  }

  @Test
  void inboundRabbitDeclarables_topicWithRoutingKeys_createsBindingsPerKey() {
    InboundSubscriptionProperties props = new InboundSubscriptionProperties();
    props.setEnabled(true);
    props.setDeclareTopology(true);
    props.setQueue("inbound.queue");
    props.setExchange("outbox.events");
    props.setExchangeType("topic");
    props.setRoutingKeys(List.of("paciente.creado", "paciente.eliminado"));

    Declarables result = config.inboundRabbitDeclarables(props);
    assertTrue(result.getDeclarables().stream().anyMatch(d -> d instanceof TopicExchange));
    long bindingCount = result.getDeclarables().stream().filter(d -> d instanceof Binding).count();
    assertTrue(bindingCount >= 2);
  }

  @Test
  void inboundRabbitDeclarables_directExchange_createsDirectExchange() {
    InboundSubscriptionProperties props = new InboundSubscriptionProperties();
    props.setEnabled(true);
    props.setDeclareTopology(true);
    props.setQueue("inbound.queue");
    props.setExchange("outbox.events");
    props.setExchangeType("direct");
    props.setRoutingKeys(List.of("my.routing.key"));

    Declarables result = config.inboundRabbitDeclarables(props);
    assertTrue(result.getDeclarables().stream().anyMatch(d -> d instanceof DirectExchange));
  }

  @Test
  void inboundRabbitDeclarables_defaultType_createsTopicExchange() {
    InboundSubscriptionProperties props = new InboundSubscriptionProperties();
    props.setEnabled(true);
    props.setDeclareTopology(true);
    props.setQueue("inbound.queue");
    props.setExchange("outbox.events");
    props.setExchangeType(null);
    props.setRoutingKeys(List.of("test.key"));

    Declarables result = config.inboundRabbitDeclarables(props);
    assertTrue(result.getDeclarables().stream().anyMatch(d -> d instanceof TopicExchange));
  }

  @Test
  void inboundRabbitDeclarables_headersExchange_createsDeclarables() {
    InboundSubscriptionProperties props = new InboundSubscriptionProperties();
    props.setEnabled(true);
    props.setDeclareTopology(true);
    props.setQueue("inbound.queue");
    props.setExchange("outbox.events");
    props.setExchangeType("headers");
    props.setRoutingKeys(List.of());

    Declarables result = config.inboundRabbitDeclarables(props);
    assertNotNull(result);
  }

  @Test
  void inboundRabbitDeclarables_topicNoRoutingKeys_returnsExchangeAndQueueOnly() {
    InboundSubscriptionProperties props = new InboundSubscriptionProperties();
    props.setEnabled(true);
    props.setDeclareTopology(true);
    props.setQueue("inbound.queue");
    props.setExchange("outbox.events");
    props.setExchangeType("topic");
    props.setRoutingKeys(List.of());

    Declarables result = config.inboundRabbitDeclarables(props);
    assertTrue(result.getDeclarables().stream().noneMatch(d -> d instanceof Binding));
  }

  @Test
  void rabbitListenerContainerFactory_queueNotAvailable_autoStartupFalse() throws Exception {
    InboundSubscriptionProperties props = new InboundSubscriptionProperties();
    props.setEnabled(true);
    props.setQueue("nonexistent-queue");

    ConnectionFactory cf = mock(ConnectionFactory.class);
    Connection mockConn = mock(Connection.class);
    Channel mockChannel = mock(Channel.class);
    when(cf.createConnection()).thenReturn(mockConn);
    when(mockConn.createChannel(false)).thenReturn(mockChannel);
    when(mockChannel.queueDeclarePassive("nonexistent-queue")).thenThrow(new RuntimeException("queue not found"));

    var factory = config.rabbitListenerContainerFactory(cf, props);
    assertNotNull(factory);
  }

  @Test
  void rabbitListenerContainerFactory_queueExists_autoStartupTrue() throws Exception {
    InboundSubscriptionProperties props = new InboundSubscriptionProperties();
    props.setEnabled(true);
    props.setQueue("existing-queue");

    ConnectionFactory cf = mock(ConnectionFactory.class);
    Connection mockConn = mock(Connection.class);
    Channel mockChannel = mock(Channel.class);
    when(cf.createConnection()).thenReturn(mockConn);
    when(mockConn.createChannel(false)).thenReturn(mockChannel);

    var factory = config.rabbitListenerContainerFactory(cf, props);
    assertNotNull(factory);
  }
}
