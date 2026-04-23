package nur.edu.nurtricenter_patient.infraestructure.discovery;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class ConsulRegistrationServiceTest {

	@Test
	void buildDefinition_producesConsulPayloadWithCheck() {
		ConsulProperties props = new ConsulProperties();
		props.setServiceId("nur-tricenter-patients");
		props.setServiceName("nur-tricenter-patients");
		props.setServiceAddress("154.38.180.80");
		props.setServicePort(8080);
		props.setHealthCheckPath("/actuator/health");
		props.setHealthCheckInterval("10s");
		props.setHealthCheckTimeout("3s");
		props.setDeregisterCriticalAfter("30s");
		props.setTags(new String[] { "spring-boot", "patients" });

		Map<String, Object> payload = ConsulRegistrationService.buildDefinition(props);

		assertThat(payload.get("ID")).isEqualTo("nur-tricenter-patients");
		assertThat(payload.get("Name")).isEqualTo("nur-tricenter-patients");
		assertThat(payload.get("Address")).isEqualTo("154.38.180.80");
		assertThat(payload.get("Port")).isEqualTo(8080);
		assertThat(payload.get("Tags")).isEqualTo(new String[] { "spring-boot", "patients" });

		@SuppressWarnings("unchecked")
		Map<String, Object> check = (Map<String, Object>) payload.get("Check");
		assertThat(check.get("HTTP")).isEqualTo("http://154.38.180.80:8080/actuator/health");
		assertThat(check.get("Method")).isEqualTo("GET");
		assertThat(check.get("Interval")).isEqualTo("10s");
		assertThat(check.get("Timeout")).isEqualTo("3s");
		assertThat(check.get("DeregisterCriticalServiceAfter")).isEqualTo("30s");
	}

	@Test
	void baseUri_concatenatesSchemeHostPort() {
		ConsulProperties props = new ConsulProperties();
		props.setScheme("http");
		props.setHost("154.38.180.80");
		props.setPort(8500);

		assertThat(props.baseUri()).isEqualTo("http://154.38.180.80:8500");
	}
}
