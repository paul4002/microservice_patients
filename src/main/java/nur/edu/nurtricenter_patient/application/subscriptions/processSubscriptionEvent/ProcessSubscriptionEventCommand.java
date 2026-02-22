package nur.edu.nurtricenter_patient.application.subscriptions.processSubscriptionEvent;

import java.util.Map;

import an.awesome.pipelinr.Command;
import nur.edu.nurtricenter_patient.core.results.Result;

public record ProcessSubscriptionEventCommand(
  String eventName,
  Map<String, Object> payload,
  String routingKey
) implements Command<Result> {}

