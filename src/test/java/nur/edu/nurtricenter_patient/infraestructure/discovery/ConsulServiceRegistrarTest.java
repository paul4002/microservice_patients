package nur.edu.nurtricenter_patient.infraestructure.discovery;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class ConsulServiceRegistrarTest {

  @Test
  void buildDefinitionProducesConsulPayload() {
    ServiceDiscoveryProperties props = new ServiceDiscoveryProperties();
    props.setServiceName("nurtricenter-patient");
    props.setServiceId("nurtricenter-patient-1");
    props.setServiceAddress("patients.internal");
    props.setServicePort(8080);
    props.setServiceTags(List.of("spring-boot", "patients"));
    props.setHealthPath("/actuator/health");
    props.setHealthInterval("10s");
    props.setHealthTimeout("2s");
    props.setDeregisterCriticalAfter("5m");

    ConsulServiceRegistrar registrar = new ConsulServiceRegistrar(props, new ObjectMapper());

    @SuppressWarnings("unchecked")
    Map<String, Object> def = registrar.buildDefinition();

    assertThat(def).containsEntry("ID", "nurtricenter-patient-1");
    assertThat(def).containsEntry("Name", "nurtricenter-patient");
    assertThat(def).containsEntry("Address", "patients.internal");
    assertThat(def).containsEntry("Port", 8080);
    assertThat(def.get("Tags")).isEqualTo(List.of("spring-boot", "patients"));

    @SuppressWarnings("unchecked")
    Map<String, Object> check = (Map<String, Object>) def.get("Check");
    assertThat(check)
        .containsEntry("HTTP", "http://patients.internal:8080/actuator/health")
        .containsEntry("Method", "GET")
        .containsEntry("Interval", "10s")
        .containsEntry("Timeout", "2s")
        .containsEntry("DeregisterCriticalServiceAfter", "5m");
  }

  @Test
  void onApplicationReadyDoesNotCallConsulWhenDisabled() {
    ServiceDiscoveryProperties props = new ServiceDiscoveryProperties();
    props.setEnabled(false);
    props.getConsul().setHost("unreachable.invalid");
    ConsulServiceRegistrar registrar = new ConsulServiceRegistrar(props, new ObjectMapper());

    registrar.onApplicationReady();
    registrar.destroy();

    assertThat(props.isEnabled()).isFalse();
  }

  @Test
  void consulBaseUriComposition() {
    ServiceDiscoveryProperties.Consul consul = new ServiceDiscoveryProperties.Consul();
    consul.setHost("consul.local");
    consul.setPort(18500);
    consul.setScheme("https");

    assertThat(consul.baseUri()).isEqualTo("https://consul.local:18500");
  }
}
