package nur.edu.nurtricenter_patient.infraestructure.discovery;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ConsulServiceRegistrar implements DisposableBean {

  private static final Logger log = LoggerFactory.getLogger(ConsulServiceRegistrar.class);

  private final ServiceDiscoveryProperties props;
  private final HttpClient http;
  private final ObjectMapper mapper;
  private volatile boolean registered = false;

  public ConsulServiceRegistrar(ServiceDiscoveryProperties props, ObjectMapper mapper) {
    this.props = props;
    this.mapper = mapper;
    this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
  }

  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReady() {
    if (!props.isEnabled()) {
      log.info("Service discovery disabled, skipping Consul registration");
      return;
    }
    try {
      byte[] body = mapper.writeValueAsBytes(buildDefinition());
      HttpRequest req = baseRequest("/v1/agent/service/register")
          .PUT(HttpRequest.BodyPublishers.ofByteArray(body))
          .header("Content-Type", "application/json")
          .build();
      HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
      if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
        registered = true;
        log.info("Registered service '{}' with Consul at {}", props.getServiceId(), props.getConsul().baseUri());
      } else {
        log.warn("Consul registration returned HTTP {}: {}", resp.statusCode(), resp.body());
      }
    } catch (Exception e) {
      log.warn("Failed to register with Consul at {}: {}", props.getConsul().baseUri(), e.getMessage());
    }
  }

  @Override
  public void destroy() {
    if (!registered) {
      return;
    }
    try {
      HttpRequest req = baseRequest("/v1/agent/service/deregister/" + props.getServiceId()).PUT(HttpRequest.BodyPublishers.noBody()).build();
      http.send(req, HttpResponse.BodyHandlers.discarding());
      log.info("Deregistered service '{}' from Consul", props.getServiceId());
    } catch (Exception e) {
      log.warn("Failed to deregister from Consul: {}", e.getMessage());
    }
  }

  Map<String, Object> buildDefinition() {
    Map<String, Object> definition = new LinkedHashMap<>();
    definition.put("ID", props.getServiceId());
    definition.put("Name", props.getServiceName());
    definition.put("Address", props.getServiceAddress());
    definition.put("Port", props.getServicePort());
    definition.put("Tags", props.getServiceTags() == null ? List.of() : props.getServiceTags());

    Map<String, Object> check = new LinkedHashMap<>();
    check.put("HTTP", "http://" + props.getServiceAddress() + ":" + props.getServicePort() + props.getHealthPath());
    check.put("Method", "GET");
    check.put("Interval", props.getHealthInterval());
    check.put("Timeout", props.getHealthTimeout());
    check.put("DeregisterCriticalServiceAfter", props.getDeregisterCriticalAfter());
    definition.put("Check", check);

    return definition;
  }

  private HttpRequest.Builder baseRequest(String path) {
    HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(props.getConsul().baseUri() + path))
        .timeout(Duration.ofSeconds(3));
    if (props.getConsul().getToken() != null && !props.getConsul().getToken().isBlank()) {
      b.header("X-Consul-Token", props.getConsul().getToken());
    }
    return b;
  }
}
