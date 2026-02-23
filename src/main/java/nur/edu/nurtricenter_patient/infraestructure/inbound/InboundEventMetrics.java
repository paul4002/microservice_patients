package nur.edu.nurtricenter_patient.infraestructure.inbound;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Sample;
import org.springframework.stereotype.Component;

@Component
public class InboundEventMetrics {

  private final MeterRegistry meterRegistry;

  public InboundEventMetrics(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  public void incrementReceived(String eventName) {
    counter("patients.inbound.events.received", eventName).increment();
  }

  public void incrementProcessed(String eventName) {
    counter("patients.inbound.events.processed", eventName).increment();
  }

  public void incrementFailed(String eventName) {
    counter("patients.inbound.events.failed", eventName).increment();
  }

  public void incrementDuplicate(String eventName) {
    counter("patients.inbound.events.duplicated", eventName).increment();
  }

  public Sample startHandlerLatency() {
    return Timer.start(meterRegistry);
  }

  public void stopHandlerLatency(Sample sample, String eventName) {
    if (sample == null) {
      return;
    }
    sample.stop(
      Timer.builder("patients.inbound.handler.latency")
        .description("Inbound handler execution latency")
        .tag("event", normalizeEvent(eventName))
        .register(meterRegistry)
    );
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

