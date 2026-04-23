package nur.edu.nurtricenter_patient.infraestructure.discovery;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ConsulProperties.class)
public class DiscoveryConfig {

	@Bean
	public ConsulRegistrationService consulRegistrationService(
		ConsulProperties properties,
		RestClient.Builder restClientBuilder
	) {
		return new ConsulRegistrationService(properties, restClientBuilder);
	}
}
