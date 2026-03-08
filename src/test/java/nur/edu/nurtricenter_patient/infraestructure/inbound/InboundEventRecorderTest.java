package nur.edu.nurtricenter_patient.infraestructure.inbound;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InboundEventRecorderTest {

  private InboundEventRepository repository;
  private InboundEventRecorder recorder;

  @BeforeEach
  void setUp() {
    repository = mock(InboundEventRepository.class);
    recorder = new InboundEventRecorder(repository);
  }

  @Test
  void tryStart_shouldReturnFalseWhenEventIdIsNull() {
    boolean result = recorder.tryStart(null, "event.name", "routing.key", UUID.randomUUID(), 1,
        LocalDateTime.now(), "{}");
    assertFalse(result);
    verify(repository, never()).saveAndFlush(any());
  }

  @Test
  void tryStart_shouldReturnFalseWhenEventAlreadyExists() {
    UUID eventId = UUID.randomUUID();
    InboundEventEntity existing = new InboundEventEntity();
    when(repository.findByEventId(eventId)).thenReturn(Optional.of(existing));

    boolean result = recorder.tryStart(eventId, "event.name", "routing.key", UUID.randomUUID(), 1,
        LocalDateTime.now(), "{}");

    assertFalse(result);
    verify(repository, never()).saveAndFlush(any());
  }

  @Test
  void tryStart_shouldSaveAndReturnTrueWhenEventIsNew() {
    UUID eventId = UUID.randomUUID();
    when(repository.findByEventId(eventId)).thenReturn(Optional.empty());

    boolean result = recorder.tryStart(eventId, "event.name", "routing.key", UUID.randomUUID(), 1,
        LocalDateTime.now(), "{}");

    assertTrue(result);
    ArgumentCaptor<InboundEventEntity> captor = ArgumentCaptor.forClass(InboundEventEntity.class);
    verify(repository).saveAndFlush(captor.capture());
    InboundEventEntity saved = captor.getValue();
    assertEquals(eventId, saved.getEventId());
    assertEquals("event.name", saved.getEventName());
    assertEquals("routing.key", saved.getRoutingKey());
    assertEquals("RECEIVED", saved.getStatus());
  }

  @Test
  void tryStart_shouldReturnFalseOnDataIntegrityViolation() {
    UUID eventId = UUID.randomUUID();
    when(repository.findByEventId(eventId)).thenReturn(Optional.empty());
    when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

    boolean result = recorder.tryStart(eventId, "event.name", "routing.key", UUID.randomUUID(), 1,
        LocalDateTime.now(), "{}");

    assertFalse(result);
  }

  @Test
  void markProcessed_shouldSetStatusToProcessed() {
    UUID eventId = UUID.randomUUID();
    InboundEventEntity entity = new InboundEventEntity();
    entity.setEventId(eventId);
    entity.setStatus("RECEIVED");
    when(repository.findByEventId(eventId)).thenReturn(Optional.of(entity));

    recorder.markProcessed(eventId);

    ArgumentCaptor<InboundEventEntity> captor = ArgumentCaptor.forClass(InboundEventEntity.class);
    verify(repository).save(captor.capture());
    assertEquals("PROCESSED", captor.getValue().getStatus());
  }

  @Test
  void markFailed_shouldSetStatusToFailedAndSaveError() {
    UUID eventId = UUID.randomUUID();
    InboundEventEntity entity = new InboundEventEntity();
    entity.setEventId(eventId);
    entity.setStatus("RECEIVED");
    when(repository.findByEventId(eventId)).thenReturn(Optional.of(entity));

    recorder.markFailed(eventId, "something went wrong");

    ArgumentCaptor<InboundEventEntity> captor = ArgumentCaptor.forClass(InboundEventEntity.class);
    verify(repository).save(captor.capture());
    assertEquals("FAILED", captor.getValue().getStatus());
    assertEquals("something went wrong", captor.getValue().getLastError());
  }

  @Test
  void markProcessed_shouldDoNothingWhenEventNotFound() {
    UUID eventId = UUID.randomUUID();
    when(repository.findByEventId(eventId)).thenReturn(Optional.empty());

    recorder.markProcessed(eventId);

    verify(repository, never()).save(any());
  }
}
