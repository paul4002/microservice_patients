package nur.edu.nurtricenter_patient.application.subscriptions.processSubscriptionEvent;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import an.awesome.pipelinr.Command;
import io.micrometer.core.instrument.Timer.Sample;
import jakarta.transaction.Transactional;
import nur.edu.nurtricenter_patient.core.abstractions.AggregateRoot;
import nur.edu.nurtricenter_patient.core.abstractions.IUnitOfWork;
import nur.edu.nurtricenter_patient.core.results.Error;
import nur.edu.nurtricenter_patient.core.results.ErrorType;
import nur.edu.nurtricenter_patient.core.results.Result;
import nur.edu.nurtricenter_patient.domain.patient.IPatientRepository;
import nur.edu.nurtricenter_patient.domain.patient.Patient;
import nur.edu.nurtricenter_patient.domain.patient.SubscriptionStatus;
import nur.edu.nurtricenter_patient.domain.patient.events.PatientSubscriptionRemovedEvent;
import nur.edu.nurtricenter_patient.domain.patient.events.PatientSubscriptionUpdatedEvent;
import nur.edu.nurtricenter_patient.infraestructure.inbound.InboundEventMetrics;
import nur.edu.nurtricenter_patient.infraestructure.inbound.InboundEventRecorder;

@Component
public class ProcessSubscriptionEventHandler implements Command.Handler<ProcessSubscriptionEventCommand, Result> {
  private static final Logger log = LoggerFactory.getLogger(ProcessSubscriptionEventHandler.class);
  private final IPatientRepository patientRepository;
  private final IUnitOfWork unitOfWork;
  private final InboundEventRecorder inboundEventRecorder;
  private final InboundEventMetrics metrics;
  private final Clock clock;

  public ProcessSubscriptionEventHandler(
    IPatientRepository patientRepository,
    IUnitOfWork unitOfWork,
    InboundEventRecorder inboundEventRecorder,
    InboundEventMetrics metrics
  ) {
    this.patientRepository = patientRepository;
    this.unitOfWork = unitOfWork;
    this.inboundEventRecorder = inboundEventRecorder;
    this.metrics = metrics;
    this.clock = Clock.systemUTC();
  }

  @Override
  @Transactional
  public Result handle(ProcessSubscriptionEventCommand request) {
    String eventName = resolveEventName(request);
    if (eventName == null || eventName.isBlank()) {
      return Result.failure(new Error("SubscriptionEvent.EventNameMissing", "Event name is required", ErrorType.VALIDATION));
    }
    if (request.eventId() == null) {
      return Result.failure(new Error("SubscriptionEvent.EventIdMissing", "Event id is required", ErrorType.VALIDATION));
    }

    try (
      MDC.MDCCloseable ignoredCorrelation = putMdc("correlation_id", request.correlationId() != null ? request.correlationId().toString() : null);
      MDC.MDCCloseable ignoredEventId = putMdc("event_id", request.eventId().toString())
    ) {
      LocalDateTime occurredOn = parseOccurredOn(request.occurredOn());
      boolean started = inboundEventRecorder.tryStart(
        request.eventId(),
        eventName,
        request.routingKey(),
        request.correlationId(),
        request.schemaVersion(),
        occurredOn,
        request.rawMessage()
      );
      if (!started) {
        metrics.incrementDuplicate(eventName);
        log.info("Inbound duplicate ignored: event={} eventId={}", eventName, request.eventId());
        return Result.success();
      }

      Sample latencySample = metrics.startHandlerLatency();
      Result result = switch (eventName) {
        case "suscripciones.suscripcion-actualizada" -> applySubscriptionUpdate(eventName, request.payload());
        case "contrato.creado" -> applyContractCreated(eventName, request.payload());
        case "suscripciones.suscripcion-eliminada", "contrato.cancelado", "contrato.cancelar" -> applySubscriptionRemoval(eventName, request.payload());
        default -> Result.success();
      };
      metrics.stopHandlerLatency(latencySample, eventName);
      if (result.isSuccess()) {
        inboundEventRecorder.markProcessed(request.eventId());
        metrics.incrementProcessed(eventName);
        log.info("Inbound processed: event={} eventId={}", eventName, request.eventId());
      } else {
        inboundEventRecorder.markFailed(request.eventId(), result.getError().getDescription());
        metrics.incrementFailed(eventName);
        log.warn("Inbound failed: event={} eventId={} error={}", eventName, request.eventId(), result.getError().getDescription());
      }
      return result;
    }
  }

  private MDC.MDCCloseable putMdc(String key, String value) {
    if (value == null || value.isBlank()) {
      return MDC.putCloseable(key, "");
    }
    return MDC.putCloseable(key, value);
  }

  private Result applySubscriptionUpdate(String eventName, Map<String, Object> payload) {
    UUID subscriptionId = readUuid(payload, "suscripcionId", "subscriptionId", "contratoId", "id");
    if (subscriptionId == null) {
      return Result.failure(new Error("SubscriptionEvent.SubscriptionIdMissing", "Missing subscription id in {event}", ErrorType.VALIDATION, eventName));
    }

    List<Patient> patients = patientRepository.getBySubscriptionId(subscriptionId);
    if (patients.isEmpty()) {
      return Result.success();
    }

    LocalDate endsOn = readDate(payload, "fechaFin", "endDate", "expiresOn", "vencimiento");
    SubscriptionStatus status = resolveStatus(payload, endsOn);

    List<AggregateRoot> changed = new ArrayList<>();
    for (Patient patient : patients) {
      boolean didChange = patient.syncSubscription(subscriptionId, status, endsOn);
      if (!didChange) {
        continue;
      }
      patientRepository.update(patient);
      patient.addDomainEvent(new PatientSubscriptionUpdatedEvent(
        patient.getId(),
        subscriptionId,
        status,
        endsOn,
        eventName
      ));

      changed.add(patient);
    }

    if (!changed.isEmpty()) {
      unitOfWork.commitAsync(changed.toArray(new AggregateRoot[0]));
    }
    return Result.success();
  }

