package nur.edu.nurtricenter_patient.infraestructure.inbound;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(
  name = "inbound_events",
  indexes = {
    @Index(name = "idx_inbound_events_event_id", columnList = "event_id"),
    @Index(name = "idx_inbound_events_status", columnList = "status"),
    @Index(name = "idx_inbound_events_received_on", columnList = "received_on")
  }
)
public class InboundEventEntity {
  @Id
  private UUID id;
  @Column(name = "event_id", nullable = false, unique = true)
  private UUID eventId;
  @Column(name = "event_name")
  private String eventName;
  @Column(name = "routing_key")
  private String routingKey;
  @Column(name = "correlation_id")
  private UUID correlationId;
  @Column(name = "schema_version")
  private Integer schemaVersion;
  private String status;
  @Column(name = "occurred_on")
  private LocalDateTime occurredOn;
  @Column(name = "received_on")
  private LocalDateTime receivedOn;
  @Column(name = "processed_on")
  private LocalDateTime processedOn;
  @Column(name = "updated_on")
  private LocalDateTime updatedOn;

  @Column(columnDefinition = "TEXT")
  private String payload;

  @Column(name = "last_error", columnDefinition = "TEXT")
  private String lastError;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getEventId() {
    return eventId;
  }

  public void setEventId(UUID eventId) {
    this.eventId = eventId;
  }

  public String getEventName() {
    return eventName;
  }

  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  public String getRoutingKey() {
    return routingKey;
  }

  public void setRoutingKey(String routingKey) {
    this.routingKey = routingKey;
  }

  public UUID getCorrelationId() {
    return correlationId;
  }

  public void setCorrelationId(UUID correlationId) {
    this.correlationId = correlationId;
  }

  public Integer getSchemaVersion() {
    return schemaVersion;
  }

  public void setSchemaVersion(Integer schemaVersion) {
    this.schemaVersion = schemaVersion;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LocalDateTime getOccurredOn() {
    return occurredOn;
  }

  public void setOccurredOn(LocalDateTime occurredOn) {
    this.occurredOn = occurredOn;
  }

  public LocalDateTime getReceivedOn() {
    return receivedOn;
  }

  public void setReceivedOn(LocalDateTime receivedOn) {
    this.receivedOn = receivedOn;
  }

  public LocalDateTime getProcessedOn() {
    return processedOn;
  }

  public void setProcessedOn(LocalDateTime processedOn) {
    this.processedOn = processedOn;
  }

  public LocalDateTime getUpdatedOn() {
    return updatedOn;
  }

  public void setUpdatedOn(LocalDateTime updatedOn) {
    this.updatedOn = updatedOn;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public String getLastError() {
    return lastError;
  }

  public void setLastError(String lastError) {
    this.lastError = lastError;
  }
}
