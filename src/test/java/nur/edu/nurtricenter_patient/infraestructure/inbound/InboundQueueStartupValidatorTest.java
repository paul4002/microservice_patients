package nur.edu.nurtricenter_patient.infraestructure.inbound;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import nur.edu.nurtricenter_patient.infraestructure.outbox.OutboxPublisherProperties;

class InboundQueueStartupValidatorTest {

  private OutboxPublisherProperties outboxProps() {
    OutboxPublisherProperties p = new OutboxPublisherProperties();
    p.setHost("localhost");
    p.setPort(5672);
    p.setVhost("/");
    return p;
  }

  @Test
  void constructor_disabled_doesNotThrow() {
    InboundSubscriptionProperties inboundProps = new InboundSubscriptionProperties();
    inboundProps.setEnabled(false);
    ConnectionFactory cf = mock(ConnectionFactory.class);

    assertDoesNotThrow(() -> new InboundQueueStartupValidator(cf, inboundProps, outboxProps()));
  }

  @Test
  void constructor_enabledWithBlankQueue_logsWarning() {
    InboundSubscriptionProperties inboundProps = new InboundSubscriptionProperties();
    inboundProps.setEnabled(true);
    inboundProps.setQueue("");
    ConnectionFactory cf = mock(ConnectionFactory.class);

    assertDoesNotThrow(() -> new InboundQueueStartupValidator(cf, inboundProps, outboxProps()));
  }

  @Test
  void constructor_enabledWithBlankQueueFailFast_throws() {
    InboundSubscriptionProperties inboundProps = new InboundSubscriptionProperties();
    inboundProps.setEnabled(true);
    inboundProps.setQueue("");
    inboundProps.setFailFastOnMissingQueue(true);
    ConnectionFactory cf = mock(ConnectionFactory.class);

    assertThrows(IllegalStateException.class,
        () -> new InboundQueueStartupValidator(cf, inboundProps, outboxProps()));
  }

  @Test
  void constructor_enabledQueueNotAvailable_logsWarning() throws Exception {
    InboundSubscriptionProperties inboundProps = new InboundSubscriptionProperties();
    inboundProps.setEnabled(true);
    inboundProps.setQueue("my-queue");

    ConnectionFactory cf = mock(ConnectionFactory.class);
    Connection conn = mock(Connection.class);
    Channel channel = mock(Channel.class);
    when(cf.createConnection()).thenReturn(conn);
    when(conn.createChannel(false)).thenReturn(channel);
    when(channel.queueDeclarePassive("my-queue")).thenThrow(new RuntimeException("not found"));

    assertDoesNotThrow(() -> new InboundQueueStartupValidator(cf, inboundProps, outboxProps()));
  }

  @Test
  void constructor_enabledQueueNotAvailableFailFast_throws() throws Exception {
    InboundSubscriptionProperties inboundProps = new InboundSubscriptionProperties();
    inboundProps.setEnabled(true);
    inboundProps.setQueue("my-queue");
    inboundProps.setFailFastOnMissingQueue(true);

    ConnectionFactory cf = mock(ConnectionFactory.class);
    Connection conn = mock(Connection.class);
    Channel channel = mock(Channel.class);
    when(cf.createConnection()).thenReturn(conn);
    when(conn.createChannel(false)).thenReturn(channel);
    when(channel.queueDeclarePassive("my-queue")).thenThrow(new RuntimeException("not found"));

    assertThrows(IllegalStateException.class,
        () -> new InboundQueueStartupValidator(cf, inboundProps, outboxProps()));
  }

  @Test
  void constructor_queueVerifiedSuccessfully_doesNotThrow() throws Exception {
    InboundSubscriptionProperties inboundProps = new InboundSubscriptionProperties();
    inboundProps.setEnabled(true);
    inboundProps.setQueue("my-queue");

    ConnectionFactory cf = mock(ConnectionFactory.class);
    Connection conn = mock(Connection.class);
    Channel channel = mock(Channel.class);
    when(cf.createConnection()).thenReturn(conn);
    when(conn.createChannel(false)).thenReturn(channel);

    assertDoesNotThrow(() -> new InboundQueueStartupValidator(cf, inboundProps, outboxProps()));
  }
}
