package nur.edu.nurtricenter_patient.infraestructure.discovery;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "consul")
public class ConsulProperties {

	private boolean enabled = true;
	private String host = "207.180.197.169";
	private int port = 8500;
	private String scheme = "http";
	private String serviceId = "nur-tricenter-patients";
	private String serviceName = "nur-tricenter-patients";
	private String serviceAddress = "207.180.197.169";
	private int servicePort = 8080;
	private String healthCheckPath = "/actuator/health";
	private String healthCheckInterval = "15s";
	private String healthCheckTimeout = "5s";
	private String deregisterCriticalAfter = "1m";
	private String[] tags = new String[] { "spring-boot", "patients" };

	public String baseUri() {
		return scheme + "://" + host + ":" + port;
	}

	public boolean isEnabled() { return enabled; }
	public void setEnabled(boolean enabled) { this.enabled = enabled; }
	public String getHost() { return host; }
	public void setHost(String host) { this.host = host; }
	public int getPort() { return port; }
	public void setPort(int port) { this.port = port; }
	public String getScheme() { return scheme; }
	public void setScheme(String scheme) { this.scheme = scheme; }
	public String getServiceId() { return serviceId; }
	public void setServiceId(String serviceId) { this.serviceId = serviceId; }
	public String getServiceName() { return serviceName; }
	public void setServiceName(String serviceName) { this.serviceName = serviceName; }
	public String getServiceAddress() { return serviceAddress; }
	public void setServiceAddress(String serviceAddress) { this.serviceAddress = serviceAddress; }
	public int getServicePort() { return servicePort; }
	public void setServicePort(int servicePort) { this.servicePort = servicePort; }
	public String getHealthCheckPath() { return healthCheckPath; }
	public void setHealthCheckPath(String healthCheckPath) { this.healthCheckPath = healthCheckPath; }
	public String getHealthCheckInterval() { return healthCheckInterval; }
	public void setHealthCheckInterval(String healthCheckInterval) { this.healthCheckInterval = healthCheckInterval; }
	public String getHealthCheckTimeout() { return healthCheckTimeout; }
	public void setHealthCheckTimeout(String healthCheckTimeout) { this.healthCheckTimeout = healthCheckTimeout; }
	public String getDeregisterCriticalAfter() { return deregisterCriticalAfter; }
	public void setDeregisterCriticalAfter(String deregisterCriticalAfter) { this.deregisterCriticalAfter = deregisterCriticalAfter; }
	public String[] getTags() { return tags; }
	public void setTags(String[] tags) { this.tags = tags; }
}
