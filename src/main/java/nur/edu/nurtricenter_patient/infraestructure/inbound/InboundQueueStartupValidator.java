package nur.edu.nurtricenter_patient.infraestructure.inbound;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.stereotype.Component;

import nur.edu.nurtricenter_patient.infraestructure.outbox.OutboxPublisherProperties;

@Component
public class InboundQueueStartupValidator {
  private static final Logger log = LoggerFactory.getLogger(InboundQueueStartupValidator.class);

  public InboundQueueStartupValidator(
    ConnectionFactory connectionFactory,
    InboundSubscriptionProperties inboundProps,
    OutboxPublisherProperties outboxProps
  ) {
    if (!inboundProps.isEnabled()) {
      log.info("Inbound RabbitMQ listener is disabled (inbound.rabbitmq.enabled=false)");
      return;
    }

    String queue = inboundProps.getQueue();
    if (queue == null || queue.isBlank()) {
      String message = "Inbound RabbitMQ queue is empty (inbound.rabbitmq.queue)";
      if (inboundProps.isFailFastOnMissingQueue()) {
        throw new IllegalStateException(message);
      }
      log.warn(message);
      return;
    }

    try (Connection connection = connectionFactory.createConnection();
         Channel channel = connection.createChannel(false)) {
      channel.queueDeclarePassive(queue);
      log.info("Inbound queue verified: queue={} vhost={} host={}:{}", queue, outboxProps.getVhost(), outboxProps.getHost(), outboxProps.getPort());
      return;
    } catch (Exception ex) {
      String message = String.format(
        "Unable to validate inbound queue '%s' in vhost '%s' at %s:%d (%s)",
        queue,
        outboxProps.getVhost(),
        outboxProps.getHost(),
        outboxProps.getPort(),
        ex.getMessage()
      );
      if (inboundProps.isFailFastOnMissingQueue()) {
        throw new IllegalStateException(message, ex);
      }
      log.warn(message);
    }
  }
}
