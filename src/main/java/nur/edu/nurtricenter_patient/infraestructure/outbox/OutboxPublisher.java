package nur.edu.nurtricenter_patient.infraestructure.outbox;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class OutboxPublisher {
  private final OutboxEventRepository repository;
  private final RabbitTemplate rabbitTemplate;
  private final OutboxPublisherProperties props;

  public OutboxPublisher(OutboxEventRepository repository, RabbitTemplate rabbitTemplate, OutboxPublisherProperties props) {
    this.repository = repository;
    this.rabbitTemplate = rabbitTemplate;
    this.props = props;
  }

  @Scheduled(fixedDelayString = "${rabbitmq.outbox-poll-interval-ms:1000}")
  public void publishPending() {
    List<OutboxEventEntity> events = repository.findByProcessedOnIsNullAndNextAttemptAtLessThanEqualOrderByOccurredOnAsc(
      LocalDateTime.now(),
      PageRequest.of(0, props.getOutboxBatchSize())
    );
    for (OutboxEventEntity event : events) {
      publishSingle(event);
    }
  }

  @Transactional
  protected void publishSingle(OutboxEventEntity event) {
    try {
      String routingKey = event.getEventName() != null ? event.getEventName() : (props.getRoutingKey() != null ? props.getRoutingKey() : "");
      rabbitTemplate.convertAndSend(props.getExchange(), routingKey, event.getPayload());
      event.setProcessedOn(LocalDateTime.now());
      event.setLastError(null);
      repository.save(event);
    } catch (Exception ex) {
      int attempts = event.getAttempts() != null ? event.getAttempts() + 1 : 1;
      event.setAttempts(attempts);
      event.setLastError(ex.getMessage());
      long baseBackoffMs = props.getPublishBackoffMs();
      int cap = props.getPublishRetries() > 0 ? props.getPublishRetries() : 10;
      long delayMs = baseBackoffMs * Math.min(attempts, cap);
      event.setNextAttemptAt(LocalDateTime.now().plusNanos(delayMs * 1_000_000));
      repository.save(event);
    }
  }
}