  private Result applySubscriptionRemoval(String eventName, Map<String, Object> payload) {
    UUID subscriptionId = readUuid(payload, "suscripcionId", "subscriptionId", "contratoId", "id");
    if (subscriptionId == null) {
      return Result.failure(new Error("SubscriptionEvent.SubscriptionIdMissing", "Missing subscription id in {event}", ErrorType.VALIDATION, eventName));
    }

    List<Patient> patients = patientRepository.getBySubscriptionId(subscriptionId);
    if (patients.isEmpty()) {
      return Result.success();
    }

    String reason = readString(payload, "motivoCancelacion", "reason", "motivo");
    List<AggregateRoot> changed = new ArrayList<>();
    for (Patient patient : patients) {
      UUID previous = patient.getSubscriptionId();
      if (!Objects.equals(previous, subscriptionId)) {
        continue;
      }
      boolean didChange = patient.removeSubscription(SubscriptionStatus.CANCELLED, null);
      if (!didChange) {
        continue;
      }
      patientRepository.update(patient);
      patient.addDomainEvent(new PatientSubscriptionRemovedEvent(
        patient.getId(),
        previous,
        reason != null ? reason : "subscription-removed",
        eventName
      ));
      changed.add(patient);
    }

    if (!changed.isEmpty()) {
      unitOfWork.commitAsync(changed.toArray(new AggregateRoot[0]));
    }
    return Result.success();
  }

  private Result applyContractCreated(String eventName, Map<String, Object> payload) {
    UUID contractId = readUuid(payload, "contratoId", "suscripcionId", "subscriptionId", "id");
    if (contractId == null) {
      return Result.failure(new Error("SubscriptionEvent.ContractIdMissing", "Missing contratoId in {event}", ErrorType.VALIDATION, eventName));
    }

    UUID patientId = readUuid(payload, "pacienteId", "patientId");
    if (patientId == null) {
      return Result.failure(new Error("SubscriptionEvent.PatientIdMissing", "Missing pacienteId in {event}", ErrorType.VALIDATION, eventName));
    }

    Patient patient = patientRepository.getById(patientId);
    if (patient == null) {
      return Result.success();
    }

    LocalDate endsOn = readDate(payload, "fechaFin", "endDate", "expiresOn", "vencimiento");
    SubscriptionStatus status = resolveStatus(payload, endsOn);
    boolean changed = patient.syncSubscription(contractId, status, endsOn);
    if (!changed) {
      return Result.success();
    }

    patientRepository.update(patient);
    patient.addDomainEvent(new PatientSubscriptionUpdatedEvent(
      patient.getId(),
      contractId,
      status,
      endsOn,
      eventName
    ));
    unitOfWork.commitAsync(patient);
    return Result.success();
  }

  private SubscriptionStatus resolveStatus(Map<String, Object> payload, LocalDate endsOn) {
    String raw = readString(payload, "estado", "status", "subscriptionStatus");
    LocalDate today = LocalDate.now(clock);
    if (endsOn != null && endsOn.isBefore(today)) {
      return SubscriptionStatus.EXPIRED;
    }
    if (raw == null || raw.isBlank()) {
      return SubscriptionStatus.ACTIVE;
    }

    String normalized = raw.trim().toUpperCase(Locale.ROOT);
    return switch (normalized) {
      case "ACTIVE", "ACTIVA", "VIGENTE" -> SubscriptionStatus.ACTIVE;
      case "EXPIRED", "EXPIRADA", "VENCIDA" -> SubscriptionStatus.EXPIRED;
      case "CANCELLED", "CANCELADA", "INACTIVA", "INACTIVE" -> SubscriptionStatus.CANCELLED;
      default -> SubscriptionStatus.ACTIVE;
    };
  }

  private UUID readUuid(Map<String, Object> payload, String... keys) {
    for (String key : keys) {
      String value = readString(payload, key);
      if (value == null || value.isBlank()) {
        continue;
      }
      try {
        return UUID.fromString(value);
      } catch (IllegalArgumentException ex) {
        return null;
      }
    }
    return null;
  }

  private LocalDate readDate(Map<String, Object> payload, String... keys) {
    for (String key : keys) {
      String value = readString(payload, key);
      if (value == null || value.isBlank()) {
        continue;
      }
      try {
        return LocalDate.parse(value);
      } catch (DateTimeParseException ex) {
        try {
          return OffsetDateTime.parse(value).toLocalDate();
        } catch (DateTimeParseException ignored) {
          return null;
        }
      }
    }
    return null;
  }

  private String readString(Map<String, Object> payload, String... keys) {
    if (payload == null) {
      return null;
    }
    for (String key : keys) {
      Object value = payload.get(key);
      if (value == null) {
        continue;
      }
      if (value instanceof String s) {
        return s;
      }
      if (value instanceof Number n) {
        return String.valueOf(n);
      }
    }
    return null;
  }

  private String resolveEventName(ProcessSubscriptionEventCommand request) {
    if (request.eventName() != null && !request.eventName().isBlank()) {
      return request.eventName();
    }
    return request.routingKey();
  }

  private LocalDateTime parseOccurredOn(String occurredOn) {
    if (occurredOn == null || occurredOn.isBlank()) {
      return LocalDateTime.now(clock);
    }
    try {
      return OffsetDateTime.parse(occurredOn).toLocalDateTime();
    } catch (DateTimeParseException ex) {
      return LocalDateTime.now(clock);
    }
  }
}
