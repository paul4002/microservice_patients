package nur.edu.nurtricenter_patient.infraestructure.discovery;

import java.util.Collections;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "service-discovery")
public class ServiceDiscoveryProperties {

  private boolean enabled = true;
  private String serviceName = "nurtricenter-patient";
  private String serviceId = "nurtricenter-patient";
  private String serviceAddress = "nurtricenter-patient";
  private int servicePort = 8080;
  private List<String> serviceTags = Collections.emptyList();
  private String healthPath = "/actuator/health";
  private String healthInterval = "15s";
  private String healthTimeout = "3s";
  private String deregisterCriticalAfter = "1m";
  private Consul consul = new Consul();

  public boolean isEnabled() { return enabled; }
  public void setEnabled(boolean enabled) { this.enabled = enabled; }

  public String getServiceName() { return serviceName; }
  public void setServiceName(String serviceName) { this.serviceName = serviceName; }

  public String getServiceId() { return serviceId; }
  public void setServiceId(String serviceId) { this.serviceId = serviceId; }

  public String getServiceAddress() { return serviceAddress; }
  public void setServiceAddress(String serviceAddress) { this.serviceAddress = serviceAddress; }

  public int getServicePort() { return servicePort; }
  public void setServicePort(int servicePort) { this.servicePort = servicePort; }

  public List<String> getServiceTags() { return serviceTags; }
  public void setServiceTags(List<String> serviceTags) { this.serviceTags = serviceTags; }

  public String getHealthPath() { return healthPath; }
  public void setHealthPath(String healthPath) { this.healthPath = healthPath; }

  public String getHealthInterval() { return healthInterval; }
  public void setHealthInterval(String healthInterval) { this.healthInterval = healthInterval; }

  public String getHealthTimeout() { return healthTimeout; }
  public void setHealthTimeout(String healthTimeout) { this.healthTimeout = healthTimeout; }

  public String getDeregisterCriticalAfter() { return deregisterCriticalAfter; }
  public void setDeregisterCriticalAfter(String deregisterCriticalAfter) {
    this.deregisterCriticalAfter = deregisterCriticalAfter;
  }

  public Consul getConsul() { return consul; }
  public void setConsul(Consul consul) { this.consul = consul; }

  public static class Consul {
    private String host = "consul";
    private int port = 8500;
    private String scheme = "http";
    private String token = "";

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getScheme() { return scheme; }
    public void setScheme(String scheme) { this.scheme = scheme; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String baseUri() {
      return scheme + "://" + host + ":" + port;
    }
  }
}
