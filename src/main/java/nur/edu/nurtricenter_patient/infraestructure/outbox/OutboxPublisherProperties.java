package nur.edu.nurtricenter_patient.infraestructure.outbox;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rabbitmq")
public class OutboxPublisherProperties {
  private String host;
  private int port = 5672;
  private String username;
  private String password;
  private String vhost = "/";
  private String exchange;
  private String exchangeType = "fanout";
  private boolean exchangeDurable = true;
  private String routingKey;
  private String queue;
  private boolean queueDurable = true;
  private boolean queueExclusive = false;
  private boolean queueAutoDelete = false;
  private String bindingKey = "";
  private boolean declareTopology = false;
  private int publishRetries = 3;
  private long publishBackoffMs = 250;
  private int connectTimeoutSeconds = 3;
  private int readWriteTimeoutSeconds = 3;
  private int outboxBatchSize = 50;
  private long outboxPollIntervalMs = 1000;

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getVhost() {
    return vhost;
  }

  public void setVhost(String vhost) {
    this.vhost = vhost;
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

  public String getRoutingKey() {
    return routingKey;
  }

  public void setRoutingKey(String routingKey) {
    this.routingKey = routingKey;
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

  public String getBindingKey() {
    return bindingKey;
  }

  public void setBindingKey(String bindingKey) {
    this.bindingKey = bindingKey;
  }

  public boolean isDeclareTopology() {
    return declareTopology;
  }

  public void setDeclareTopology(boolean declareTopology) {
    this.declareTopology = declareTopology;
  }

  public int getPublishRetries() {
    return publishRetries;
  }

  public void setPublishRetries(int publishRetries) {
    this.publishRetries = publishRetries;
  }

  public long getPublishBackoffMs() {
    return publishBackoffMs;
  }

  public void setPublishBackoffMs(long publishBackoffMs) {
    this.publishBackoffMs = publishBackoffMs;
  }

  public int getConnectTimeoutSeconds() {
    return connectTimeoutSeconds;
  }

  public void setConnectTimeoutSeconds(int connectTimeoutSeconds) {
    this.connectTimeoutSeconds = connectTimeoutSeconds;
  }

  public int getReadWriteTimeoutSeconds() {
    return readWriteTimeoutSeconds;
  }

  public void setReadWriteTimeoutSeconds(int readWriteTimeoutSeconds) {
    this.readWriteTimeoutSeconds = readWriteTimeoutSeconds;
  }

  public int getOutboxBatchSize() {
    return outboxBatchSize;
  }

  public void setOutboxBatchSize(int outboxBatchSize) {
    this.outboxBatchSize = outboxBatchSize;
  }

  public long getOutboxPollIntervalMs() {
    return outboxPollIntervalMs;
  }

  public void setOutboxPollIntervalMs(long outboxPollIntervalMs) {
    this.outboxPollIntervalMs = outboxPollIntervalMs;
  }
}
