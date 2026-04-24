package nur.edu.nurtricenter_patient.infraestructure.inbound;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

class InboundConsumerToggleTest {

  @Test
  void isEnabled_whenDisabledInProps_returnsFalse() {
    InboundSubscriptionProperties props = new InboundSubscriptionProperties();
    props.setEnabled(false);
    ConnectionFactory cf = mock(ConnectionFactory.class);

    InboundConsumerToggle toggle = new InboundConsumerToggle(cf, props);
    assertFalse(toggle.isEnabled());
  }

  @Test
  void isEnabled_enabledButEmptyQueue_returnsFalse() {
    InboundSubscriptionProperties props = new InboundSubscriptionProperties();
    props.setEnabled(true);
    props.setQueue("");
    ConnectionFactory cf = mock(ConnectionFactory.class);

    InboundConsumerToggle toggle = new InboundConsumerToggle(cf, props);
    assertFalse(toggle.isEnabled());
  }

  @Test
  void isEnabled_enabledQueueNotAvailable_returnsFalse() throws Exception {
    InboundSubscriptionProperties props = new InboundSubscriptionProperties();
    props.setEnabled(true);
    props.setQueue("my-queue");

    ConnectionFactory cf = mock(ConnectionFactory.class);
    Connection conn = mock(Connection.class);
    Channel channel = mock(Channel.class);
    when(cf.createConnection()).thenReturn(conn);
    when(conn.createChannel(false)).thenReturn(channel);
    when(channel.queueDeclarePassive("my-queue")).thenThrow(new RuntimeException("not found"));

    InboundConsumerToggle toggle = new InboundConsumerToggle(cf, props);
    assertFalse(toggle.isEnabled());
  }

  @Test
  void isEnabled_enabledQueueExists_returnsTrue() throws Exception {
    InboundSubscriptionProperties props = new InboundSubscriptionProperties();
    props.setEnabled(true);
    props.setQueue("my-queue");

    ConnectionFactory cf = mock(ConnectionFactory.class);
    Connection conn = mock(Connection.class);
    Channel channel = mock(Channel.class);
    when(cf.createConnection()).thenReturn(conn);
    when(conn.createChannel(false)).thenReturn(channel);

    InboundConsumerToggle toggle = new InboundConsumerToggle(cf, props);
    assertTrue(toggle.isEnabled());
  }
}
