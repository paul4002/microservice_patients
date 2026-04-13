package nur.edu.nurtricenter_patient.application.abstractions;

import java.util.function.Supplier;

public interface IInboundEventMetrics {
  void incrementProcessed(String eventName);

  void incrementFailed(String eventName);

  void incrementDuplicate(String eventName);

  <T> T timeHandler(String eventName, Supplier<T> action);
}
