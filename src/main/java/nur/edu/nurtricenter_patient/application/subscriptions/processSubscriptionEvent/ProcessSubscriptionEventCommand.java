package nur.edu.nurtricenter_patient.application.subscriptions.processSubscriptionEvent;

import java.util.Map;
import java.util.UUID;

import an.awesome.pipelinr.Command;
import nur.edu.nurtricenter_patient.core.results.Result;

public record ProcessSubscriptionEventCommand(
  String eventName,
  Map<String, Object> payload,
  String routingKey,
  UUID eventId,
  UUID correlationId,
  Integer schemaVersion,
  String occurredOn,
  String rawMessage
) implements Command<Result> {}
