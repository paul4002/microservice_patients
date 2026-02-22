package nur.edu.nurtricenter_patient.infraestructure.outbox;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import nur.edu.nurtricenter_patient.domain.patient.events.PatientCreatedEvent;

class OutboxEventMapperTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @AfterEach
  void cleanupMdc() {
    MDC.clear();
  }

  @Test
  void toEntity_shouldMapStandardFieldsAndUseCorrelationIdFromMdc() throws IOException {
    UUID patientId = UUID.randomUUID();
    UUID subscriptionId = UUID.randomUUID();
    UUID correlationId = UUID.randomUUID();
    MDC.put("correlation_id", correlationId.toString());

    PatientCreatedEvent event = new PatientCreatedEvent(patientId, "Juan Perez", "1234567", subscriptionId);
    OutboxEventMapper mapper = new OutboxEventMapper(objectMapper);

    OutboxEventEntity entity = mapper.toEntity(event);

    assertNotNull(entity.getId());
    assertEquals(event.getId(), entity.getEventId());
    assertEquals("PatientCreatedEvent", entity.getEventName());
    assertEquals("paciente.paciente-creado", entity.getRoutingKey());
    assertEquals(1, entity.getSchemaVersion());
    assertEquals(correlationId, entity.getCorrelationId());
    assertEquals(patientId.toString(), entity.getAggregateId());

    JsonNode payload = objectMapper.readTree(entity.getPayload());
    assertEquals(patientId.toString(), payload.get("pacienteId").asText());
    assertEquals("Juan Perez", payload.get("nombre").asText());
    assertEquals("1234567", payload.get("documento").asText());
    assertEquals(subscriptionId.toString(), payload.get("suscripcionId").asText());
  }

  @Test
  void toEntity_shouldGenerateCorrelationIdWhenMissing() {
    UUID patientId = UUID.randomUUID();
    PatientCreatedEvent event = new PatientCreatedEvent(patientId, "Juan Perez", "1234567", null);
    OutboxEventMapper mapper = new OutboxEventMapper(objectMapper);

    OutboxEventEntity entity = mapper.toEntity(event);

    assertNotNull(entity.getCorrelationId());
    assertDoesNotThrow(() -> UUID.fromString(entity.getCorrelationId().toString()));
  }
}
