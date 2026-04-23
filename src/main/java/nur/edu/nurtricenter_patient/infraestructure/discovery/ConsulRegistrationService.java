package nur.edu.nurtricenter_patient.infraestructure.discovery;

import jakarta.annotation.PreDestroy;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

public class ConsulRegistrationService {

	private static final Logger log = LoggerFactory.getLogger(ConsulRegistrationService.class);

	private final ConsulProperties properties;
	private final RestClient restClient;

	public ConsulRegistrationService(ConsulProperties properties, RestClient.Builder builder) {
		this.properties = properties;
		this.restClient = builder.baseUrl(properties.baseUri()).build();
	}

	@EventListener(ApplicationReadyEvent.class)
	public void register() {
		if (!properties.isEnabled()) {
			log.info("Consul registration disabled (consul.enabled=false).");
			return;
		}
		Map<String, Object> definition = buildDefinition(properties);
		try {
			HttpStatusCode status = restClient
				.put()
				.uri("/v1/agent/service/register")
				.body(definition)
				.retrieve()
				.toBodilessEntity()
				.getStatusCode();
			if (status.is2xxSuccessful()) {
				log.info("Registered service {} with Consul at {}", properties.getServiceId(), properties.baseUri());
			} else {
				log.warn("Consul registration non-success: status={}", status);
			}
		} catch (Exception ex) {
			log.warn("Consul registration failed (continuing without registration): {}", ex.getMessage());
		}
	}

	@PreDestroy
	public void deregister() {
		if (!properties.isEnabled()) return;
		try {
			restClient
				.put()
				.uri("/v1/agent/service/deregister/{id}", properties.getServiceId())
				.retrieve()
				.toBodilessEntity();
			log.info("Deregistered service {} from Consul.", properties.getServiceId());
		} catch (Exception ex) {
			log.warn("Consul deregistration failed: {}", ex.getMessage());
		}
	}

	public static Map<String, Object> buildDefinition(ConsulProperties props) {
		Map<String, Object> check = new LinkedHashMap<>();
		check.put("HTTP", "http://" + props.getServiceAddress() + ":" + props.getServicePort() + props.getHealthCheckPath());
		check.put("Method", "GET");
		check.put("Interval", props.getHealthCheckInterval());
		check.put("Timeout", props.getHealthCheckTimeout());
		check.put("DeregisterCriticalServiceAfter", props.getDeregisterCriticalAfter());

		Map<String, Object> definition = new LinkedHashMap<>();
		definition.put("ID", props.getServiceId());
		definition.put("Name", props.getServiceName());
		definition.put("Address", props.getServiceAddress());
		definition.put("Port", props.getServicePort());
		definition.put("Tags", props.getTags());
		definition.put("Check", check);
		return definition;
	}
}
