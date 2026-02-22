package nur.edu.nurtricenter_patient.infraestructure.outbox;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity {
  @Id
  private UUID id;

  private String aggregateType;
  private String aggregateId;
  private String eventType;
  private String eventName;

  @Column(columnDefinition = "TEXT")
  private String payload;

  private LocalDateTime occurredOn;
  private LocalDateTime processedOn;
  private Integer attempts;
  private LocalDateTime nextAttemptAt;

  @Column(columnDefinition = "TEXT")
  private String lastError;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getAggregateType() {
    return aggregateType;
  }

  public void setAggregateType(String aggregateType) {
    this.aggregateType = aggregateType;
  }

  public String getAggregateId() {
    return aggregateId;
  }

  public void setAggregateId(String aggregateId) {
    this.aggregateId = aggregateId;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getEventName() {
    return eventName;
  }

  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public LocalDateTime getOccurredOn() {
    return occurredOn;
  }

  public void setOccurredOn(LocalDateTime occurredOn) {
    this.occurredOn = occurredOn;
  }

  public LocalDateTime getProcessedOn() {
    return processedOn;
  }

  public void setProcessedOn(LocalDateTime processedOn) {
    this.processedOn = processedOn;
  }

  public Integer getAttempts() {
    return attempts;
  }

  public void setAttempts(Integer attempts) {
    this.attempts = attempts;
  }

  public LocalDateTime getNextAttemptAt() {
    return nextAttemptAt;
  }

  public void setNextAttemptAt(LocalDateTime nextAttemptAt) {
    this.nextAttemptAt = nextAttemptAt;
  }

  public String getLastError() {
    return lastError;
  }

  public void setLastError(String lastError) {
    this.lastError = lastError;
  }
}
