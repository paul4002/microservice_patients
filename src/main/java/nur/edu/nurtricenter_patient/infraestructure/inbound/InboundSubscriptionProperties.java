package nur.edu.nurtricenter_patient.infraestructure.inbound;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "inbound.rabbitmq")
public class InboundSubscriptionProperties {
  private boolean enabled = true;
  private String exchange = "outbox.events";
  private String exchangeType = "fanout";
  private boolean exchangeDurable = true;
  private String queue = "pacientes.inbound";
  private boolean queueDurable = true;
  private boolean queueExclusive = false;
  private boolean queueAutoDelete = false;
  private boolean declareTopology = false;
  private boolean failFastOnMissingQueue = false;
  private List<String> routingKeys = new ArrayList<>();
  private List<Integer> schemaVersions = List.of(1);

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getExchange() {
    return exchange;
  }

  public void setExchange(String exchange) {
    this.exchange = exchange;
  }

  public String getExchangeType() {
    return exchangeType;
  }

  public void setExchangeType(String exchangeType) {
    this.exchangeType = exchangeType;
  }

  public boolean isExchangeDurable() {
    return exchangeDurable;
  }

  public void setExchangeDurable(boolean exchangeDurable) {
    this.exchangeDurable = exchangeDurable;
  }

  public String getQueue() {
    return queue;
  }

  public void setQueue(String queue) {
    this.queue = queue;
  }

  public boolean isQueueDurable() {
    return queueDurable;
  }

  public void setQueueDurable(boolean queueDurable) {
    this.queueDurable = queueDurable;
  }

  public boolean isQueueExclusive() {
    return queueExclusive;
  }

  public void setQueueExclusive(boolean queueExclusive) {
    this.queueExclusive = queueExclusive;
  }

  public boolean isQueueAutoDelete() {
    return queueAutoDelete;
  }

  public void setQueueAutoDelete(boolean queueAutoDelete) {
    this.queueAutoDelete = queueAutoDelete;
  }

  public boolean isDeclareTopology() {
    return declareTopology;
  }

  public void setDeclareTopology(boolean declareTopology) {
    this.declareTopology = declareTopology;
  }

  public boolean isFailFastOnMissingQueue() {
    return failFastOnMissingQueue;
  }

  public void setFailFastOnMissingQueue(boolean failFastOnMissingQueue) {
    this.failFastOnMissingQueue = failFastOnMissingQueue;
  }

  public List<String> getRoutingKeys() {
    return routingKeys;
  }

  public void setRoutingKeys(List<String> routingKeys) {
    this.routingKeys = routingKeys;
  }

  public List<Integer> getSchemaVersions() {
    return schemaVersions;
  }

  public void setSchemaVersions(List<Integer> schemaVersions) {
    this.schemaVersions = schemaVersions;
  }
}
