package nur.edu.nurtricenter_patient.infraestructure.inbound;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import nur.edu.nurtricenter_patient.application.abstractions.IInboundEventRecorder;

@Component
public class InboundEventRecorder implements IInboundEventRecorder {
  private static final int MAX_ERROR_LENGTH = 2000;
  private final InboundEventRepository repository;

  public InboundEventRecorder(InboundEventRepository repository) {
    this.repository = repository;
  }

  @Override
  public boolean tryStart(
    UUID eventId,
    String eventName,
    String routingKey,
    UUID correlationId,
    Integer schemaVersion,
    LocalDateTime occurredOn,
    String payload
  ) {
    if (eventId == null) {
      return false;
    }
    if (repository.findByEventId(eventId).isPresent()) {
      return false;
    }

    InboundEventEntity row = new InboundEventEntity();
    row.setId(UUID.randomUUID());
    row.setEventId(eventId);
    row.setEventName(eventName);
    row.setRoutingKey(routingKey);
    row.setCorrelationId(correlationId);
    row.setSchemaVersion(schemaVersion);
    row.setOccurredOn(occurredOn);
    row.setReceivedOn(LocalDateTime.now());
    row.setUpdatedOn(LocalDateTime.now());
    row.setStatus("RECEIVED");
    row.setPayload(payload);
    row.setLastError(null);

    try {
      repository.saveAndFlush(row);
      return true;
    } catch (DataIntegrityViolationException ex) {
      return false;
    }
  }

  @Override
  public void markProcessed(UUID eventId) {
    update(eventId, "PROCESSED", null);
  }

  @Override
  public void markFailed(UUID eventId, String error) {
    update(eventId, "FAILED", truncate(error));
  }

  private static String truncate(String value) {
    if (value == null) {
      return null;
    }
    return value.length() > MAX_ERROR_LENGTH ? value.substring(0, MAX_ERROR_LENGTH) : value;
  }

  private void update(UUID eventId, String status, String error) {
    Optional<InboundEventEntity> found = repository.findByEventId(eventId);
    if (found.isEmpty()) {
      return;
    }
    InboundEventEntity row = found.get();
    row.setStatus(status);
    row.setLastError(error);
    row.setProcessedOn("PROCESSED".equals(status) ? LocalDateTime.now() : null);
    row.setUpdatedOn(LocalDateTime.now());
    repository.save(row);
  }
}
