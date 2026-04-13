package nur.edu.nurtricenter_patient.infraestructure.inbound;

import java.util.function.Supplier;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Sample;
import org.springframework.stereotype.Component;

import nur.edu.nurtricenter_patient.application.abstractions.IInboundEventMetrics;

@Component
public class InboundEventMetrics implements IInboundEventMetrics {

  private final MeterRegistry meterRegistry;

  public InboundEventMetrics(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  public void incrementReceived(String eventName) {
    counter("patients.inbound.events.received", eventName).increment();
  }

  @Override
  public void incrementProcessed(String eventName) {
    counter("patients.inbound.events.processed", eventName).increment();
  }

  @Override
  public void incrementFailed(String eventName) {
    counter("patients.inbound.events.failed", eventName).increment();
  }

  @Override
  public void incrementDuplicate(String eventName) {
    counter("patients.inbound.events.duplicated", eventName).increment();
  }

  @Override
  public <T> T timeHandler(String eventName, Supplier<T> action) {
    Sample sample = Timer.start(meterRegistry);
    try {
      return action.get();
    } finally {
      sample.stop(
        Timer.builder("patients.inbound.handler.latency")
          .description("Inbound handler execution latency")
          .tag("event", normalizeEvent(eventName))
          .register(meterRegistry)
      );
    }
  }

  private Counter counter(String name, String eventName) {
    return Counter.builder(name)
      .tag("event", normalizeEvent(eventName))
      .register(meterRegistry);
  }

  private String normalizeEvent(String eventName) {
    return eventName == null || eventName.isBlank() ? "unknown" : eventName;
  }
}

