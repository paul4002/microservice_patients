package nur.edu.nurtricenter_patient.infraestructure.inbound;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.stereotype.Component;

@Component("inboundConsumerToggle")
public class InboundConsumerToggle {
  private static final Logger log = LoggerFactory.getLogger(InboundConsumerToggle.class);

  private final ConnectionFactory connectionFactory;
  private final InboundSubscriptionProperties props;

  public InboundConsumerToggle(ConnectionFactory connectionFactory, InboundSubscriptionProperties props) {
    this.connectionFactory = connectionFactory;
    this.props = props;
  }

  public boolean isEnabled() {
    if (!props.isEnabled()) {
      return false;
    }
    String queue = props.getQueue();
    if (queue == null || queue.isBlank()) {
      log.warn("Inbound consumer disabled: inbound.rabbitmq.queue is empty.");
      return false;
    }
    try (Connection connection = connectionFactory.createConnection();
         Channel channel = connection.createChannel(false)) {
      channel.queueDeclarePassive(queue);
      return true;
    } catch (Exception ex) {
      log.warn("Inbound consumer disabled: queue '{}' not available yet ({}).", queue, ex.getMessage());
      return false;
    }
  }
}
