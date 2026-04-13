package nur.edu.nurtricenter_patient.application.abstractions;

import java.time.LocalDateTime;
import java.util.UUID;

public interface IInboundEventRecorder {
  boolean tryStart(
    UUID eventId,
    String eventName,
    String routingKey,
    UUID correlationId,
    Integer schemaVersion,
    LocalDateTime occurredOn,
    String payload
  );

  void markProcessed(UUID eventId);

  void markFailed(UUID eventId, String error);
}
